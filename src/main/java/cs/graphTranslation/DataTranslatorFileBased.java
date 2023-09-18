package cs.graphTranslation;

import cs.commons.ResourceEncoder;
import cs.schemaTranslation.SchemaTranslator;
import cs.utils.Constants;
import cs.utils.Utils;
import cs.utils.neo.Neo4jGraph;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DataTranslatorFileBased {
    String rdfFilePath;
    Integer expectedNumberOfClasses;
    Integer expNoOfInstances;
    ResourceEncoder resourceEncoder;
    String typePredicate;
    int commitSize = 10000;

    // In the following the size of each data structure
    // N = number of distinct nodes in the graph
    // T = number of distinct types
    // P = number of distinct predicates

    Map<Node, EntityData> entityDataHashMap; // Size == N For every entity we save a number of summary information //FIXME: entityDataHashMap can be simplified as in this transformation we only need to store class types
    Map<Integer, Integer> classEntityCount; // Size == T
    SchemaTranslator schemaTranslator;

    List<String> createNodeQueries;
    List<String> createKeyValuesQueries;
    List<String> createEdgeQueries;

    public DataTranslatorFileBased(String filePath, int expNoOfClasses, int expNoOfInstances, String typePredicate, ResourceEncoder resourceEncoder, SchemaTranslator schemaTranslator) {
        this.rdfFilePath = filePath;
        this.expectedNumberOfClasses = expNoOfClasses;
        this.expNoOfInstances = expNoOfInstances;
        this.typePredicate = typePredicate;
        this.classEntityCount = new HashMap<>((int) ((expectedNumberOfClasses) / 0.75 + 1));
        this.entityDataHashMap = new HashMap<>((int) ((expNoOfInstances) / 0.75 + 1));
        this.resourceEncoder = resourceEncoder;
        this.schemaTranslator = schemaTranslator;
    }

    /**
     * ============================================= Run Translator ========================================
     */
    public void run() {
        entityExtraction(); // extract entities and store in entityDataHashMap
        entitiesToPgNodes(); // iterate over extracted entities and convert them to PG-Nodes
        propertiesToPgKeysAndEdges();
        writeQueriesToFile();
        executeQueriesOverNeo4j();
        System.out.println("STATS: \n\t" + "No. of Classes: " + classEntityCount.size());
    }


    /**
     * ============================================= 1st Pass on file: Entity Extraction ========================================
     * Streaming over RDF (NT Format) triples <s,p,o> line by line to extract set of entity types and frequency of each entity.
     * =================================================================================================================
     */
    private void entityExtraction() {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Files.lines(Path.of(rdfFilePath)).forEach(line -> {
                try {
                    Node[] nodes = NxParser.parseNodes(line); // Get [S,P,O] as Node from triple
                    if (nodes[1].toString().equals(typePredicate)) { // Check if predicate is rdf:type or equivalent
                        // Track classes per entity
                        int objID = resourceEncoder.encodeAsResource(nodes[2].getLabel());
                        EntityData entityData = entityDataHashMap.get(nodes[0]);
                        if (entityData == null) {
                            entityData = new EntityData();
                        }
                        entityData.getClassTypes().add(objID);
                        entityDataHashMap.put(nodes[0], entityData);
                        classEntityCount.merge(objID, 1, Integer::sum);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        watch.stop();
        Utils.logTime("entityExtraction() ", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }


    /**
     * Entities to PG Nodes conversion
     */
    private void entitiesToPgNodes() {
        StopWatch watch = new StopWatch();
        watch.start();
        createNodeQueries = new ArrayList<>();
        entityDataHashMap.forEach(((node, entityData) -> {
            //System.out.println(node + " : " + entityData.getClassTypes());
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE (");
            entityData.getClassTypes().forEach(classID -> {
                sb.append(":").append(resourceEncoder.decodeAsResource(classID).getLocalName());
            });
            sb.append(" { iri : \"").append(node.getLabel()).append("\"})");
            createNodeQueries.add(sb.toString());
        }));
        watch.stop();
        Utils.logTime("entitiesToPgNodes()", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }


    private void entitiesToPgNodesBatch() {
        StopWatch watch = new StopWatch();
        watch.start();
        createNodeQueries = new ArrayList<>();
        int counter = 0;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Node, EntityData> entry : entityDataHashMap.entrySet()) {
            Node node = entry.getKey();
            EntityData entityData = entry.getValue();
            sb.append("(");
            for (Integer classID : entityData.getClassTypes()) {
                sb.append(":").append(resourceEncoder.decodeAsResource(classID).getLocalName());
            }
            sb.append(" { iri : \"").append(node.getLabel()).append("\"})");
            counter++;
            if (counter % commitSize == 0) {
                sb.append(";");
                createNodeQueries.add("CREATE \n" + sb);
                sb = new StringBuilder(); // Reset the StringBuilder for the next batch
            } else {
                sb.append(", ");
            }
        }
        if (!sb.isEmpty()) { // If there are remaining nodes not forming a complete batch, add them here
            sb.delete(sb.length() - 2, sb.length());
            createNodeQueries.add("CREATE \n" + sb + ";");
        }
        watch.stop();
        Utils.logTime("entitiesToPgNodes()", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    /**
     * ============================================= 2nd Pass on file: Entity's data (properties, etc) extraction and PG (key, values) or Edges creation ========================================
     */
    List<String> createEmptyIriNodeQueries;

    private void propertiesToPgKeysAndEdges() {
        StopWatch watch = new StopWatch();
        watch.start();
        createKeyValuesQueries = new ArrayList<>();
        createEdgeQueries = new ArrayList<>();
        createEmptyIriNodeQueries = new ArrayList<>();
        AtomicInteger idCounter = new AtomicInteger();
        try {
            //Set<Integer> pgEdgeSet = schemaTranslator.getPgSchema().getPgEdges();
            HashMap<Integer, Boolean> pgEdgeLiteralBooleanMap = schemaTranslator.getPgSchema().getPgEdgeBooleanMap();
            Files.lines(Path.of(rdfFilePath)).forEach(line -> {
                try {
                    // parsing <s,p,o> of triple from each line as node[0], node[1], and node[2]
                    Node[] nodes = NxParser.parseNodes(line);
                    if (!nodes[1].toString().equals(typePredicate)) {
                        Node entityNode = nodes[0];
                        String entityIri = entityNode.getLabel();
                        Resource propAsResource = ResourceFactory.createResource(nodes[1].getLabel());
                        int propertyKey = resourceEncoder.encodeAsResource(nodes[1].getLabel());
                        boolean isLiteralProperty = false;
                        //int objID = resourceEncoder.encodeAsResource(nodes[2].getLabel()); //Set<Integer> entityTypes = entityDataHashMap.get(entityNode).getClassTypes();

                        //1: Check if the property is a literal
                        if (pgEdgeLiteralBooleanMap.containsKey(propertyKey)) {
                            isLiteralProperty = pgEdgeLiteralBooleanMap.get(propertyKey);
                        }

                        //2: Check if the object node exists in the entityDataHashMap
                        if (entityDataHashMap.containsKey(nodes[2])) { //create an edge between the entity and the object node using the property as edge label, Add the object value as edge to the node with a match to a specific node (which should exist already)
                            String objectIri = nodes[2].getLabel();
                            String query = String.format("MATCH (s {iri: \"%s\"}), (u {iri: \"%s\"}) \nWITH s, u\nCREATE (s)-[:%s {iri : \"%s\"}]->(u);", entityIri, objectIri, propAsResource.getLocalName(), propAsResource.getURI());
                            createEdgeQueries.add(query);
                        } else if (isLiteralProperty) {
                            //PgEdge.getEdgeById(propertyKey).getDataType()
                            String key = propAsResource.getLocalName();
                            String keyValue = nodes[2].toString();
                            if (((Literal) nodes[2]).getLanguageTag() != null) {
                                keyValue = keyValue.replaceAll("@" + ((Literal) nodes[2]).getLanguageTag(), "");
                            }
                            String query = String.format("MATCH (s {iri: \"%s\"}) SET s.%s = COALESCE(s.%s, %s), s.iri = COALESCE(s.iri, \"%s\");", entityIri, key, key, keyValue, propAsResource.getURI());
                            //String entityIriPropertyIriValue = entityIri + "|" + propAsResource.getURI() + "|" + key + "|" + keyValue;
                            createKeyValuesQueries.add(query);
                        } else {
                            //String objectNodeQuery = String.format("CREATE (:%s { value : \"%s\" , iri : \"\" , dataType : \"%s\"  });", extractDataType(nodes[2]).getLocalName(), nodes[2].getLabel(), extractDataType(nodes[2]).getURI()); // Create a node for the object value
                            String objectNodeQuery = String.format("CREATE (:%s { identifier : \"%d\",  value : \"%s\" , iri : \"\" , dataType : \"%s\"  });", extractDataType(nodes[2]).getLocalName(), idCounter.get(), nodes[2].getLabel(), extractDataType(nodes[2]).getURI()); // Create a node for the object value
                            // Create an edge between the entity and the object node using the property as edge label
                            //String query = String.format("MATCH (s {iri: \"%s\"}), (u {value: \"%s\"}) \nWITH s, u\nCREATE (s)-[:%s]->(u);", entityIri, nodes[2].getLabel(), propAsResource.getLocalName());
                            String query = String.format("MATCH (s {iri: \"%s\"}), (u {identifier: \"%d\"}) \nWITH s, u\nCREATE (s)-[:%s]->(u);", entityIri, idCounter.get(), propAsResource.getLocalName());
                            createEmptyIriNodeQueries.add(objectNodeQuery);
                            createEdgeQueries.add(query);
                            idCounter.getAndIncrement();
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
            //convertQueriesToBatchQueries(createEmptyIriNodeQueries);
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        watch.stop();
        Utils.logTime("propertiesToPgKeysAndEdges()", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    private void convertQueriesToBatchQueries(List<String> createEmptyIriNodeQueries) {
        if(commitSize > createEmptyIriNodeQueries.size()){
            commitSize = createEmptyIriNodeQueries.size();
        }
        StringBuilder sb = new StringBuilder();
        int counter = 0;
        for (String query : createEmptyIriNodeQueries) {
            sb.append(query);
            counter++;
            if (counter % commitSize == 0) {
                sb.append(";");
                createNodeQueries.add("CREATE \n" + sb);
                sb = new StringBuilder(); // Reset the StringBuilder for the next batch
            } else {
                sb.append(", ");
            }
        }
        if (!sb.isEmpty()) { // If there are remaining nodes not forming a complete batch, add them here
            sb.delete(sb.length() - 2, sb.length());
            createNodeQueries.add("CREATE \n" + sb + ";");
        }
    }

    private static Resource extractDataType(Node node) {
        Resource literalDataType = ResourceFactory.createResource("http://www.w3.org/2001/XMLSchema#string");

        try {
            if (node instanceof Literal) {
                Literal objAsLiteral = (Literal) node;
                if (objAsLiteral.getDatatype() != null) {
                    literalDataType = ResourceFactory.createResource(objAsLiteral.getDatatype().getLabel());
                }
            } else {
                // Handle the case when the node is not a Literal
                //System.err.println("Error: Node is not a Literal");
                return literalDataType;
            }
        } catch (NullPointerException e) {
            // Handle NullPointerException here
            System.err.println("Error: Node is null or does not have a datatype");
            e.printStackTrace();
            return literalDataType;
        }

        return literalDataType;
    }


    private void writeQueriesToFile() {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            FileWriter fileWriter = new FileWriter(Constants.PG_NODE_QUERY_FILE_PATH);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            createNodeQueries.forEach(printWriter::println);
            printWriter.close();

            FileWriter fileWriter2 = new FileWriter(Constants.PG_KV_QUERY_FILE_PATH);
            PrintWriter printWriter2 = new PrintWriter(fileWriter2);
            //printWriter2.println("iri|property|propertyLocalName|value");
            createKeyValuesQueries.forEach(printWriter2::println);
            printWriter2.close();

            FileWriter fileWriter3 = new FileWriter(Constants.PG_EDGE_QUERY_FILE_PATH);
            PrintWriter printWriter3 = new PrintWriter(fileWriter3);
            createEdgeQueries.forEach(printWriter3::println);
            printWriter3.close();

            FileWriter fileWriter4 = new FileWriter(Constants.PG_EMPTY_IRI_NODE_QUERY_FILE_PATH);
            PrintWriter printWriter4 = new PrintWriter(fileWriter4);
            createEmptyIriNodeQueries.forEach(printWriter4::println);
            printWriter4.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        watch.stop();
        Utils.logTime("writeQueriesToFile()", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    private void executeQueriesOverNeo4j() {
        Neo4jGraph neo4jGraph = new Neo4jGraph();

        //System.out.println("Deleting all nodes from Neo4j");
        //neo4jGraph.deleteAllFromNeo4j();

        //System.out.println("\nExecuting createNodeQueries() over Neo4j");
        //neo4jGraph.batchQueries(createNodeQueries, commitSize);

        System.out.println("\nExecuting createEmptyIriNodeQueries() over Neo4j");
        neo4jGraph.batchQueries(createEmptyIriNodeQueries, commitSize);

        System.out.println("\nExecuting createKeyValuesQueries() over Neo4j");
        neo4jGraph.batchQueries(createKeyValuesQueries, commitSize);

        System.out.println("\nExecuting createEdgeQueries() over Neo4j");
        neo4jGraph.batchQueries(createEdgeQueries, commitSize);

        neo4jGraph.close();
    }

}


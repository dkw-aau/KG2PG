package cs.graphTranslation;

import cs.commons.ResourceEncoder;
import cs.schemaTranslation.SchemaTranslator;
import cs.schemaTranslation.pgSchema.PgEdge;
import cs.utils.Constants;
import cs.utils.neo.Neo4jGraph;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DataTranslatorFileBased {
    String rdfFilePath;
    Integer expectedNumberOfClasses;
    Integer expNoOfInstances;
    ResourceEncoder resourceEncoder;
    String typePredicate;

    // In the following the size of each data structure
    // N = number of distinct nodes in the graph
    // T = number of distinct types
    // P = number of distinct predicates

    Map<Node, EntityData> entityDataHashMap; // Size == N For every entity we save a number of summary information
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
        //executeQueriesOverNeo4j();
        writeQueriesToFile();
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
    }

    /**
     * Entities to PG Nodes conversion
     */
    private void entitiesToPgNodes() {
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
    }

    /**
     * ============================================= 2nd Pass on file: Entity's data (properties, etc) extraction and PG (key, values) or Edges creation ========================================
     */
    private void propertiesToPgKeysAndEdges() {
        StopWatch watch = new StopWatch();
        watch.start();
        createKeyValuesQueries = new ArrayList<>();
        createEdgeQueries = new ArrayList<>();
        try {
            Set<Integer> pgEdgeSet = schemaTranslator.getPgSchema().getPgEdges();
            Files.lines(Path.of(rdfFilePath)).forEach(line -> {
                try {
                    // parsing <s,p,o> of triple from each line as node[0], node[1], and node[2]
                    Node[] nodes = NxParser.parseNodes(line);
                    Node entityNode = nodes[0];

                    if (!nodes[1].toString().equals(typePredicate)) {
                        //Set<Integer> entityTypes = entityDataHashMap.get(entityNode).getClassTypes();
                        int propertyKey = resourceEncoder.encodeAsResource(nodes[1].toString());

                        //System.out.println("propertyKey = " + propertyKey);
                        boolean literalFlag = false;
                        //iterate over all edges in the PG-Schema
                        for (Integer pgEdge : pgEdgeSet) {
                            if (pgEdge.equals(propertyKey)) {
                                //System.out.println("pgEdge = " + pgEdge);
                                PgEdge currEdge = PgEdge.getEdgeById(pgEdge);
                                //FIXME: Think again if you should use isProperty() or only isLiteral(). What would be the difference?
                                if (currEdge.isProperty() && currEdge.isLiteral()) {
                                    //System.out.println("isKeyValueFlag = " + nodes[1].toString() + " - " + currEdge.getDataType());
                                    literalFlag = true;
                                    break;
                                }
                            }
                        }

                        Resource propAsResource = ResourceFactory.createResource(nodes[1].getLabel());
                        String entityIri = entityNode.getLabel();
                        if (literalFlag) {
                            String key = propAsResource.getLocalName();
                            String keyValue = nodes[2].toString();
                            String query = String.format("MATCH (s {iri: \"%s\"}) SET s.%s = COALESCE(s.%s, %s);", entityIri, key, key, keyValue);
                            createKeyValuesQueries.add(query);
                        } else {
                            String objectIri = nodes[2].getLabel();
                            //Add the object value as edge to the node with a match to a specific node (which should exist already)
                            String query = String.format("MATCH (s {iri: \"%s\"}), (u {iri: \"%s\"}) \nWITH s, u\nCREATE (s)-[:%s]->(u);", entityIri, objectIri, propAsResource.getLocalName());
                            createEdgeQueries.add(query);
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        watch.stop();
    }
    private void writeQueriesToFile(){
        try {
            FileWriter fileWriter = new FileWriter(Constants.PG_QUERY_FILE_PATH);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            createNodeQueries.forEach(printWriter::println);
            createKeyValuesQueries.forEach(printWriter::println);
            createEdgeQueries.forEach(printWriter::println);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void executeQueriesOverNeo4j() {
        Neo4jGraph neo4jGraph = new Neo4jGraph();
        neo4jGraph.deleteAllFromNeo4j();
        neo4jGraph.executeMultipleCypherQueries(createNodeQueries);
        neo4jGraph.executeMultipleCypherQueries(createKeyValuesQueries);
        neo4jGraph.executeMultipleCypherQueries(createEdgeQueries);
        neo4jGraph.close();
    }

}


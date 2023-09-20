package cs.graphTranslation;

import cs.commons.ResourceEncoder;
import cs.schemaTranslation.SchemaTranslator;
import cs.utils.Constants;
import cs.utils.Utils;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class DataTransFileToCsv {
    String rdfFilePath;
    Integer expectedNumberOfClasses;
    Integer expNoOfInstances;
    ResourceEncoder resourceEncoder;
    String typePredicate;
    SchemaTranslator schemaTranslator;

    // In the following the size of each data structure
    // N = number of distinct nodes in the graph
    // T = number of distinct types
    // P = number of distinct predicates

    Map<Node, EntityData> entityDataHashMap; // Size == N For every entity we save a number of summary information //FIXME: entityDataHashMap can be simplified as in this transformation we only need to store class types
    Map<Integer, Integer> classEntityCount; // Size == T

    Set<String> propertySet;

    public DataTransFileToCsv(String filePath, int expNoOfClasses, int expNoOfInstances, String typePredicate, ResourceEncoder resourceEncoder, SchemaTranslator schemaTranslator) {
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
        propertiesToPgKeysAndEdges();
        entityDataToCsv();
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
     * ============================================= 2nd Pass on file: Entity's data (properties, etc) extraction and PG (key, values) or Edges creation ========================================
     */


    private void propertiesToPgKeysAndEdges() {
        StopWatch watch = new StopWatch();
        watch.start();
        propertySet = new HashSet<>();
        PrintWriter pgLiteralNodesPrintWriter = createPrintWriter(Constants.PG_NODES_LITERALS);
        pgLiteralNodesPrintWriter.println("id:ID|object_value|object_type|object_iri|:LABEL");

        PrintWriter pgRelsPrintWriter = createPrintWriter(Constants.PG_RELATIONS);
        pgRelsPrintWriter.println(":START_ID|property|:END_ID|:TYPE");

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
                            //String query = String.format("MATCH (s {iri: \"%s\"}), (u {iri: \"%s\"}) \nWITH s, u\nCREATE (s)-[:%s {iri : \"%s\"}]->(u);", entityIri, objectIri, propAsResource.getLocalName(), propAsResource.getURI());
                            //Build a csv line with first column as entityIri, 2nd column as property iri, third column as idCounter.get(), forth column as property local name. Example: //:START_ID,property,:END_ID,:TYPE
                            String lineForNodeToNodeRel = entityIri + "|" + propAsResource.getURI() + "|" + objectIri + "|" + propAsResource.getLocalName();
                            pgRelsPrintWriter.println(lineForNodeToNodeRel);
                        } else {
                            String propLocalName = propAsResource.getLocalName();
                            String value = nodes[2].toString();
                            Resource dataTypeResource = extractDataType(nodes[2]);
                            String dataType = dataTypeResource.getURI();
                            String dataTypeLocalName = dataTypeResource.getLocalName();
                            if (nodes[2] instanceof Literal) {
                                if (((Literal) nodes[2]).getDatatype() != null) {
                                    value = nodes[2].getLabel();
                                }
                                if (((Literal) nodes[2]).getLanguageTag() != null) {
                                    value = value.replaceAll("@" + ((Literal) nodes[2]).getLanguageTag(), "");
                                }
                            } else if ((ResourceFactory.createResource(nodes[2].toString())).isURIResource()) {
                                value = nodes[2].getLabel();
                                dataTypeLocalName = "IRI";
                                dataType = "IRI";
                            }
                            value = value.replace("\\", "\"");
                            if (isLiteralProperty) {
                                if (entityDataHashMap.get(entityNode) != null) {
                                    entityDataHashMap.get(entityNode).getKeyValue().put(propLocalName, value);
                                    propertySet.add(propLocalName);
                                }
                            } else {
                                String lineForLiteral = idCounter.get() + "|" + value + "|" + dataType + "|" + entityIri + "|" + dataTypeLocalName;
                                pgLiteralNodesPrintWriter.println(lineForLiteral);
                                //String query = String.format("MATCH (s {iri: \"%s\"}), (u {identifier: \"%d\"}) \nWITH s, u\nCREATE (s)-[:%s]->(u);", entityIri, idCounter.get(), propAsResource.getLocalName());
                                //Build a csv line with first column as entityIri, 2nd column as property iri, third column as idCounter.get(), forth column as property local name. Example: //:START_ID,property,:END_ID,:TYPE
                                String lineForNodeToIdNodeRel = entityIri + "|" + propAsResource.getURI() + "|" + idCounter.get() + "|" + propLocalName;
                                pgRelsPrintWriter.println(lineForNodeToIdNodeRel);
                                idCounter.getAndIncrement();
                            }
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        pgLiteralNodesPrintWriter.close();
        pgRelsPrintWriter.close();
        watch.stop();
        Utils.logTime("propertiesToPgKeysAndEdges()", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }


    private void entityDataToCsv() {
        StopWatch watch = new StopWatch();
        watch.start();
        try (FileWriter writer = new FileWriter(Constants.PG_NODES_WD_PROP)) {
            // Write the header row with propertySet elements as column names
            writer.append("iri:ID|");
            for (String property : propertySet) {
                writer.append(property);
                writer.append("|");
            }
            writer.append(":LABEL\n"); // Add a new column for ClassTypes

            // Iterate over entityDataHashMap and write data to the CSV file
            for (Map.Entry<Node, EntityData> entry : entityDataHashMap.entrySet()) {
                Node node = entry.getKey();
                EntityData entityData = entry.getValue();

                // Write the Node value in the first column
                writer.append(node.getLabel());
                writer.append("|");

                // Iterate over propertySet and write values from keyValue map
                for (String property : propertySet) {
                    String value = entityData.keyValue.getOrDefault(property, "");
                    writer.append(value);
                    writer.append("|");
                }

                StringBuilder sb = new StringBuilder();
                StringJoiner joiner = new StringJoiner(";");
                entityData.getClassTypes().forEach(classID -> {
                    joiner.add(resourceEncoder.decodeAsResource(classID).getLocalName());
                });
                sb.append(joiner);
                writer.append(sb);
                writer.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        watch.stop();
        Utils.logTime("entityDataToCsv()", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
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

    private static PrintWriter createPrintWriter(String filePath) {
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            return new PrintWriter(fileWriter);
        } catch (IOException e) {
            throw new RuntimeException("Error creating PrintWriter for file: " + filePath, e);
        }
    }

}


package cs.graphTranslation;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;
import cs.Main;
import cs.commons.ResourceEncoder;
import cs.schemaTranslation.SchemaTranslator;
import cs.utils.*;
import kotlin.Pair;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    Map<Node, EntityData> entityDataHashMap; // Size == N For every entity we save a number of summary information
    Map<Integer, Integer> classEntityCount; // Size == T
    //Set<String> propertySet; // Size == P
    Map<String, String> prefixMap;

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
        Main.logger.info("Phase 1: Graph Data Translation - Extracting entities data from RDF file.");
        entityExtraction(); // extract entities and store in entityDataHashMap
        Main.logger.info("Phase 2: Graph Data Translation - Extracting properties data for extracted entities from RDF file.");
        propertiesToPgKeysAndEdges();
        Main.logger.info("Post Processing: Graph Data Translation - Writing entities data to CSV file.");
        entityDataToCsvAndJson();
        //groupingEntitiesByCommonProperties();
        Main.logger.info("Post Processing: Graph Data Translation - Writing prefix map to file.");
        FilesUtil.writeStringToStringMapToFile(prefixMap, Constants.PG_PREFIX_MAP);
        Main.logger.info("STATS: " + "No. of Classes: " + classEntityCount.size());
    }


    /**
     * ============================================= 1st Pass on file: Entity Extraction ========================================
     * Streaming over RDF (NT Format) triples <s,p,o> line by line to extract set of entity types and frequency of each entity.
     * =================================================================================================================
     */
    private void entityExtraction() {
        StopWatch watch = new StopWatch();
        watch.start();
        Set<String> nameSpaces = new HashSet<>();
        try {
            Files.lines(Path.of(rdfFilePath)).forEach(line -> {
                try {
                    Node[] nodes = NxParser.parseNodes(line); // Get [S,P,O] as Node from triple
                    if (nodes[1].toString().equals(typePredicate)) { // Check if predicate is rdf:type or equivalent
                        // Track classes per entity
                        //int objID = resourceEncoder.encodeAsResource(nodes[2].getLabel());
                        Resource objResource = ResourceFactory.createResource(nodes[2].getLabel());
                        int objID = resourceEncoder.encodeAsResource(objResource.getURI());
                        //extract name spaces and add into nameSpaces set
                        if (objResource.isURIResource())
                            nameSpaces.add(objResource.getNameSpace());
                        EntityData entityData = entityDataHashMap.get(nodes[0]);
                        if (entityData == null) {
                            entityData = new EntityData();
                        }
                        entityData.getClassTypes().add(objID);
                        entityDataHashMap.put(nodes[0], entityData);
                        classEntityCount.merge(objID, 1, Integer::sum);
                    } else {
                        //Extract namespace from property and add to nameSpaces set
                        Resource propResource = ResourceFactory.createResource(nodes[1].getLabel());
                        if (propResource.isURIResource())
                            nameSpaces.add(propResource.getNameSpace());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        prefixMap = convertNameSpacesSetToPrefixMap(nameSpaces);
        watch.stop();
        Utils.logTime("entityExtraction() ", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    /**
     * ============================================= 2nd Pass on file: Entity's data (properties, etc) extraction and PG (key, values) or Edges creation ========================================
     */

    private void propertiesToPgKeysAndEdges() {
        StopWatch watch = new StopWatch();
        watch.start();
        //propertySet = new HashSet<>();
        TypesMapper typesMapper = new TypesMapper();
        typesMapper.initTypesForCypher();
        PrintWriter pgLiteralNodesPrintWriter = createPrintWriter(Constants.PG_NODES_LITERALS);
        pgLiteralNodesPrintWriter.println("id:ID|object_value|object_type|type|:LABEL");

        PrintWriter pgRelsPrintWriter = createPrintWriter(Constants.PG_RELATIONS);
        pgRelsPrintWriter.println(":START_ID|property|:END_ID|:TYPE");

        try {
            AtomicInteger idCounter = new AtomicInteger();
            idCounter.set(0);
            Map<Pair<Integer, Integer>, Boolean> pgNodeEdgeBooleanMap = schemaTranslator.getPgSchema().getPgNodeEdgeBooleanMap();
            Files.lines(Path.of(rdfFilePath)).forEach(line -> {
                try {
                    // parsing <s,p,o> of triple from each line as node[0], node[1], and node[2]
                    Node[] nodes = NxParser.parseNodes(line);
                    if (!nodes[1].toString().equals(typePredicate) && entityDataHashMap.containsKey(nodes[0])) {
                        Node entityNode = nodes[0];
                        String entityIri = entityNode.getLabel();
                        Resource propAsResource = ResourceFactory.createResource(nodes[1].getLabel());
                        String propPrefixedLocalName = prefixMap.get(propAsResource.getNameSpace()) + "_" + propAsResource.getLocalName();
                        int propertyKey = resourceEncoder.encodeAsResource(nodes[1].getLabel());
                        boolean isLiteralProperty = false;

                        Set<Boolean> booleanSet = new HashSet<>();
                        entityDataHashMap.get(entityNode).getClassTypes().forEach(classID -> {
                            Pair<Integer, Integer> nodeEdgePair = new Pair<>(classID, propertyKey);
                            if (pgNodeEdgeBooleanMap.containsKey(nodeEdgePair)) {
                                booleanSet.add(pgNodeEdgeBooleanMap.get(nodeEdgePair));
                            }
                        });

                        if (booleanSet.size() == 1) {
                            isLiteralProperty = booleanSet.iterator().next();
                        }

                        //2: Check if the object node exists in the entityDataHashMap
                        if (entityDataHashMap.containsKey(nodes[2])) { //create an edge between the entity and the object node using the property as edge label, Add the object value as edge to the node with a match to a specific node (which should exist already)
                            String objectIri = nodes[2].getLabel();
                            //String query = String.format("MATCH (s {iri: \"%s\"}), (u {iri: \"%s\"}) \nWITH s, u\nCREATE (s)-[:%s {iri : \"%s\"}]->(u);", entityIri, objectIri, propAsResource.getLocalName(), propAsResource.getURI());
                            //Build a csv line with first column as entityIri, 2nd column as property iri, third column as objectIri, forth column as property local name. Example: //:START_ID,property,:END_ID,:TYPE
                            String lineForNodeToNodeRel = entityIri + "|" + propAsResource.getURI() + "|" + objectIri + "|" + propPrefixedLocalName;
                            pgRelsPrintWriter.println(lineForNodeToNodeRel);
                        } else {
                            //String propLocalName = propAsResource.getLocalName();
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
                            char[] bytes = value.toCharArray();

                            for (int i = 0; i < bytes.length; i++) {
                                /*if (value.charAt(i) == 92 && i + 1 != bytes.length && bytes[i + 1] != 110) bytes[i] = 34;*/
                                if (value.charAt(i) == 92 && (i + 1 != bytes.length) && (bytes[i + 1] == 34) && (i + 1 != bytes.length) && (bytes[i - 1] != 92))
                                    bytes[i] = 34;
                            }

                            value = new String(bytes);
                            if (isLiteralProperty) {
                                if (entityDataHashMap.get(entityNode) != null) {
                                    entityDataHashMap.get(entityNode).getKeyValue().put(propPrefixedLocalName, value);
                                    //propertySet.add(propLocalName);
                                }
                            } else {
                                //String lineForLiteral = id + "|" + value + "|" + dataType + "|" + entityIri + "|" + dataTypeLocalName;
                                int id = idCounter.getAndIncrement();
                                String cypherType = typesMapper.getMap().get(dataType);
                                if (cypherType == null) cypherType = "STRING";
                                //id:ID|object_value|object_type|type|object_iri|:LABEL
                                String lineForLiteral = id + "|" + value + "|" + dataType + "|" + cypherType + "|" + dataTypeLocalName + ";LitNode";

                                pgLiteralNodesPrintWriter.println(lineForLiteral);
                                //String query = String.format("MATCH (s {iri: \"%s\"}), (u {identifier: \"%d\"}) \nWITH s, u\nCREATE (s)-[:%s]->(u);", entityIri, id, propAsResource.getLocalName());
                                //Build a csv line with first column as entityIri, 2nd column as property iri, third column as id, forth column as property local name. Example: //:START_ID,property,:END_ID,:TYPE
                                String lineForNodeToIdNodeRel = entityIri + "|" + propAsResource.getURI() + "|" + id + "|" + propPrefixedLocalName;
                                pgRelsPrintWriter.println(lineForNodeToIdNodeRel);
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


    /**
     * ============================================= Writing entity data to CSV File ========================================
     */

    private void groupingEntitiesByCommonProperties() {
        Main.logger.info("Grouping entities by common properties.");
        // Create a ConcurrentHashMap to store the grouping of property keys to nodes
        ConcurrentHashMap<Set<String>, Set<Node>> groupedEntities = new ConcurrentHashMap<>();

        entityDataHashMap.entrySet().parallelStream().forEach(entry -> {
            Node node = entry.getKey();
            EntityData entityData = entry.getValue();

            Map<String, String> propKeyToValue = entityData.getKeyValue();

            // Extract the property keys and store them in a set
            Set<String> propertyKeys = propKeyToValue.keySet();

            // Add the current node to the set of nodes with common property keys
            groupedEntities.merge(propertyKeys, new HashSet<>(Collections.singleton(node)), (existingSet, newNodeSet) -> {
                existingSet.addAll(newNodeSet);
                return existingSet;
            });
        });
        Main.logger.info("Created " + groupedEntities.size() + " groups of entities.");
        Main.logger.info("Generating CSV files per group.");
        genCsvPerGroup(groupedEntities);
        Main.logger.info("Generating STATS CSV for groups.");
        writeGroupsStatsToCsv(groupedEntities);
    }

    // Helper method to generate CSV files per group of entities
    private void genCsvPerGroup(@NotNull ConcurrentHashMap<Set<String>, Set<Node>> groupedEntities) {
        // Define the output directory where CSV files will be saved
        String outputDirectory = ConfigManager.getProperty("output_file_path") + "pg_nodes/";
        // Counter to generate incrementing numbers for file names
        AtomicInteger fileCounter = new AtomicInteger(1);

        // Iterate through the groupedEntities and create CSV files
        for (Map.Entry<Set<String>, Set<Node>> entry : groupedEntities.entrySet()) {
            Set<String> propertyKeys = entry.getKey();
            if (!propertyKeys.isEmpty()) {
                Set<Node> nodes = entry.getValue();
                // Generate the file name with an incrementing number
                String fileName = generateFileName(fileCounter, entry.getKey().size(), entry.getValue().size());
                // Create a CSV file for this property group
                String csvFileName = outputDirectory + fileName;
                //Main.logger.info("Generating CSV file: " + csvFileName);
                try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFileName), CSVFormat.DEFAULT.withDelimiter('|').withQuote(null))) {
                    // Write CSV header row
                    List<String> header = new ArrayList<>();
                    header.add("iri:ID");
                    header.addAll(propertyKeys);
                    header.add(":LABEL");
                    csvPrinter.printRecord(header);

                    // Write data rows
                    for (Node node : nodes) {
                        EntityData entityData = entityDataHashMap.get(node);
                        List<String> row = new ArrayList<>();
                        row.add(node.getLabel());
                        for (String propertyKey : propertyKeys) {
                            row.add(entityData.getKeyValue().get(propertyKey));
                        }
                        StringBuilder sb = new StringBuilder();
                        StringJoiner joiner = new StringJoiner(";");
                        entityData.getClassTypes().forEach(classID -> {
                            //joiner.add(resourceEncoder.decodeAsResource(classID).getLocalName());
                            Resource type = resourceEncoder.decodeAsResource(classID);
                            String prefixedTypeName = prefixMap.get(type.getNameSpace()) + "_" + type.getLocalName();
                            joiner.add(prefixedTypeName);
                        });
                        sb.append(joiner);
                        row.add(sb.toString());
                        csvPrinter.printRecord(row);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    // Helper method to generate file names with an incrementing number
    private static String generateFileName(AtomicInteger fileCounter, int propSize, int valueSize) {
        int counter = fileCounter.getAndIncrement();
        return "PG_NODES_WD_PROP_" + counter + "_" + propSize + "_" + valueSize + ".csv";
    }

    public static void writeGroupsStatsToCsv(ConcurrentHashMap<Set<String>, Set<Node>> groupedEntities) {
        try (FileWriter writer = new FileWriter(ConfigManager.getProperty("output_file_path") + "propGroupsStats.csv")) {
            writer.append("Props|CountValues");
            writer.append("\n");

            // Iterate through the sorted ConcurrentHashMap and write data to CSV
            for (Map.Entry<Set<String>, Set<Node>> entry : groupedEntities.entrySet()) {
                Set<String> keySet = entry.getKey();
                Set<Node> valueSet = entry.getValue();
                int keySize = keySet.size();
                int valueSize = valueSet.size();
                writer.append(keySet.toString());
                writer.append("|");
                writer.append(String.valueOf(keySize));
                writer.append("|");
                writer.append(String.valueOf(valueSize));
                writer.append("\n");
            }

            System.out.println("CSV file has been written successfully!");
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }

    /**
     * ============================================= Helper Methods ========================================
     */

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

    private static Map<String, String> convertNameSpacesSetToPrefixMap(Set<String> nameSpaces) {
        Map<String, String> namespaceMap = new HashMap<>();
        int index = 0;

        for (String namespace : nameSpaces) {
            String prefix = "ns" + index;
            namespaceMap.put(namespace, prefix);
            index++;
        }
        return namespaceMap;
    }


    private void entityDataToCsvAndJson() {
        System.out.println("Transforming entity data to CSV.");
        StopWatch watch = new StopWatch();
        watch.start();
        ObjectMapper objectMapper = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        try (FileWriter csvWriter = new FileWriter(Constants.PG_NODES_WD_LABELS)) {
            try (PrintWriter jsonWriter = new PrintWriter(new BufferedWriter(new FileWriter(Constants.PG_NODES_PROPS_JSON)))) {
                csvWriter.append("iri:ID|:LABEL\n");
                jsonWriter.write("["); // Insert '[' at the beginning of the file
                Iterator<Map.Entry<Node, EntityData>> iterator = entityDataHashMap.entrySet().iterator();
                boolean isFirstEntry = true;
                while (iterator.hasNext()) {
                    Map.Entry<Node, EntityData> entry = iterator.next();
                    Node node = entry.getKey();
                    EntityData entityData = entry.getValue();

                    // Write the Node value in the first column
                    csvWriter.append(node.getLabel());
                    csvWriter.append("|");

                    StringBuilder sb = new StringBuilder();
                    StringJoiner joiner = new StringJoiner(";");
                    entityData.getClassTypes().forEach(classID -> {
                        Resource typeResource = resourceEncoder.decodeAsResource(classID);
                        String prefixedType = prefixMap.get(typeResource.getNameSpace()) + "_" + typeResource.getLocalName();
                        joiner.add(prefixedType);
                    });
                    joiner.add("Node");
                    sb.append(joiner);
                    csvWriter.append(sb);
                    csvWriter.append("\n");

                    // Check if entityData.getKeyValue() is not empty
                    if (!entityData.getKeyValue().isEmpty()) {
                        // Create a JSON object for each entry
                        ObjectNode jsonObject = objectMapper.createObjectNode();

                        // Set the ID using node.getLabel()
                        jsonObject.put("iri", node.getLabel());

                        // Create a "properties" object and add properties from entityData.getKeyValue()
                        ObjectNode propertiesObject = objectMapper.createObjectNode();
                        for (Map.Entry<String, String> mapEntry : entityData.getKeyValue().entrySet()) {
                            String key = mapEntry.getKey();
                            String value = mapEntry.getValue();
                            propertiesObject.put(key, value);
                        }
                        if (isValid(propertiesObject.toString(), objectMapper))
                            jsonObject.set("properties", propertiesObject); // Set the "properties" object
                        else Main.logger.warn("Invalid properties JSON object: " + propertiesObject);

                        if (isValid(jsonObject.toString(), objectMapper)) {
                            if (!isFirstEntry) {
                                jsonWriter.write(",");
                            }
                            jsonWriter.println(jsonObject);// Serialize the JSON object to a string and write it to the output file
                            if (isFirstEntry) {
                                isFirstEntry = false;
                            }
                        } else {
                            Main.logger.warn("Invalid complete JSON object: " + jsonObject);
                        }
                    }
                }

                prefixesToJson(jsonWriter, objectMapper);

                jsonWriter.write("]"); // After the loop, close the JSON array
            } catch (IOException e) {
                e.printStackTrace();
            }
            csvWriter.append("http://relweb.cs.aau.dk/kg2pg/prefixes|Node;Prefixes\n"); // Add node for prefixes with specific label
        } catch (IOException e) {
            e.printStackTrace();
        }
        watch.stop();
        Utils.logTime("entityDataToCsvAndJson()", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    private void prefixesToJson(PrintWriter jsonWriter, ObjectMapper objectMapper) {
        if (!prefixMap.isEmpty()) {
            jsonWriter.write(",");
            ObjectNode prefixMapObject = objectMapper.createObjectNode();
            prefixMapObject.put("iri", "http://relweb.cs.aau.dk/kg2pg/prefixes");
            ObjectNode prefixesNode = convertPrefixMapToJson(prefixMap);
            if (isValid(prefixesNode.toString(), objectMapper))
                prefixMapObject.set("properties", prefixesNode);
            jsonWriter.println(prefixMapObject);
        }
    }

    public boolean isValid(String json, ObjectMapper mapper) {
        try {
            mapper.readTree(json);
        } catch (JacksonException e) {
            return false;
        }
        return true;
    }

    public static ObjectNode convertPrefixMapToJson(Map<String, String> prefixMap) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode resultObject = objectMapper.createObjectNode();
        for (Map.Entry<String, String> entry : prefixMap.entrySet()) {
            resultObject.put(entry.getValue(), entry.getKey());
        }
        return resultObject;
    }

// This way of writing sparse csv file using all properties is very
// expensive and results in generation of very large csv file for large graphs
   /* private void entityDataToCsv() {
        System.out.println("Transforming entity data to CSV.");
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
    }*/

}


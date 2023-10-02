package cs.schemaTranslation.pgSchema;

import cs.commons.ResourceEncoder;
import cs.utils.Constants;
import cs.utils.neo.Neo4jGraph;
import kotlin.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.jena.rdf.model.Resource;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class PgSchemaWriter {
    ResourceEncoder resourceEncoder;
    PgSchema pgSchema;
    List<String> pgSchemaNodeQueries = new ArrayList<>();
    List<String> pgSchemaEdgesQueries = new ArrayList<>();

    Set<String> pgNodeTypes = new HashSet<>();
    List<String> pgEdgeTypes = new ArrayList<>();
    List<String> pgEdgeCardinality = new ArrayList<>();

    public PgSchemaWriter(ResourceEncoder encoder, PgSchema pgSchema) {
        this.resourceEncoder = encoder;
        this.pgSchema = pgSchema;
    }

    public void parseSchema() {
        Map<Integer, Map<Pair<Integer, Integer>, Pair<Integer, Integer>>> pgNodePgEdgeMap = new HashMap<>(); // Iterate over pgSchema.nodeEdgeCardinality to create pgNodePgEdgeMap which is a map of nodeId to a map of edgeId to cardinality;
        pgSchema.getNodeEdgeCardinalityMap().forEach((pair, cardinality) -> {
            Integer nodeId = pair.getFirst();
            if (pgNodePgEdgeMap.get(nodeId) != null) {
                pgNodePgEdgeMap.get(nodeId).put(pair, cardinality);
            } else {
                pgNodePgEdgeMap.put(nodeId, new HashMap<>());
                pgNodePgEdgeMap.get(nodeId).put(pair, cardinality);
            }
        });

        // Iterate over pgNodePgEdgeMap to handle literal type properties
        pgNodePgEdgeMap.forEach((nodeId, data) -> {
            Resource nodeAsResource = resourceEncoder.decodeAsResource(nodeId);
            List<String> properties = new ArrayList<>();
            data.forEach((edge, cardinality) -> {
                Integer edgeId = edge.getSecond();
                Resource edgeAsResource = resourceEncoder.decodeAsResource(edgeId);
                PgEdge pgEdge = PgEdge.getEdgeById(edgeId);
                if (pgEdge.isLiteral()) {
                    if (cardinality.equals(new Pair<>(1, 1))) {
                        String property = " %s : %s".formatted(edgeAsResource.getLocalName(), pgEdge.getDataType().toUpperCase());
                        properties.add(property);
                    } else if (cardinality.equals(new Pair<>(0, 0))) {
                        String property = " OPTIONAL %s : %s ARRAY {} ".formatted(edgeAsResource.getLocalName(), pgEdge.getDataType().toUpperCase());
                        properties.add(property);
                    } else if (cardinality.equals(new Pair<>(0, 1))) {
                        String property = " OPTIONAL %s : %s".formatted(edgeAsResource.getLocalName(), pgEdge.getDataType().toUpperCase());
                        properties.add(property);
                    } else if (cardinality.getFirst().equals(0) && cardinality.getSecond() > 1) {
                        String property = " OPTIONAL %s : %s ARRAY {0, %d}".formatted(edgeAsResource.getLocalName(), pgEdge.getDataType().toUpperCase(), cardinality.getSecond());
                        properties.add(property);
                    } else if (cardinality.getFirst().equals(1) && cardinality.getSecond() > 1) {
                        String property = " OPTIONAL %s : %s ARRAY {1, %d}".formatted(edgeAsResource.getLocalName(), pgEdge.getDataType().toUpperCase(), cardinality.getSecond());
                        properties.add(property);
                    }
                }
            });
            String nodeType;
            if (properties.isEmpty()) {
                nodeType = "(%sType: %s { id: %d, iri: \"%s\" })".formatted(nodeAsResource.getLocalName().toLowerCase(), nodeAsResource.getLocalName(), nodeId, nodeAsResource.getURI());
            } else {
                nodeType = "(%sType: %s { id: %d, iri: \"%s\", %s })".formatted(nodeAsResource.getLocalName().toLowerCase(), nodeAsResource.getLocalName(), nodeId, nodeAsResource.getURI(), String.join(", ", properties));
            }
            pgNodeTypes.add(nodeType);
        });

        pgSchema.nodeEdgeTarget.forEach((nodeEdgePair, targetNodes) -> {
            //CREATE EDGE TYPE (:CustomerType)-[OwnsAccountType: owns]->(:AccountType)
            Integer sourceNodeId = nodeEdgePair.getFirst();
            Integer edgeId = nodeEdgePair.getSecond();
            Resource sourceNodeAsResource = resourceEncoder.decodeAsResource(sourceNodeId);
            Resource edgeAsResource = resourceEncoder.decodeAsResource(edgeId);
            PgEdge pgEdge = PgEdge.getEdgeById(edgeId);
            Pair<Integer, Integer> edgeCardinality = pgSchema.getNodeEdgeCardinalityMap().get(nodeEdgePair);
            boolean skipFlag = edgeCardinality.equals(new Pair<>(-1, -1));

            if (targetNodes.isEmpty()) {
                String edgeType = "CREATE EDGE TYPE (:%sType)-[%sType: %s { iri: \"%s\" }]->()".formatted(sourceNodeAsResource.getLocalName(), edgeAsResource.getLocalName(), edgeAsResource.getLocalName(), edgeAsResource.getURI());
                pgEdgeTypes.add(edgeType);
            } else if (targetNodes.size() == 1) {
                Resource targetNodeAsResource = resourceEncoder.decodeAsResource(targetNodes.iterator().next());
                String edgeType = "CREATE EDGE TYPE (:%sType)-[%sType: %s { iri: \"%s\" } ]->(:%sType)".formatted(sourceNodeAsResource.getLocalName().toLowerCase(), edgeAsResource.getLocalName(), edgeAsResource.getLocalName(), edgeAsResource.getURI(), targetNodeAsResource.getLocalName().toLowerCase());
                pgEdgeTypes.add(edgeType);
                if (!skipFlag) {
                    String edgeCard = "";
                    //char is = sourceNodeAsResource.getLocalName().toLowerCase().charAt(0); //initial for source node
                    //char it = targetNodeAsResource.getLocalName().toLowerCase().charAt(0); // initial for target node
                    char it, is;
                    String isLocalName = sourceNodeAsResource.getLocalName();
                    String itLocalName = targetNodeAsResource.getLocalName();
                    if (itLocalName != null && !itLocalName.isEmpty() && isLocalName != null && !isLocalName.isEmpty()) {
                        is = isLocalName.toLowerCase().charAt(0);
                        it = itLocalName.toLowerCase().charAt(0);
                        if (edgeCardinality.getFirst().equals(-1)) {
                            edgeCard = "FOR (%s: %s) COUNT %d..%d OF %s WITHIN (%s)-[:%s]->(%s: %s)".formatted(is, sourceNodeAsResource.getLocalName(), 0, edgeCardinality.getSecond(), it, is, edgeAsResource.getLocalName(), it, targetNodeAsResource.getLocalName());
                        } else if (edgeCardinality.getSecond().equals(-1))
                            edgeCard = "FOR (%s: %s) COUNT %d.. OF %s WITHIN (%s)-[:%s]->(%s: %s)".formatted(is, sourceNodeAsResource.getLocalName(), edgeCardinality.getFirst(), it, is, edgeAsResource.getLocalName(), it, targetNodeAsResource.getLocalName());
                        else
                            edgeCard = "FOR (%s: %s) COUNT %d..%s OF %s WITHIN (%s)-[:%s]->(%s: %s)".formatted(is, sourceNodeAsResource.getLocalName(), edgeCardinality.getFirst(), edgeCardinality.getSecond(), it, is, edgeAsResource.getLocalName(), it, targetNodeAsResource.getLocalName());

                        pgEdgeCardinality.add(edgeCard);
                    } else {
                        // Handle the case where localName is null or empty
                        System.out.println("One of these is null: sourceNode: " + sourceNodeAsResource.getLocalName() + " targetNode: " + targetNodeAsResource.getLocalName());
                    }
                }
            } else {
                List<String> targetNodeTypes = new ArrayList<>();
                List<String> tNodeTypes = new ArrayList<>();
                for (Integer tNodeId : targetNodes) {
                    Resource targetNodeAsResource = resourceEncoder.decodeAsResource(tNodeId);
                    PgNode targetPgNode = PgNode.getNodeById(tNodeId);
                    if (targetPgNode.isAbstract()) {
                        String nodeType = "(%sType: %s { id: %d, iri: \"%s\" })".formatted(targetNodeAsResource.getLocalName(), targetNodeAsResource.getLocalName(), tNodeId, targetNodeAsResource.getURI());
                        targetNodeTypes.add(targetNodeAsResource.getLocalName().toLowerCase() + "Type");
                        tNodeTypes.add(StringUtils.capitalize(targetNodeAsResource.getLocalName()));
                        pgNodeTypes.add(nodeType); // This is a special case, here you will also encounter target abstract node types, so you need to handle them
                    } else {
                        targetNodeTypes.add(targetNodeAsResource.getLocalName().toLowerCase() + "Type");
                        tNodeTypes.add(StringUtils.capitalize(targetNodeAsResource.getLocalName()));

                    }
                }
                String edgeType = "CREATE EDGE TYPE (:%sType)-[%sType: %s { iri: \"%s\" }]->(:%s)".formatted(sourceNodeAsResource.getLocalName(), edgeAsResource.getLocalName(), edgeAsResource.getLocalName(), edgeAsResource.getURI(), String.join(" | :", targetNodeTypes));
                pgEdgeTypes.add(edgeType);


                if (!skipFlag) {
                    String edgeCard = "";
                    char is = sourceNodeAsResource.getLocalName().toLowerCase().charAt(0); //initial for source node
                    char it = 'T';
                    if (edgeCardinality.getFirst().equals(-1))
                        //edgeCard = "FOR (%s: %s) COUNT %d..%d OF %s WITHIN (%s)-[:%s]->(%s: %s)".formatted(sourceNodeAsResource.getLocalName(), sourceNodeAsResource.getLocalName(), 0, edgeCardinality.getSecond(), edgeAsResource.getLocalName(), sourceNodeAsResource.getLocalName(), edgeAsResource.getLocalName(), String.join(" | ", targetNodeTypes), String.join(" | ", targetNodeTypes));
                        edgeCard = "FOR (%s: %s) COUNT %d..%d OF %s WITHIN (%s)-[:%s]->(%s: {%s})".formatted(is, sourceNodeAsResource.getLocalName(), 0, edgeCardinality.getSecond(), it, is, edgeAsResource.getLocalName(), it, String.join(" | ", tNodeTypes));
                    else if (edgeCardinality.getSecond().equals(-1))
                        edgeCard = "FOR (%s: %s) COUNT %d.. OF %s WITHIN (%s)-[:%s]->(%s: {%s})".formatted(is, sourceNodeAsResource.getLocalName(), 0, it, is, edgeAsResource.getLocalName(), it, String.join(" | ", tNodeTypes));
                    else
                        edgeCard = "FOR (%s: %s) COUNT %d..%d OF %s WITHIN (%s)-[:%s]->(%s: {%s})".formatted(is, sourceNodeAsResource.getLocalName(), edgeCardinality.getFirst(), edgeCardinality.getSecond(), it, is, edgeAsResource.getLocalName(), it, String.join(" | ", tNodeTypes));
                    pgEdgeCardinality.add(edgeCard);
                }
            }
        });

        // **************  Create Neo4j Queries ********************
        /*
        for (Map.Entry<Integer, List<Integer>> entry : pgSchema.nodesToEdges.entrySet()) { // Iterate over nodesToEdges map
            Integer nodeId = entry.getKey();
            String createNodeQuery = String.format("CREATE (n:Node {id: %d, iri : \"%s\"});", nodeId, resourceEncoder.decodeAsResource(nodeId).getURI()); // Create Neo4j query to create node
            pgSchemaNodeQueries.add(createNodeQuery);
        }


        for (Map.Entry<Pair<Integer, Integer>, Set<Integer>> entry : pgSchema.nodeEdgeTarget.entrySet()) { // Iterate over nodeEdgeTarget map
            Pair<Integer, Integer> key = entry.getKey();
            Integer sourceNodeId = key.getFirst();
            Integer edgeId = key.getSecond();
            for (Integer targetNode : entry.getValue()) {
                // Create Neo4j query to create edge with source and target nodes
                String createEdgeWithNodesQuery = String.format("""
                        MATCH (source:Node {id: %d}), (target:Node {id: %d}) WITH source, target CREATE (source)-[:Edge {id: %d , iri: "%s"}]->(target);
                        """, sourceNodeId, targetNode, edgeId, resourceEncoder.decodeAsResource(edgeId).getURI());
                pgSchemaEdgesQueries.add(createEdgeWithNodesQuery);
            }
        }*/
    }

    public void writePgSchemaSyntaxToFile() {
        try {
            FileWriter fileWriter = new FileWriter(Constants.PG_SCHEMA_SYNTAX_FILE_PATH);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(" // Node Types");
            pgNodeTypes.forEach(printWriter::println);

            printWriter.println("");
            printWriter.println(" // Edge Types");
            pgEdgeTypes.forEach(printWriter::println);

            printWriter.println("");
            printWriter.println(" // Cardinalities of Edges");
            pgEdgeCardinality.forEach(printWriter::println);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePgSchemaCypherQueriesToFile() {
        try {
            FileWriter fileWriter = new FileWriter(Constants.PG_SCHEMA_QUERY_FILE_PATH);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            pgSchemaNodeQueries.forEach(printWriter::println);
            pgSchemaEdgesQueries.forEach(printWriter::println);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void executeQueriesOverNeo4j() {
        StopWatch watch = new StopWatch();
        watch.start();
        Neo4jGraph neo4jGraph = new Neo4jGraph();
        neo4jGraph.deleteAllFromNeo4j();
        neo4jGraph.executeMultipleCypherQueries(pgSchemaNodeQueries);
        neo4jGraph.executeMultipleCypherQueries(pgSchemaEdgesQueries);
        neo4jGraph.close();
        watch.stop();
        System.out.println("Time taken to execute queries over Neo4j: " + watch.getTime() + " ms");
    }

    public String replaceAngles(String str) {
        return str.replaceAll("<", "").replaceAll(">", "");
    }

    public String getLastPartAfterSlash(String url) {
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
            return url.substring(lastSlashIndex + 1);
        } else {
            return "";
        }
    }

    private String constructNodeQuery(Integer nodeId, String nodeIri) {
        return String.format("CREATE (n:Node {id: %d, iri : \"%s\"});", nodeId, nodeIri);
    }
    //construct node query with a list of properties

}

package cs.schemaTranslation.pgSchema;

import cs.commons.ResourceEncoder;
import cs.utils.Constants;
import cs.utils.neo.Neo4jGraph;
import kotlin.Pair;
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

    public PgSchemaWriter(ResourceEncoder encoder, PgSchema pgSchema) {
        this.resourceEncoder = encoder;
        this.pgSchema = pgSchema;
    }

    public void parseSchema() {

        // Iterate over pgSchema.nodeEdgeCardinality to create pgNodePgEdgeMap which is a map of nodeId to a map of edgeId to cardinality;
        Map<Integer, Map<Pair<Integer, Integer>, Pair<Integer, Integer>>> pgNodePgEdgeMap = new HashMap<>();
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
                nodeType = "(%sType: %s { id: %d, iri: \"%s\" })".formatted(nodeAsResource.getLocalName(), nodeAsResource.getLocalName(), nodeId, nodeAsResource.getURI());
            } else {
                nodeType = "(%sType: %s { id: %d, iri: \"%s\", %s })".formatted(nodeAsResource.getLocalName(), nodeAsResource.getLocalName(), nodeId, nodeAsResource.getURI(), String.join(", ", properties));
            }
            System.out.println(nodeType);

        });

        pgSchema.nodeEdgeTarget.forEach((nodeEdgePair, targetNodes) -> {
            //CREATE EDGE TYPE (:CustomerType)-[OwnsAccountType: owns]->(:AccountType)
            Integer sourceNodeId = nodeEdgePair.getFirst();
            Integer edgeId = nodeEdgePair.getSecond();
            Resource sourceNodeAsResource = resourceEncoder.decodeAsResource(sourceNodeId);
            Resource edgeAsResource = resourceEncoder.decodeAsResource(edgeId);
            PgEdge pgEdge = PgEdge.getEdgeById(edgeId);

            if (targetNodes.isEmpty()) {
                String edgeType = "CREATE EDGE TYPE (:%sType)-[%sType: %s]->()".formatted(sourceNodeAsResource.getLocalName(), edgeAsResource.getLocalName(), edgeAsResource.getLocalName());
                System.out.println(edgeType);
            } else if (targetNodes.size() == 1) {
                Resource targetNodeAsResource = resourceEncoder.decodeAsResource(targetNodes.iterator().next());
                String edgeType = "CREATE EDGE TYPE (:%sType)-[%sType: %s]->(:%sType)".formatted(sourceNodeAsResource.getLocalName(), edgeAsResource.getLocalName(), edgeAsResource.getLocalName(), targetNodeAsResource.getLocalName());
                System.out.println(edgeType);
            } else {
                List<String> targetNodeTypes = new ArrayList<>();
                //FIXME: This is a special case, here you will also encounter target abstract node types, so you need to handle them

                for (Integer tNodeId : targetNodes) {
                    Resource targetNodeAsResource = resourceEncoder.decodeAsResource(tNodeId);
                    PgNode targetPgNode = PgNode.getNodeById(tNodeId);
                    if (targetPgNode.isAbstract()) {
                        targetNodeTypes.add(targetNodeAsResource.getLocalName() + "Type");

                    } else {
                        targetNodeTypes.add(targetNodeAsResource.getLocalName());
                    }

                }
                String edgeType = "CREATE EDGE TYPE (:%sType)-[%sType: %s]->(:%sType)".formatted(sourceNodeAsResource.getLocalName(), edgeAsResource.getLocalName(), edgeAsResource.getLocalName(), String.join(" | ", targetNodeTypes));
                System.out.println(edgeType);
            }
        });


        System.out.println("=======================================");
        // Iterate over nodesToEdges map
        for (Map.Entry<Integer, List<Integer>> entry : pgSchema.nodesToEdges.entrySet()) {
            Integer nodeId = entry.getKey();
            String createNodeQuery = String.format("CREATE (n:Node {id: %d, iri : \"%s\"});", nodeId, resourceEncoder.decodeAsResource(nodeId).getURI()); // Create Neo4j query to create node
            pgSchemaNodeQueries.add(createNodeQuery);
        }

        // Iterate over nodeEdgeTarget map
        for (Map.Entry<Pair<Integer, Integer>, Set<Integer>> entry : pgSchema.nodeEdgeTarget.entrySet()) {
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

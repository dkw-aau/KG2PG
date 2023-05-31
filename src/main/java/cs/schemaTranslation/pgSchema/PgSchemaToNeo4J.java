package cs.schemaTranslation.pgSchema;

import cs.commons.ResourceEncoder;
import kotlin.Pair;

import java.util.Map;
import java.util.Set;

public class PgSchemaToNeo4J {
    ResourceEncoder resourceEncoder;
    PgSchema pgSchema;

    public PgSchemaToNeo4J(ResourceEncoder encoder, PgSchema pgSchema) {
        this.resourceEncoder = encoder;
        this.pgSchema = pgSchema;
    }

    public void generateNeo4jQueries() {
        StringBuilder queryBuilder = new StringBuilder();

        // Iterate over nodesToEdges map
        for (Map.Entry<Integer, Set<Integer>> entry : pgSchema.nodesToEdges.entrySet()) {
            Integer nodeId = entry.getKey();
            Set<Integer> edgeIds = entry.getValue();

            // Create Neo4j query to create node
            String createNodeQuery = String.format("CREATE (n:Node {id: %d, iri : \"%s\"});\n", nodeId, resourceEncoder.decodeAsResource(nodeId).getLocalName());
            queryBuilder.append(createNodeQuery);
        }

        System.out.println(queryBuilder);

        // Iterate over nodeEdgeTarget map
        for (Map.Entry<Pair<Integer, Integer>, Set<Integer>> entry : pgSchema.nodeEdgeTarget.entrySet()) {
            Pair<Integer, Integer> key = entry.getKey();
            Integer sourceNodeId = key.getFirst();
            Integer edgeId = key.getSecond();
            for (Integer targetNode : entry.getValue()) {
                // Create Neo4j query to create edge with source and target nodes
                StringBuilder qb = new StringBuilder();
                String createEdgeWithNodesQuery = String.format("""
                        MATCH (source:Node {id: %d}), (target:Node {id: %d})
                        WITH source, target
                        CREATE (source)-[:Edge {id: %d , iri: "%s"}]->(target);
                        """, sourceNodeId, targetNode, edgeId, resourceEncoder.decodeAsResource(edgeId).getURI());
                qb.append(createEdgeWithNodesQuery);
                System.out.println(qb);
            }
        }
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

}

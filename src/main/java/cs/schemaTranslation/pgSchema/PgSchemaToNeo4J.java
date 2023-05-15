package cs.schemaTranslation.pgSchema;

import cs.commons.StringEncoder;
import kotlin.Pair;

import java.util.Map;
import java.util.Set;

public class PgSchemaToNeo4J {
    StringEncoder stringEncoder;
    PgSchema pgSchema;

    public PgSchemaToNeo4J(StringEncoder encoder, PgSchema pgSchema) {
        this.stringEncoder = encoder;
        this.pgSchema = pgSchema;
    }

    public String generateNeo4jQueries() {
        StringBuilder queryBuilder = new StringBuilder();

        // Iterate over nodesToEdges map
        for (Map.Entry<Integer, Set<Integer>> entry : pgSchema.nodesToEdges.entrySet()) {
            Integer nodeId = entry.getKey();
            Set<Integer> edgeIds = entry.getValue();

            // Create Neo4j query to create node
            String createNodeQuery = String.format("CREATE (n:Node {id: %d, iri : \"%s\"});\n", nodeId, getLastPartAfterSlash(stringEncoder.decode(nodeId)));
            queryBuilder.append(createNodeQuery);

        }

        // Iterate over nodeEdgeTarget map
        for (Map.Entry<Pair<Integer, Integer>, Integer> entry : pgSchema.nodeEdgeTarget.entrySet()) {
            Pair<Integer, Integer> key = entry.getKey();
            Integer sourceNodeId = key.getFirst();
            Integer edgeId = key.getSecond();
            Integer targetNodeId = entry.getValue();

            // Create Neo4j query to create edge with source and target nodes
            String createEdgeWithNodesQuery = String.format("""
                    MATCH (source:Node {id: %d}), (target:Node {id: %d})
                    WITH source, target
                    CREATE (source)-[:Edge {id: %d , iri: "%s"}]->(target);
                    """, sourceNodeId, targetNodeId, edgeId, getLastPartAfterSlash(stringEncoder.decode(edgeId)));
            queryBuilder.append(createEdgeWithNodesQuery);
        }
        System.out.println(queryBuilder.toString());
        return queryBuilder.toString();
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

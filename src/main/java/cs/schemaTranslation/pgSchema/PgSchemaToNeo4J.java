package cs.schemaTranslation.pgSchema;

import cs.commons.ResourceEncoder;
import cs.utils.Constants;
import cs.utils.neo.Neo4jGraph;
import kotlin.Pair;
import org.apache.commons.lang3.time.StopWatch;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PgSchemaToNeo4J {
    ResourceEncoder resourceEncoder;
    PgSchema pgSchema;
    List<String> pgSchemaNodeQueries = new ArrayList<>();
    List<String> pgSchemaEdgesQueries = new ArrayList<>();

    public PgSchemaToNeo4J(ResourceEncoder encoder, PgSchema pgSchema) {
        this.resourceEncoder = encoder;
        this.pgSchema = pgSchema;
    }

    public void generateCypherQueries() {
        // Iterate over nodesToEdges map
        for (Map.Entry<Integer, Set<Integer>> entry : pgSchema.nodesToEdges.entrySet()) {
            Integer nodeId = entry.getKey();
            //Set<Integer> edgeIds = entry.getValue();
            //FIXME: Parse the edges to include single-type literal edges within the node, considering their cardinality

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

}

package cs.utils.neo;

import cs.utils.Utils;
import org.apache.commons.lang3.time.StopWatch;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.neo4j.driver.Values.parameters;

public class Neo4jGraph {
    //http://a256-gc1-17.srv.aau.dk:7474/browser/
    String SERVER_ROOT_URI = "bolt://10.92.0.34:7687";
    //String SERVER_ROOT_URI = "bolt://a256-gc1-17.srv.aau.dk:7687";
    String username = "neo4j";
    String password = "12345678";
    private final Driver driver;
    private final int maxThreads; // Adjust the number of threads as needed

    public Neo4jGraph() {
        this.driver = GraphDatabase.driver(SERVER_ROOT_URI, AuthTokens.basic(username, password));
        this.maxThreads = 16;
    }

    public boolean nodeExistsWithIri(String iriValue) {
        try (Session session = driver.session()) {
            String query = "MATCH (n) WHERE n.iri = $iriValue RETURN COUNT(n) > 0 AS exists";
            return session.readTransaction(tx -> {
                try {
                    var result = tx.run(query, Map.of("iriValue", iriValue));
                    return result.single().get("exists").asBoolean();
                } catch (ClientException e) {
                    // Handle any exceptions here
                    e.printStackTrace();
                    return false;
                }
            });
        }
    }

    public void createNodeWithIri(String iriValue) {
        try (Session session = driver.session()) {
            String query = "CREATE (n:Node {iri: $iriValue})";
            session.writeTransaction(tx -> {
                tx.run(query, Map.of("iriValue", iriValue));
                return null;
            });
        }
    }

    public void addLabelToNodeWithIri(String iriValue, String label) {
        try (Session session = driver.session()) {
            String query = "MATCH (n {iri: $iriValue}) SET n:" + label;
            session.writeTransaction(tx -> {
                tx.run(query, Map.of("iriValue", iriValue));
                return null;
            });
        }
    }

    public void createEdgeBetweenTwoNodes(String sourceIri, String targetIri, String edgeName, String propertyKey, String propertyValue) {
        try (Session session = driver.session()) {
            String query = "MATCH (source {iri: $sourceIri}), (target {iri: $targetIri}) " +
                    "MERGE (source)-[:" + edgeName + " {" + propertyKey + ": $propertyValue}]->(target)";
            session.writeTransaction(tx -> {
                tx.run(query, Map.of("sourceIri", sourceIri, "targetIri", targetIri, "propertyValue", propertyValue));
                return null;
            });
        }
    }

    public void createLiteralObjectNode(int id, String objectType, String objectValue, String type) {
        try (Session session = driver.session()) {
            String query = "CREATE (n:LitNode {id: $id, object_type: $objectType, object_value: $objectValue, type: $type})";
            Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            params.put("objectType", objectType);
            params.put("objectValue", objectValue);
            params.put("type", type);

            session.writeTransaction(tx -> {
                tx.run(query, params);
                return null;
            });
        }
    }

    public void createEdgeBetweenAnIriAndLitNode(String sourceIri, int targetNodeId, String edgeName, String propertyKey, String propertyValue) {
        try (Session session = driver.session()) {
            String query = "MATCH (source {iri: $sourceIri}), (target {id: $targetId}) " +
                    "MERGE (source)-[:" + edgeName + " {" + propertyKey + ": $propertyValue}]->(target)";
            session.writeTransaction(tx -> {
                tx.run(query, Map.of("sourceIri", sourceIri, "targetId", targetNodeId, "propertyValue", propertyValue));
                return null;
            });
        }
    }


    public long getTotalNodeCount() {
        try (Session session = driver.session()) {
            String query = "MATCH (n) RETURN count(n) AS totalNodes";
            return session.readTransaction(tx -> {
                Result result = tx.run(query);
                return result.single().get("totalNodes").asLong();
            });
        }
    }

    public long getTotalLiteralNodeCount() {
        try (Session session = driver.session()) {
            String query = "MATCH (n:LitNode) RETURN count(n) AS totalNodes";
            return session.readTransaction(tx -> {
                Result result = tx.run(query);
                return result.single().get("totalNodes").asLong();
            });
        }
    }

    public void executeMultipleCypherQueries(List<String> cypherQueries) {
        try (Session session = driver.session()) {
            StopWatch watch = new StopWatch();
            watch.start();
            session.writeTransaction(tx -> {
                cypherQueries.forEach(tx::run);
                return null;
            });
            watch.stop();
            Utils.logTime("executeMultipleCypherQueries()", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
        }
    }

    public void deleteAllFromNeo4j() {
        try (Session session = driver.session()) {
            StopWatch watch = new StopWatch();
            watch.start();
            session.writeTransaction(tx -> {
                tx.run("MATCH (n) DETACH DELETE n");
                return null;
            });
            watch.stop();
            Utils.logTime("deleteAllFromNeo4j()", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));

        }
    }

    public void batchQueries(List<String> queries, int commitSize) {
        StopWatch watch = new StopWatch();
        watch.start();

        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);

        try (Driver driver = GraphDatabase.driver(SERVER_ROOT_URI, AuthTokens.basic(username, password))) {
            for (int i = 0; i < queries.size(); i += commitSize) {
                int endIndex = Math.min(i + commitSize, queries.size());
                List<String> batch = queries.subList(i, endIndex);

                executor.submit(() -> {
                    try (Session session = driver.session()) {
                        try (Transaction transaction = session.beginTransaction()) {
                            for (String query : batch) {
                                try {
                                    transaction.run(query);
                                } catch (ClientException e) {
                                    System.err.println("ICQ: " + query);
                                }
                            }
                            transaction.commit();
                        }
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                });
            }

            // Shutdown the executor and wait for all tasks to complete
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        watch.stop();
        Utils.logTime("batchQueries()", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    public void batchQueriesSimple(List<String> queries, int commitSize) {
        StopWatch watch = new StopWatch();
        watch.start();
        try (Driver driver = this.driver) {
            try (Session session = driver.session()) {
                // Execute queries in batches
                for (int i = 0; i < queries.size(); i += commitSize) {
                    int endIndex = Math.min(i + commitSize, queries.size());
                    List<String> batch = queries.subList(i, endIndex);

                    try (Transaction transaction = session.beginTransaction()) {
                        for (String query : batch) {
                            transaction.run(query);
                        }
                        transaction.commit();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        watch.stop();
        Utils.logTime("batchQueries()", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    public void executeSingleCypherQuery(String cypherQuery) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(cypherQuery);
                return null;
            });
        }
    }

    public void close() {
        driver.close();
    }
}
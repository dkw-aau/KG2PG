package cs.graphTranslation.npm;

import cs.Main;
import cs.utils.ConfigManager;
import cs.utils.Utils;
import org.apache.commons.lang3.time.StopWatch;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.exceptions.Neo4jException;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class QueryUtilsNeo4j {
    private final Driver driver;
    private final String db;

    public QueryUtilsNeo4j(String db, String url, String username, String password) {
        this.db = db;
        this.driver = GraphDatabase.driver(url, AuthTokens.basic(username, password));
    }

    public boolean nodeExistsWithIri(String iriValue) {
        try (Session session = driver.session(SessionConfig.forDatabase(db))) {
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

    //*****
    public String getCypherCreateNodeWithIri(String iriValue) {
        return "MERGE (n:Node {iri: \"" + iriValue + "\"}) ON CREATE SET n += {iri: \"" + iriValue + "\"};";
    }


    public String getCypherAddLabelToNodeWithIri(String iriValue, String label) {
        return "MATCH (n:Node {iri: \"" + iriValue + "\"}) SET n:" + label + ";";
    }

    public String getCypherCreateEdgeBetweenTwoNodes(String sourceIri, String targetIri, String edgeName, String propertyKey, String propertyValue) {
        return "MATCH (source:Node {iri: \"" + sourceIri + "\"}), (target:Node {iri: \"" + targetIri + "\"}) " +
                "MERGE (source)-[:" + edgeName + " {" + propertyKey + ": \"" + propertyValue + "\"}]->(target);";
    }

    public String getCypherCreateLiteralObjectNode(int id, String objectType, String objectValue, String type) {
        return "CREATE (n:LitNode {id: " + id + ", object_type: \"" + objectType + "\", object_value: \"" + objectValue + "\", type: \"" + type + "\"});";
    }

    public String getCypherCreateEdgeBetweenAnIriAndLitNode(String sourceIri, int targetNodeId, String edgeName, String propertyKey, String propertyValue) {
        return "MATCH (source:Node {iri: \"" + sourceIri + "\"}), (target:LitNode {id: " + targetNodeId + "}) " +
                "MERGE (source)-[:" + edgeName + " {" + propertyKey + ": \"" + propertyValue + "\"}]->(target);";
    }

    //******

    public void createNodeWithIri(String iriValue) {
        try (Session session = driver.session(SessionConfig.forDatabase(db))) {
            String query = "CREATE (n:Node {iri: $iriValue})";
            session.writeTransaction(tx -> {
                tx.run(query, Map.of("iriValue", iriValue));
                return null;
            });
        }
    }

    public void addLabelToNodeWithIri(String iriValue, String label) {
        try (Session session = driver.session(SessionConfig.forDatabase(db))) {
            String query = "MATCH (n {iri: $iriValue}) SET n:" + label;
            session.writeTransaction(tx -> {
                tx.run(query, Map.of("iriValue", iriValue));
                return null;
            });
        }
    }

    public void createEdgeBetweenTwoNodes(String sourceIri, String targetIri, String edgeName, String propertyKey, String propertyValue) {
        try (Session session = driver.session(SessionConfig.forDatabase(db))) {
            String query = "MATCH (source {iri: $sourceIri}), (target {iri: $targetIri}) " +
                    "MERGE (source)-[:" + edgeName + " {" + propertyKey + ": $propertyValue}]->(target)";
            session.writeTransaction(tx -> {
                tx.run(query, Map.of("sourceIri", sourceIri, "targetIri", targetIri, "propertyValue", propertyValue));
                return null;
            });
        }
    }

    public void createLiteralObjectNode(int id, String objectType, String objectValue, String type) {
        try (Session session = driver.session(SessionConfig.forDatabase(db))) {
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
        try (Session session = driver.session(SessionConfig.forDatabase(db))) {
            String query = "MATCH (source {iri: $sourceIri}), (target {id: $targetId}) " +
                    "MERGE (source)-[:" + edgeName + " {" + propertyKey + ": $propertyValue}]->(target)";
            session.writeTransaction(tx -> {
                tx.run(query, Map.of("sourceIri", sourceIri, "targetId", targetNodeId, "propertyValue", propertyValue));
                return null;
            });
        }
    }

    public void deleteRelationshipForLitNode(String sourceIri, String property, String prefixedEdge, String targetObjectValue) {
        try (Session session = driver.session(SessionConfig.forDatabase(db))) {
            String query = "MATCH (source:Node {iri: $sourceIri})-[rel:" + prefixedEdge + " {property: $property}]->(target:LitNode {object_value: " + targetObjectValue + "}) DELETE rel";
            session.writeTransaction(tx -> {
                Result result = tx.run(query, Values.parameters("sourceIri", sourceIri, "property", property));
                if (!result.consume().counters().containsUpdates()) {
                    Main.logger.error("Delete Relation (Lit) Query NOT SUCCESSFUL: " + query);
                }
                return null;
            });
        }
    }

    public void deleteRelationshipForIriNode(String sourceIri, String property, String prefixedEdge, String targetIri) {
        try (Session session = driver.session(SessionConfig.forDatabase(db))) {
            String query = "MATCH (source:Node {iri: $sourceIri})-[rel:" + prefixedEdge + " {property: $property}]->(target:Node {iri: $targetIri}) DELETE rel";
            session.writeTransaction(tx -> {
                Result result = tx.run(query, Values.parameters("sourceIri", sourceIri, "property", property, "targetIri", targetIri));
                if (!result.consume().counters().containsUpdates()) {
                    Main.logger.error("Delete Relation Query did not execute successfully.");
                }
                return null;
            });
        }
    }


    public void updateObjectValueForLitNode(String sourceIri, String prefixedEdge, String property, String targetObjectValue, String newObjectValue) {
        try (Session session = driver.session(SessionConfig.forDatabase(db))) {
            String query = "MATCH (source:Node{iri: '$sourceIri'})-[rel:" + prefixedEdge + " {property: '$property'}]->(target:LitNode {object_value: $targetObjectValue}) SET target.object_value = $newObjectValue";
            String queryWithValues = query
                    .replace("$sourceIri", sourceIri)
                    .replace("$property", property)
                    .replace("$targetObjectValue", targetObjectValue)
                    .replace("$newObjectValue", newObjectValue);
            try {
                Result result = session.writeTransaction(tx -> {
                    //Result queryResult = tx.run(query, Values.parameters( "sourceIri", sourceIri, "property", property, "targetObjectValue", targetObjectValue, "newObjectValue", newObjectValue));
                    return tx.run(queryWithValues);
                });
                // Check if the query was executed successfully
                if (!result.consume().counters().containsUpdates()) {
                    Main.logger.error("Query NOT SUCCESSFUL :: updateObjectValueForLitNode()  " + queryWithValues);
                }
            } catch (Neo4jException e) {
                System.err.println("Neo4j Exception: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("General Exception: " + e.getMessage());
            }
        }
    }


    public void deleteNodeByPropertyValue(String label, String property, String propertyValue) {
        try (Session session = driver.session(SessionConfig.forDatabase(db))) {
            String query = "MATCH (target:" + label + " {" + property + ": $propertyValue}) DELETE target";
            System.out.println("Query: " + query);
            session.writeTransaction(tx -> {
                Result result = tx.run(query, Values.parameters("propertyValue", propertyValue));
                // Check if the query was executed successfully
                if (result.consume().counters().containsUpdates()) {
                    System.out.println("Delete Query executed successfully.");
                } else {
                    System.out.println("Delete Query did not execute successfully.");
                }
                return null;
            });
        }
    }

    public long getTotalNodeCount() {
        try (Session session = driver.session(SessionConfig.forDatabase(db))) {
            String query = "MATCH (n) RETURN count(n) AS totalNodes";
            return session.readTransaction(tx -> {
                Result result = tx.run(query);
                return result.single().get("totalNodes").asLong();
            });
        }
    }

    public long getTotalLiteralNodeCount() {
        try (Session session = driver.session(SessionConfig.forDatabase(db))) {
            String query = "MATCH (n:LitNode) RETURN count(n) AS totalNodes";
            return session.readTransaction(tx -> {
                Result result = tx.run(query);
                return result.single().get("totalNodes").asLong();
            });
        }
    }


    public void executeQueriesInBatches(LinkedHashSet<String> queries, int commitSize) {
        StopWatch watch = new StopWatch();
        watch.start();

        try (Driver driver = this.driver) {
            try (Session session = driver.session(SessionConfig.forDatabase(db))) {
                Iterator<String> queryIterator = queries.iterator();
                int counter = 0;
                while (queryIterator.hasNext()) {
                    try (Transaction transaction = session.beginTransaction()) {
                        for (int i = 0; i < commitSize && queryIterator.hasNext(); i++) {
                            String query = queryIterator.next();
                            //System.out.println(query);
                            transaction.run(query);
                        }
                        transaction.commit();
                        System.out.println("Commit ... " + counter);
                        counter++;
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        watch.stop();
        Utils.logTime("batchQueries()", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }


    public void close() {
        driver.close();
    }
}
package cs.graphTranslation.npm;

import cs.utils.ConfigManager;
import cs.utils.Utils;
import org.apache.commons.lang3.time.StopWatch;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.exceptions.Neo4jException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class QueryUtilsNeo4j {
    private final Driver driver;
    private final String db;

    public QueryUtilsNeo4j() {
        this.db = ConfigManager.getProperty("neo4j_db");
        this.driver = GraphDatabase.driver(
                ConfigManager.getProperty("neo4j_URL"),
                AuthTokens.basic(ConfigManager.getProperty("neo4j_username"), ConfigManager.getProperty("neo4j_password")));
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
                if (result.consume().counters().containsUpdates()) {
                    System.out.println("Delete Relation (Lit) Query executed successfully.");
                } else {
                    System.out.println("Delete Relation (Lit) Query did not execute successfully.");
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
                // Check if the query was executed successfully
                if (result.consume().counters().containsUpdates()) {
                    System.out.println("Delete Relation Query executed successfully.");
                } else {
                    System.out.println("Delete Relation Query did not execute successfully.");
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
                if (result.consume().counters().containsUpdates()) {
                    System.out.println("Query executed successfully.");
                } else {
                    System.out.println("Query did not execute successfully.");
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

    public void close() {
        driver.close();
    }
}
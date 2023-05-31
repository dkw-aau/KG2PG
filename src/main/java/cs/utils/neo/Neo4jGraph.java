package cs.utils.neo;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.net.URISyntaxException;

import static org.neo4j.driver.Values.parameters;

public class Neo4jGraph {
    private static final String SERVER_ROOT_URI = "bolt://10.92.0.34:7687";
    private final Driver driver;

    public Neo4jGraph() {
        this.driver = GraphDatabase.driver(SERVER_ROOT_URI, AuthTokens.basic("neo4j", "notension12125"));
    }

    public void executeCypherQuery(String cypherQuery) {
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

    public void addNode(String value) {
        try (Session session = driver.session()) {
            session.run("CREATE (:Node {value: $value})", parameters("value", value));
        }
    }
    public void connectNodes(String source, String target) {
        try (Session session = driver.session()) {
            session.run("MATCH\n" +
                    "  (a:Node),\n" +
                    "  (b:Node)\n" +
                    "WHERE a.value = $A AND b.value = $B\n" +
                    "CREATE (a)-[r:Relation {name: a.value + '<->' + b.value}]->(b)", parameters("A", source, "B", target));
        }
    }

    public static void main(String[] args) throws URISyntaxException {
//        cs.utils.Neo4jGraph neo = new cs.utils.Neo4jGraph();
//        neo.addNode(String.valueOf(1));
//        neo.addNode(String.valueOf(2));
//        neo.connectNodes(String.valueOf(1), String.valueOf(2));
    }
}
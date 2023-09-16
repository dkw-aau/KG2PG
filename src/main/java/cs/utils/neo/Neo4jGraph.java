package cs.utils.neo;

import org.neo4j.driver.*;

import java.util.List;

import static org.neo4j.driver.Values.parameters;

public class Neo4jGraph {
    //http://a256-gc1-17.srv.aau.dk:7474/browser/
    //String SERVER_ROOT_URI = "bolt://10.92.0.34:7687";
    String SERVER_ROOT_URI = "bolt://a256-gc1-17.srv.aau.dk:7687";
    String username = "neo4j";
    String password = "12345678";
    private final Driver driver;

    public Neo4jGraph() {
        this.driver = GraphDatabase.driver(SERVER_ROOT_URI, AuthTokens.basic(username, password));
    }

    public void executeSingleCypherQuery(String cypherQuery) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(cypherQuery);
                return null;
            });
        }
    }

    public void executeMultipleCypherQueries(List<String> cypherQueries) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                cypherQueries.forEach(tx::run);
                return null;
            });
        }
    }

    public void deleteAllFromNeo4j() {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (n) DETACH DELETE n");
                return null;
            });
        }
    }

    public void close() {
        driver.close();
    }

}
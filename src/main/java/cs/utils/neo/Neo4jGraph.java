package cs.utils.neo;

import cs.utils.Utils;
import org.apache.commons.lang3.time.StopWatch;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.exceptions.Neo4jException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Neo4jGraph {
    String SERVER_ROOT_URI = "bolt://10.92.0.34:7687";
    String username = "neo4j";
    String password = "12345678";
    String db = "examplev0";
    private final Driver driver;


    public Neo4jGraph() {
        this.driver = GraphDatabase.driver(SERVER_ROOT_URI, AuthTokens.basic(username, password));
    }

    public void executeMultipleCypherQueries(List<String> cypherQueries) {
        try (Session session = driver.session(SessionConfig.forDatabase(db))) {
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
        try (Session session = driver.session(SessionConfig.forDatabase(db))) {
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

    public void batchQueries(List<String> queries, int commitSize, int maxThreads) {
        StopWatch watch = new StopWatch();
        watch.start();

        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);

        try (Driver driver = GraphDatabase.driver(SERVER_ROOT_URI, AuthTokens.basic(username, password))) {
            for (int i = 0; i < queries.size(); i += commitSize) {
                int endIndex = Math.min(i + commitSize, queries.size());
                List<String> batch = queries.subList(i, endIndex);

                executor.submit(() -> {
                    try (Session session = driver.session(SessionConfig.forDatabase(db))) {
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
            try (Session session = driver.session(SessionConfig.forDatabase(db))) {
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
        try (Session session = driver.session(SessionConfig.forDatabase(db))) {
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
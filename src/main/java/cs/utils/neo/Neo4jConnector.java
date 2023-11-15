package cs.utils.neo;

import cs.utils.ConfigManager;
import org.neo4j.driver.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Neo4jConnector {
    Driver driver;

    Neo4jConnector(String uri, String user, String password) {
        try {
            this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeCypherQueries(List<String[]> indexAndQuery, String dbForSession) {
        indexAndQuery.remove(0);
        FileWriter writer = null;
        try {
            writer = new FileWriter(ConfigManager.getProperty("output_file_path") + dbForSession + "_results.csv");
            writer.write("Index,executionTime,fetchTime,totalTime,numberOfResults\n");
            System.out.println("Index,executionTime,fetchTime,totalTime,numberOfResults");
            Session session = null;
            try {
                session = driver.session(SessionConfig.forDatabase(dbForSession));
                // At this point, the connection should be established
                for (String[] array : indexAndQuery) {
                    String index = array[0];
                    String query = array[1];

                    try (Transaction transaction = session.beginTransaction()) {
                        long startTime = System.currentTimeMillis();
                        Result result = transaction.run(query);
                        long endTime = System.currentTimeMillis();
                        long executionTime = endTime - startTime;

                        long fetchStartTime = System.currentTimeMillis();
                        int totalResults = 0;
                        while (result.hasNext()) {
                            result.next();
                            totalResults++;
                        }
                        long fetchEndTime = System.currentTimeMillis();
                        long fetchTime = fetchEndTime - fetchStartTime;

                        long totalTime = executionTime + fetchTime;

                        System.out.println(index + "," + executionTime + "," + fetchTime + "," + totalTime + "," + totalResults);
                        writer.write(index + "," + executionTime + "," + fetchTime + "," + totalTime + "," + totalResults + "\n");
                        transaction.commit();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (session != null) {
                    session.close();
                }
                if (driver != null) {
                    driver.close();
                }
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void executeCypherQueriesWithAverages(List<String[]> indexAndQuery, String dbForSession, int repetitions) {
        indexAndQuery.remove(0);
        FileWriter writer = null;
        try {
            writer = new FileWriter(ConfigManager.getProperty("output_file_path") + dbForSession + "_results.csv");
            writer.write("Index,executionTime,fetchTime,totalTime,numberOfResults\n");
            System.out.println("Index,executionTime,fetchTime,totalTime,numberOfResults");

            Session session = null;
            try {
                session = driver.session(SessionConfig.forDatabase(dbForSession));
                // At this point, the connection should be established

                for (String[] array : indexAndQuery) {
                    String index = array[0];
                    String query = array[1];

                    long totalExecutionTime = 0;
                    long totalFetchTime = 0;
                    int totalResults = 0;

                    for (int i = 0; i < repetitions; i++) {
                        try (Transaction transaction = session.beginTransaction()) {
                            long startTime = System.currentTimeMillis();
                            Result result = transaction.run(query);
                            long endTime = System.currentTimeMillis();
                            long executionTime = endTime - startTime;

                            long fetchStartTime = System.currentTimeMillis();
                            int resultCount = 0;

                            while (result.hasNext()) {
                                result.next();
                                resultCount++;
                            }

                            long fetchEndTime = System.currentTimeMillis();
                            long fetchTime = fetchEndTime - fetchStartTime;

                            totalExecutionTime += executionTime;
                            totalFetchTime += fetchTime;
                            totalResults = resultCount;

                            transaction.commit();
                        }
                        /*try (Transaction t = session.beginTransaction()) {
                            t.run("CALL db.clearQueryCaches();");
                            t.commit();
                        }*/
                    }

                    // Calculate averages for execution time and fetch time
                    long averageExecutionTime = totalExecutionTime / repetitions;
                    long averageFetchTime = totalFetchTime / repetitions;

                    System.out.println(index + "," + averageExecutionTime + "," + averageFetchTime + "," + (averageExecutionTime + averageFetchTime) + "," + totalResults);
                    writer.write(index + "," + averageExecutionTime + "," + averageFetchTime + "," + (averageExecutionTime + averageFetchTime) + "," + totalResults + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Handle the exception (e.g., log an error or exit the application)
            } finally {
                if (session != null) {
                    session.close();
                }
                if (driver != null) {
                    driver.close();
                }
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    public void close() {
        driver.close();
    }
}



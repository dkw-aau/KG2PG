package cs.utils.graphdb;

import cs.utils.ConfigManager;
import cs.utils.FilesUtil;
import cs.utils.Utils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class queries a WikiData graphdb endpoint and executes a list of queries available in the resources directory
 */
public class S3PGBenchKG {
    private final GraphDBUtils graphDBUtils;

    public S3PGBenchKG() {
        graphDBUtils = new GraphDBUtils();
    }

    public void executeQueries() {
        //readQueriesFromFile(ConfigManager.getProperty("resources_path") + "/kg_queries.csv");
        readQueriesFromFileWithAverage(ConfigManager.getProperty("resources_path") + "/kg_queries.csv", 10);
    }

    private void readQueriesFromFile(String fileAddress) {
        List<String[]> indexAndQuery = FilesUtil.readCsvAllDataOnceWithPipeSeparator(fileAddress);
        FileWriter writer = null;
        try {
            writer = new FileWriter(ConfigManager.getProperty("output_file_path") + "_kg_results.csv");
            writer.write("Index,NumberOfResults,ExecutionTimeMilliSeconds,ExecutionTimeSeconds,ExecutionTimeMinutes\n");
            indexAndQuery.remove(0);
            for (String[] array : indexAndQuery) {
                String index = array[0];
                String query = array[1];
                //?entity ?propValue
                StopWatch watch = new StopWatch();
                watch.start();
                int numberOfRows = graphDBUtils.runSelectQueryCountOutputRows(query);
                watch.stop();
                System.out.println(index + "," + query + "," + numberOfRows + "," + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()) + "," + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
                writer.write(index + "," + numberOfRows + "," + TimeUnit.MILLISECONDS + "," + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()) + "," + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()) + "\n");
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void readQueriesFromFileWithAverage(String fileAddress, int repetitions) {
        List<String[]> indexAndQuery = FilesUtil.readCsvAllDataOnceWithPipeSeparator(fileAddress);
        FileWriter writer = null;
        try {
            writer = new FileWriter(ConfigManager.getProperty("output_file_path") + "_kg_results.csv");
            writer.write("Index,NumberOfResults,ExecutionTimeMilliSeconds,ExecutionTimeSeconds,ExecutionTimeMinutes\n");
            indexAndQuery.remove(0);
            for (String[] array : indexAndQuery) {
                String index = array[0];
                String query = array[1];

                long totalExecutionTimeMillis = 0;
                int totalResults = 0;

                for (int i = 0; i < repetitions; i++) {
                    // Start the stopwatch
                    StopWatch watch = new StopWatch();
                    watch.start();

                    int numberOfRows = graphDBUtils.runSelectQueryCountOutputRows(query);

                    // Stop the stopwatch
                    watch.stop();

                    totalExecutionTimeMillis += watch.getTime();
                    totalResults += numberOfRows;
                }

                long averageExecutionTimeMillis = totalExecutionTimeMillis / repetitions;
                long averageExecutionTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(averageExecutionTimeMillis);
                long averageExecutionTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(averageExecutionTimeMillis);

                System.out.println(index + "," + query + "," + totalResults + "," + averageExecutionTimeMillis + "," + averageExecutionTimeSeconds + "," + averageExecutionTimeMinutes);
                writer.write(index + "," + totalResults + "," + averageExecutionTimeMillis + "," + averageExecutionTimeSeconds + "," + averageExecutionTimeMinutes + "\n");
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

package cs.utils.graphdb;

import cs.utils.*;


import org.apache.commons.lang3.time.StopWatch;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class queries a WikiData graphdb endpoint and executes a list of queries available in the resources directory
 */
public class WDBenchGraphDB {
    private final GraphDBUtils graphDBUtils;

    public WDBenchGraphDB() {
        graphDBUtils = new GraphDBUtils();
    }

    public void executeQueries() {
        System.out.println("execute multiple_bgps");
        readQueriesFromFile(ConfigManager.getProperty("resources_path") + "/wikidata_queries/multiple_bgps.csv", "multiple_bgps");

        System.out.println("execute single_bgps");
        readQueriesFromFile(ConfigManager.getProperty("resources_path") + "/wikidata_queries/single_bgps.csv", "single_bgps");

        System.out.println("execute opts");
        readQueriesFromFile(ConfigManager.getProperty("resources_path") + "/wikidata_queries/opts.csv", "opts");

        System.out.println("execute paths");
        readQueriesFromFile(ConfigManager.getProperty("resources_path") + "/wikidata_queries/paths.csv", "paths");
    }

    private void readQueriesFromFile(String fileAddress, String type) {
        List<String[]> indexAndQuery = FilesUtil.readCsvAllDataOnce(fileAddress);
        for (String[] array : indexAndQuery) {
            String index = array[0];
            String query = "SELECT * WHERE { " + array[1] + " } LIMIT 100000";

            StopWatch watch = new StopWatch();
            watch.start();

            int numberOfRows = graphDBUtils.runSelectQueryCountOutputRows(query);
            watch.stop();

            System.out.println(type + "," + index + "," + query + "," + numberOfRows + "," + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()) + "," + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
            Utils.logQueryingStats(type + "," + index + "," + query + "," + numberOfRows, TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
        }
    }
}

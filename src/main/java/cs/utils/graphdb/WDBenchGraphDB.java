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
        readQueriesFromFile(getResourceFilePath("wikidata_queries/multiple_bgps.csv"), "multiple_bgps");

        System.out.println("execute single_bgps");
        readQueriesFromFile(getResourceFilePath("wikidata_queries/single_bgps.csv"), "single_bgps");

        System.out.println("execute opts");
        readQueriesFromFile(getResourceFilePath("wikidata_queries/opts.csv"), "opts");

        System.out.println("execute paths");
        readQueriesFromFile(getResourceFilePath("wikidata_queries/paths.csv"), "paths");
    }
    
    private String getResourceFilePath(String fileName) {
        // Try to get from resources_path config first (for backward compatibility)
        String resourcesPath = ConfigManager.getProperty("resources_path");
        if (resourcesPath != null) {
            return resourcesPath + "/" + fileName;
        }
        
        // Fall back to relative path or embedded resource
        return ConfigManager.getResourcePath(fileName);
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

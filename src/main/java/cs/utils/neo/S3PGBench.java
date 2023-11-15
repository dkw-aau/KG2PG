package cs.utils.neo;


import cs.Main;
import cs.utils.ConfigManager;
import cs.utils.FilesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class S3PGBench {
    String user = "neo4j";
    String password = "12345678";

    public S3PGBench() {
    }

    public void benchS3pgQueries() {
        Main.logger.info("---- benchS3pgQueries ---- ");
        String filePath = ConfigManager.getProperty("resources_path") + "/s3pg_queries.csv";
        List<String[]> indexAndQuery = FilesUtil.readCsvAllDataOnceWithPipeSeparator(filePath);
        String dbForSession = "dbp22s3pg";
        executeQueries(indexAndQuery, getNeo4jDbUrlForS3pg(), dbForSession);
    }

    public void benchRdf2pgQueries() {
        Main.logger.info("---- benchRdf2pgQueries ---- ");
        String filePath = ConfigManager.getProperty("resources_path") + "/rdf2pg_queries.csv";
        List<String[]> indexAndQuery = FilesUtil.readCsvAllDataOnceWithPipeSeparator(filePath);
        String dbForSession = "rdf2pgdbpedia2022";
        executeQueries(indexAndQuery, getNeo4jDbUrlForRdf2pg(), dbForSession);
    }

    public void benchNeoSemQueries() {
        Main.logger.info("---- benchNeoSemQueries ---- ");
        String filePath = ConfigManager.getProperty("resources_path") + "/ns_queries.csv";
        List<String[]> indexAndQuery = FilesUtil.readCsvAllDataOnceWithPipeSeparator(filePath);
        String dbForSession = "dbpedia2022neo2";
        executeQueries(indexAndQuery, getNeo4jDbUrlForNeoSem(), dbForSession);
    }

    public void executeQueries(List<String[]> indexAndQuery, String uri, String dbForSession) {
        Neo4jConnector connector = new Neo4jConnector(uri, user, password);
        connector.executeCypherQueriesWithAverages(indexAndQuery, dbForSession, 10);
        connector.close();
    }

    @NotNull
    private static String getNeo4jDbUrlForS3pg() {
        //http://a256-gc1-17.srv.aau.dk:7574/browser/
        String host = "a256-gc1-17.srv.aau.dk";
        int port = 7688;
        String database = "dbp22s3pg";
        return "bolt://" + host + ":" + port + "/" + database;
    }

    @NotNull
    private static String getNeo4jDbUrlForRdf2pg() {
        //http://a256-gc1-17.srv.aau.dk:7674/browser/
        String host = "a256-gc1-17.srv.aau.dk";
        int port = 7689;
        String database = "rdf2pgdbpedia2022";
        return "bolt://" + host + ":" + port + "/" + database;
    }

    @NotNull
    private static String getNeo4jDbUrlForNeoSem() {
        //http://a256-gc1-17.srv.aau.dk:7874/browser/
        String host = "a256-gc1-17.srv.aau.dk";
        int port = 7691;
        String database = "dbpedia2022neo2";
        return "bolt://" + host + ":" + port + "/" + database;
    }
}

package cs;

import cs.commons.ResourceEncoder;
import cs.graphTranslation.pm.DataTransFileToCsv;
import cs.graphTranslation.npm.DataTransFileToCsvNpm;
import cs.graphTranslation.npm.DataTransUpdatesNpm;
import cs.schemaTranslation.SchemaTranslator;
import cs.utils.ConfigManager;
import cs.utils.Constants;
import cs.utils.FilesUtil;
import cs.utils.graphdb.S3PGBenchKG;
import cs.utils.neo.S3PGBench;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class Main {
    public static String configPath;
    public static String datasetPath;
    public static String datasetName;
    public static String outputFilePath;
    public static int numberOfClasses;
    public static int numberOfInstances;
    public static boolean isWikiData;
    public static Logger logger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    private static final ResourceEncoder resourceEncoder = new ResourceEncoder();
    private static final boolean isParsimonious = false;

    public static void main(String[] args) {
        configPath = args[0];
        logger.setLevel(Level.INFO);
        readConfig();
        //runS3pg();
        runS3pgMonotone();
    }

    private static void runS3pg() {
        SchemaTranslator s3pgSchema = s3pgSchemaTransformation();
        if (isParsimonious) {
            s3pgParsimoniousGraphTransformation(s3pgSchema);
        } else {
            s3pgNonParsimoniousGraphTransformation(s3pgSchema);
        }
    }

    private static SchemaTranslator s3pgSchemaTransformation() {
        return new SchemaTranslator(resourceEncoder);
    }

    private static void s3pgParsimoniousGraphTransformation(SchemaTranslator schemaTranslator) {
        DataTransFileToCsv dtFb = new DataTransFileToCsv(datasetPath, numberOfClasses, numberOfInstances, Constants.RDF_TYPE, resourceEncoder, schemaTranslator);
        dtFb.run();
    }

    private static void s3pgNonParsimoniousGraphTransformation(SchemaTranslator schemaTranslator) {
        DataTransFileToCsvNpm dtNpm = new DataTransFileToCsvNpm(datasetPath, numberOfClasses, numberOfInstances, Constants.RDF_TYPE, resourceEncoder, schemaTranslator);
        dtNpm.run();
    }

    private static void runS3pgMonotone() {
        DataTransUpdatesNpm dtNpm = new DataTransUpdatesNpm();
        dtNpm.run();
    }

    private static void runQueryBenchmark() {
        //Benchmark Query Runtime Analysis on KG over GraphDB
        new S3PGBenchKG().executeQueries();

        //Benchmark Query Runtime Analysis on PGs transformed by different approaches
        S3PGBench s3PGBench = new S3PGBench();
        s3PGBench.benchNeoSemQueries();
        s3PGBench.benchS3pgQueries();
        s3PGBench.benchRdf2pgQueries();
    }

    private static void readConfig() {
        datasetPath = paramVal("dataset_path");
        datasetName = FilesUtil.getFileName(datasetPath);
        numberOfClasses = Integer.parseInt(paramVal("expected_number_classes")); // expected or estimated numberOfClasses
        numberOfInstances = Integer.parseInt(paramVal("expected_number_of_lines")) / 2; // expected or estimated numberOfInstances
        isWikiData = isActivated("is_wikidata");
        outputFilePath = paramVal("output_file_path");
    }

    private static boolean isActivated(String option) {
        return Boolean.parseBoolean(ConfigManager.getProperty(option));
    }

    private static String paramVal(String prop) {
        return ConfigManager.getProperty(prop);
    }
}
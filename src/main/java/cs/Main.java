package cs;

import cs.commons.ResourceEncoder;
import cs.graphTranslation.DataTransFileToCsv;
import cs.schemaTranslation.SchemaTranslator;
import cs.utils.ConfigManager;
import cs.utils.Constants;
import cs.utils.FilesUtil;
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

    public static void main(String[] args) {
        configPath = args[0];
        logger.setLevel(Level.INFO);
        readConfig();

        ResourceEncoder resourceEncoder = new ResourceEncoder();

        SchemaTranslator schemaTranslator = new SchemaTranslator(resourceEncoder);

        //Graph Data Translation
        DataTransFileToCsv dtFb = new DataTransFileToCsv(datasetPath, numberOfClasses, numberOfInstances, Constants.RDF_TYPE, resourceEncoder, schemaTranslator);
        dtFb.run();
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
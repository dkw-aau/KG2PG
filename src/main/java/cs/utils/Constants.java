package cs.utils;

/**
 * This class contains all the constants used globally throughout the project
 */
public class Constants {
    public static String SHAPES_NAMESPACE = "http://shaclshapes.org/";
    public static String MEMBERSHIP_GRAPH_ROOT_NODE = "<http://www.schema.hng.root> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.schema.hng.root#HNG_Root> .";
    public static String RDF_TYPE = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
    public static String RDF_TYPE_PROPERTY = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    public static String INSTANCE_OF = "<http://www.wikidata.org/prop/direct/P31>";
    public static String INSTANCE_OF_WD = "http://www.wikidata.org/prop/direct/P31";
    public static String SUB_CLASS_OF = "<http://www.w3.org/2000/01/rdf-schema#subClassOf>";
    
    // some constant addresses of files
    public static String TEMP_DATASET_FILE = ConfigManager.getProperty("output_file_path") + "/" + ConfigManager.getProperty("dataset_name") + "-shape-props-stats.csv";
    public static String TEMP_DATASET_FILE_2 = ConfigManager.getProperty("output_file_path") + "/" + "shape-props-extended-stats.csv";
    
    public static String RUNTIME_LOGS = ConfigManager.getProperty("output_file_path") + ConfigManager.getProperty("dataset_name") + "_RUNTIME_LOGS.csv";
    public static String SAMPLING_LOGS = ConfigManager.getProperty("output_file_path") + ConfigManager.getProperty("dataset_name") + "_SAMPLING_LOGS.csv";
    
    public static String QUERYING_LOGS = ConfigManager.getProperty("output_file_path") + ConfigManager.getProperty("dataset_name") + "_QUERYING_LOGS.csv";
    
}

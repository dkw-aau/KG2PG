package cs.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import cs.Main;

/**
 * This class is used to configure the input params provided in the config file
 */
public class ConfigManager {
    private static final String DEFAULT_CONFIG_PATH = "config/config.properties";
    private static final String EXTERNAL_CONFIG_PATH = "config.properties";
    private static Properties config;
    
    public static String getProperty(String property) {
        if (config == null) {
            config = loadConfiguration();
        }
        return config.getProperty(property);
    }
    
    private static Properties loadConfiguration() {
        Properties props = new Properties();
        
        // If Main.configPath is set (from command line args), use it
        if (Main.configPath != null) {
            try (FileInputStream configFile = new FileInputStream(Main.configPath)) {
                props.load(configFile);
                System.out.println("Loaded config from: " + Main.configPath);
                return props;
            } catch (IOException ex) {
                System.err.println("Failed to load config from " + Main.configPath + ": " + ex.getMessage());
            }
        }
        
        // Try external config first (allows user customization)
        File externalConfig = new File(EXTERNAL_CONFIG_PATH);
        if (externalConfig.exists()) {
            try (FileInputStream fis = new FileInputStream(externalConfig)) {
                props.load(fis);
                System.out.println("Loaded external config.properties");
                return props;
            } catch (IOException e) {
                System.err.println("Failed to load external config: " + e.getMessage());
            }
        }
        
        // Fall back to embedded config (both from root and config/ directory)
        InputStream configStream = null;
        try {
            // Try config.properties from classpath root first
            configStream = ConfigManager.class.getClassLoader().getResourceAsStream("config.properties");
            if (configStream == null) {
                // Try config/config.properties
                configStream = ConfigManager.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_PATH);
            }
            
            if (configStream != null) {
                props.load(configStream);
                System.out.println("Loaded embedded config.properties");
            } else {
                throw new RuntimeException("config.properties not found in JAR");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config: " + e.getMessage());
        } finally {
            if (configStream != null) {
                try {
                    configStream.close();
                } catch (IOException e) {
                    // Ignore close exception
                }
            }
        }
        
        return props;
    }
    
    public static String getResourcePath(String resourceName) {
        // Check if resource exists as external file first
        Path externalPath = Paths.get(resourceName);
        if (Files.exists(externalPath)) {
            return externalPath.toString();
        }
        
        // Fall back to embedded resource
        return getEmbeddedResourcePath(resourceName);
    }
    
    public static InputStream getResourceStream(String resourceName) throws IOException {
        // Try external file first
        Path externalPath = Paths.get(resourceName);
        if (Files.exists(externalPath)) {
            return Files.newInputStream(externalPath);
        }
        
        // Fall back to embedded resource
        InputStream is = ConfigManager.class.getClassLoader().getResourceAsStream(resourceName);
        if (is == null) {
            throw new FileNotFoundException("Resource not found: " + resourceName);
        }
        return is;
    }
    
    /**
     * Get a file path that works for both external files and embedded resources
     * For embedded resources, this may extract them to a temporary location
     */
    public static String getDataPath(String dataPath) {
        // Check if external file exists first
        Path externalPath = Paths.get(dataPath);
        if (Files.exists(externalPath)) {
            return externalPath.toString();
        }
        
        // For embedded resources, try to use them via InputStream
        // If a physical file path is absolutely required, extract to temp file
        InputStream resourceStream = ConfigManager.class.getClassLoader().getResourceAsStream(dataPath);
        if (resourceStream != null) {
            try {
                resourceStream.close();
                return extractResourceToTemp(dataPath);
            } catch (IOException e) {
                System.err.println("Failed to handle embedded resource: " + dataPath);
            }
        }
        
        // Return original path as fallback
        return dataPath;
    }
    
    /**
     * Extract an embedded resource to a temporary file and return its path
     */
    private static String extractResourceToTemp(String resourcePath) throws IOException {
        InputStream resourceStream = ConfigManager.class.getClassLoader().getResourceAsStream(resourcePath);
        if (resourceStream == null) {
            throw new FileNotFoundException("Resource not found: " + resourcePath);
        }
        
        // Create temp file with original extension
        String fileName = Paths.get(resourcePath).getFileName().toString();
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = fileName.substring(dotIndex);
            fileName = fileName.substring(0, dotIndex);
        }
        
        Path tempFile = Files.createTempFile(fileName, extension);
        tempFile.toFile().deleteOnExit(); // Clean up on JVM exit
        
        try (InputStream is = resourceStream) {
            Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        
        return tempFile.toString();
    }
    
    private static String getEmbeddedResourcePath(String resourceName) {
        // For embedded resources, we'll need to extract them temporarily or use streams
        // This method would handle extracting embedded resources to temp files if needed
        return resourceName; // Simplified - adjust based on your needs
    }
    
    public static void ensureDirectoryExists(String path) {
        try {
            Path dir = Paths.get(path);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                System.out.println("Created directory: " + path);
            }
        } catch (IOException e) {
            System.err.println("Failed to create directory " + path + ": " + e.getMessage());
        }
    }
    
    /**
     * Create a timestamped output directory for each run
     * Format: output/datasetName_YYYY-MM-DD_HH-MM-SS_timestamp/
     */
    public static String createTimestampedOutputDirectory(String baseOutputPath, String datasetName) {
        try {
            // Get current timestamp
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formatter = 
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String readableTimestamp = now.format(formatter);
            long unixTimestamp = System.currentTimeMillis();
            
            // Create directory name: datasetName_readable-timestamp_unix-timestamp
            String dirName = String.format("%s_%s_%d", 
                datasetName != null ? datasetName : "run", 
                readableTimestamp, 
                unixTimestamp);
            
            // Ensure base output path ends with separator
            if (!baseOutputPath.endsWith("/") && !baseOutputPath.endsWith("\\")) {
                baseOutputPath += "/";
            }
            
            // Create full output path
            String fullOutputPath = baseOutputPath + dirName + "/";
            
            // Create the directory
            ensureDirectoryExists(fullOutputPath);
            
            System.out.println("Created timestamped output directory: " + fullOutputPath);
            return fullOutputPath;
            
        } catch (Exception e) {
            System.err.println("Failed to create timestamped directory, using base path: " + e.getMessage());
            ensureDirectoryExists(baseOutputPath);
            return baseOutputPath;
        }
    }
}
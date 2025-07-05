package cs.utils;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains various methods used as a utility in the project to interact with files
 */
public class FilesUtil {
    public static void writeToFile(String str, String fileNameAndPath) {
        try {
            FileWriter fileWriter = new FileWriter(new File(fileNameAndPath));
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(str);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFileInAppendMode(String str, String fileNameAndPath) {
        try {
            FileWriter fileWriter = new FileWriter(new File(fileNameAndPath), true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(str);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String[]> readCsvAllDataOnceWithPipeSeparator(String fileAddress) {
        List<String[]> allData = null;
        try {
            FileReader filereader = new FileReader(fileAddress);
            // create csvParser object with
            // custom separator pipe
            CSVParser parser = new CSVParserBuilder().withSeparator('|').build();

            // create csvReader object with
            // parameter file reader and parser
            CSVReader csvReader = new CSVReaderBuilder(filereader).withCSVParser(parser).build();

            // Read all data at once
            allData = csvReader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allData;
    }

    public static List<String[]> readCsvAllDataOnce(String fileAddress) {
        List<String[]> allData = null;
        try {
            FileReader filereader = new FileReader(fileAddress);
            // create csvParser object with
            // custom separator pipe
            CSVParser parser = new CSVParserBuilder().withSeparator(',').build();

            // create csvReader object with
            // parameter file reader and parser
            CSVReader csvReader = new CSVReaderBuilder(filereader).withCSVParser(parser).build();

            // Read all data at once
            allData = csvReader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allData;
    }

    public static boolean deleteFile(String fileAddress) {
        File file = new File(fileAddress);
        return file.delete();
    }

    public static String readQuery(String query) {
        String q = null;
        try {
            // Try to read from resources using the new ConfigManager method
            try (InputStream is = ConfigManager.getResourceStream("queries/" + query + ".txt")) {
                q = new String(is.readAllBytes());
            } catch (IOException e) {
                // Fallback to old method if resources_path is configured
                String resourcesPath = ConfigManager.getProperty("resources_path");
                if (resourcesPath != null) {
                    String queriesDirectory = resourcesPath + "/queries/";
                    q = new String(Files.readAllBytes(Paths.get(queriesDirectory + query + ".txt")));
                } else {
                    throw e;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return q;
    }

    public static String readSHACLQuery(String query) {
        String q = null;
        try {
            // Try to read from resources using the new ConfigManager method
            try (InputStream is = ConfigManager.getResourceStream("shacl_queries/" + query + ".txt")) {
                q = new String(is.readAllBytes());
            } catch (IOException e) {
                // Fallback to old method if resources_path is configured
                String resourcesPath = ConfigManager.getProperty("resources_path");
                if (resourcesPath != null) {
                    String queriesDirectory = resourcesPath + "/shacl_queries/";
                    q = new String(Files.readAllBytes(Paths.get(queriesDirectory + query + ".txt")));
                } else {
                    throw e;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return q;
    }


    public static String readShaclStatsQuery(String query, String type) {
        String q = null;
        try {
            // Try to read from resources using the new ConfigManager method
            try (InputStream is = ConfigManager.getResourceStream("shacl_stats_queries/" + type + "/" + query + ".txt")) {
                q = new String(is.readAllBytes());
            } catch (IOException e) {
                // Fallback to old method if resources_path is configured
                String resourcesPath = ConfigManager.getProperty("resources_path");
                if (resourcesPath != null) {
                    String queriesDirectory = resourcesPath + "/shacl_stats_queries/" + type + "/";
                    q = new String(Files.readAllBytes(Paths.get(queriesDirectory + query + ".txt")));
                } else {
                    throw e;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return q;
    }

    public static String getFileName(String path) {
        File file = new File(path);
        return FilenameUtils.removeExtension(file.getName());
    }

    public static void writeStringToStringMapToFile(Map<String, String> namespaceMap, String fileName) {
        try (FileWriter fileWriter = new FileWriter(fileName);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT)) {

            // Write CSV headers (optional)
            csvPrinter.printRecord("NAMESPACE", "PREFIX");

            // Write the namespaceMap to the CSV file
            for (Map.Entry<String, String> entry : namespaceMap.entrySet()) {
                csvPrinter.printRecord(entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> readCsvToMap(String csvFilePath) {
        Map<String, String> prefixMap = new HashMap<>();

        try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(csvFilePath)).withSkipLines(1).build()) {
            String[] line;
            while (true) {
                try {
                    if ((line = csvReader.readNext()) == null) break;
                    if (line.length >= 2) {
                        String key = line[0];
                        String value = line[1];
                        prefixMap.put(key, value);
                    }
                } catch (CsvValidationException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return prefixMap;
    }

}

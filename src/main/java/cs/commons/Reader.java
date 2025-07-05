package cs.commons;

import cs.utils.ConfigManager;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.InputStream;

public class Reader {
    /**
     * @param SHACLFilePath provide the path
     * @param format        use value like "TURTLE"
     * @return jena model
     */
    public static Model readFileToModel(String SHACLFilePath, String format) {
        Model model = ModelFactory.createDefaultModel();
        try {
            // Use ConfigManager to handle both external and embedded resources
            try (InputStream inputStream = ConfigManager.getResourceStream(SHACLFilePath)) {
                model.read(inputStream, null, format);
            }
        } catch (Exception e) {
            System.err.println("Failed to read file: " + SHACLFilePath);
            e.printStackTrace();
        }
        return model;
    }
}

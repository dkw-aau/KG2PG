package cs.commons;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.FileInputStream;

public class Reader {
    /**
     * @param SHACLFilePath provide the path
     * @param format        use value like "TURTLE"
     * @return jena model
     */
    public static Model readFileToModel(String SHACLFilePath, String format) {
        Model model = ModelFactory.createDefaultModel();
        try {
            FileInputStream fileInputStream = new FileInputStream(SHACLFilePath);
            model.read(fileInputStream, null, format);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }
}

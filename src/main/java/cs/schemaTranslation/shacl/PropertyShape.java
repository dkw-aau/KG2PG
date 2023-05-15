package cs.schemaTranslation.shacl;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class PropertyShape {
    private Property path;
    private int minCount;
    private int maxCount;

    public PropertyShape(String pathUri, int minCount, int maxCount) {
        this.path = ResourceFactory.createProperty(pathUri);
        this.minCount = minCount;
        this.maxCount = maxCount;
    }

    // Getters and setters

    public Property getPath() {
        return path;
    }

    public int getMinCount() {
        return minCount;
    }

    public int getMaxCount() {
        return maxCount;
    }
}

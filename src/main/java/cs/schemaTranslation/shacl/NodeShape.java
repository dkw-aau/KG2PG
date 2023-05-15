package cs.schemaTranslation.shacl;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.ArrayList;
import java.util.List;

public class NodeShape {
    private String id;
    private Resource targetClass;
    private List<PropertyShape> propertyShapes;

    public NodeShape(String id, String targetClass) {
        this.id = id;
        this.targetClass = ResourceFactory.createResource(targetClass);
        this.propertyShapes = new ArrayList<>();
    }

    public void addPropertyShape(PropertyShape propertyShape) {
        propertyShapes.add(propertyShape);
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public Resource getTargetClass() {
        return targetClass;
    }

    public List<PropertyShape> getPropertyShapes() {
        return propertyShapes;
    }
}
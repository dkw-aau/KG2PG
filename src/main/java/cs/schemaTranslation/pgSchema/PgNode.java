package cs.schemaTranslation.pgSchema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PgNode {
    private final Integer id;
    private String nodeShapeIri;

    private static Map<Integer, PgNode> nodeMap;
    private static List<Integer> propIDs;

    String type;

    public PgNode(Integer id) {
        this.id = id;
        nodeMap = new HashMap<>();
        nodeMap.put(id, this); // Add the current node to the nodeMap
    }

    public Integer getId() {
        return id;
    }

    public String getNodeShapeIri() {
        return nodeShapeIri;
    }

    public void setNodeShapeIri(String nodeShapeIri) {
        this.nodeShapeIri = nodeShapeIri;
    }

    public static PgNode getNodeById(Integer id) {
        return nodeMap.get(id);
    }


    public void addPropId(int id) {
        if (propIDs == null) {
            propIDs = new ArrayList<>();
        }
        propIDs.add(id);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PgNode pgNode = (PgNode) o;
        return id.equals(pgNode.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

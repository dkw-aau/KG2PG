package cs.schemaTranslation.pgSchema;

import java.util.HashMap;
import java.util.Map;

public class PgNode {
    private final Integer id;
    private String nodeShapeIri;

    private static Map<Integer, PgNode> nodeMap;

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

package cs.schemaTranslation.pgSchema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PgNode {
    private Integer id; // Node ID
    private String nodeShapeIri; // IRI of the SHACL shape that corresponds to this node


    private static Map<Integer, PgNode> nodeMap; // Map of node IDs to nodes
    Boolean isAbstract = false; // Is this node abstract?

    public PgNode(Integer id) {
        this.id = id;
        getNodeMap().put(id, this); // Add the current node to the nodeMap
    }

    /**
     * Lazy initialization of nodeMap
     * By using lazy initialization, the nodeMap will be created only when it is accessed for the first time. Subsequent calls to getNodeMap() will return the existing nodeMap, ensuring that all PgNode instances are added to the same map.
     */
    private static Map<Integer, PgNode> getNodeMap() {
        if (nodeMap == null) {
            nodeMap = new HashMap<>();
        }
        return nodeMap;
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
        return getNodeMap().get(id);
    }

    public Boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(Boolean anAbstract) {
        isAbstract = anAbstract;
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

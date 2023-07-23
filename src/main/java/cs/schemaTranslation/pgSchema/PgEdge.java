package cs.schemaTranslation.pgSchema;

import java.util.HashMap;
import java.util.Map;

public class PgEdge {
    private final Integer id;
    Integer minCount;
    Integer maxCount;
    String dataType;
    Boolean isLiteral = false;
    Boolean isProperty = false;
    private static Map<Integer, PgEdge> edgeMap = new HashMap<>();

    public PgEdge(Integer id) {
        this.id = id;
        edgeMap.put(id, this); // Add the current edge to the edgeMap
    }

    public Boolean isLiteral() {
        return isLiteral;
    }

    public void setLiteral(Boolean literal) {
        isLiteral = literal;
    }

    public static PgEdge getEdgeById(Integer edgeId) {
        return edgeMap.get(edgeId);
    }

    public Integer getId() {
        return id;
    }

    public Integer getMinCount() {
        return minCount;
    }

    public void setMinCount(Integer minCount) {
        this.minCount = minCount;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public Boolean isProperty() {
        return isProperty;
    }

    public void setProperty(Boolean property) {
        isProperty = property;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    //A method to check if minCount and maxCount are 1 then set isProperty to true
    public boolean handlePropertyType() {
        if (this.minCount == 1 && this.maxCount == 1) {
            this.isProperty = true;
            return true;
        } else {
            return false;
        }
    }
}
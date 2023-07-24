package cs.schemaTranslation.pgSchema;

import java.util.HashMap;
import java.util.Map;

public class PgEdge {
    private final Integer id;
    String dataType;
    Boolean isLiteral = false; // When property is single type data type constraint then isLiteral = true
    //Boolean isProperty = false; // When MinCount = MixCount = 1, then isProperty = true     //FIXME: remove it not required
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

    /*public Boolean isProperty() {
        return isProperty;
    }*/

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

  /*  public void setIsProperty(boolean status) {
        isProperty = status;
    }*/
}
package cs.schemaTranslation.pgSchema;

public class PgEdge {
    Integer id;
    Integer minCount;
    Integer maxCount;


    String dataType;
    Boolean isProperty = false;

    public PgEdge(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Boolean getProperty() {
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
    public void handlePropertyType() {
        if (this.minCount == 1 && this.maxCount == 1) {
            this.isProperty = true;
        }
    }
}
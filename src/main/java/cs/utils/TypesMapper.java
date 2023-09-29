package cs.utils;

import java.util.HashMap;

// This class maps RDF literal types to Postgres and Cypher data types
public class TypesMapper {
    private HashMap<String, String> map;

    public TypesMapper() {
        map = new HashMap<>();
    }

    public HashMap<String, String> getMap() {
        return map;
    }

    public void setMap(HashMap<String, String> map) {
        this.map = map;
    }

    public void initTypesForCypher() {
        rdfToCypherTypesMap();
    }

    public void rdfToCypherTypesMap() {
        //look at the table https://www.w3.org/TR/rdf11-concepts/
        map.put("IRI", "STRING");
        map.put("http://www.w3.org/2001/XMLSchema#string", "STRING");
        map.put("http://www.w3.org/2001/XMLSchema#boolean", "BOOLEAN");
        map.put("http://www.w3.org/2001/XMLSchema#decimal", "FLOAT");
        map.put("http://www.w3.org/2001/XMLSchema#integer", "INTEGER");

        map.put("http://www.w3.org/2001/XMLSchema#double", "FLOAT");
        map.put("http://www.w3.org/2001/XMLSchema#float", "FLOAT");

        map.put("http://www.w3.org/2001/XMLSchema#date", "DATE");
        map.put("http://www.w3.org/2001/XMLSchema#time", "Time");
        map.put("http://www.w3.org/2001/XMLSchema#dateTime", "DateTime");

        map.put("http://www.w3.org/2001/XMLSchema#gYear", "INT");
        map.put("http://www.w3.org/2001/XMLSchema#gMonth", "INT");
        map.put("http://www.w3.org/2001/XMLSchema#gDay", "INT");
        map.put("http://www.w3.org/2001/XMLSchema#gYearMonth", "STRING");
        map.put("http://www.w3.org/2001/XMLSchema#gMonthDay", "STRING");
        map.put("http://www.w3.org/2001/XMLSchema#duration", "DURATION");
        map.put("http://www.w3.org/2001/XMLSchema#yearMonthDuration", "DURATION");
        map.put("http://www.w3.org/2001/XMLSchema#dayTimeDuration", "DURATION");

        map.put("http://www.w3.org/2001/XMLSchema#byte", "STRING");
        map.put("http://www.w3.org/2001/XMLSchema#short", "STRING");
        map.put("http://www.w3.org/2001/XMLSchema#int", "INTEGER");
        map.put("http://www.w3.org/2001/XMLSchema#long", "FLOAT");
        map.put("http://www.w3.org/2001/XMLSchema#unsignedbyte", "STRING");
        map.put("http://www.w3.org/2001/XMLSchema#unsignedshort", "STRING");
        map.put("http://www.w3.org/2001/XMLSchema#unsignedint", "INTEGER");
        map.put("http://www.w3.org/2001/XMLSchema#unsignedlong", "FLOAT");


        map.put("http://www.w3.org/2001/XMLSchema#positiveInteger", "INTEGER");
        map.put("http://www.w3.org/2001/XMLSchema#nonNegativeInteger", "INTEGER");
        map.put("http://www.w3.org/2001/XMLSchema#negativeInteger", "INTEGER");
        map.put("http://www.w3.org/2001/XMLSchema#nonPositiveInteger", "INTEGER");

        map.put("http://www.w3.org/2001/XMLSchema#hexBinary", "STRING");
        map.put("http://www.w3.org/2001/XMLSchema#base64Binary", "STRING");

        map.put("http://www.w3.org/2001/XMLSchema#anyURI", "STRING");
        map.put("http://www.w3.org/2001/XMLSchema#language", "STRING");
        map.put("http://www.w3.org/2001/XMLSchema#normalizedString", "STRING");
        map.put("http://www.w3.org/2001/XMLSchema#token", "STRING");
        map.put("http://www.w3.org/2001/XMLSchema#NMTOKEN", "STRING");
        map.put("http://www.w3.org/2001/XMLSchema#Name", "STRING");
        map.put("http://www.w3.org/2001/XMLSchema#NCName", "STRING");
    }

    public void rdfToPostgresTypesMap() {
        //look at the table https://www.w3.org/TR/rdf11-concepts/
        map.put("http://www.w3.org/2001/XMLSchema#string", "TEXT");
        map.put("http://www.w3.org/2001/XMLSchema#boolean", "boolean");
        map.put("http://www.w3.org/2001/XMLSchema#decimal", "decimal");
        map.put("http://www.w3.org/2001/XMLSchema#integer", "INT");

        map.put("http://www.w3.org/2001/XMLSchema#double", "numeric");
        map.put("http://www.w3.org/2001/XMLSchema#float", "numeric");

        map.put("http://www.w3.org/2001/XMLSchema#date", "date");
        map.put("http://www.w3.org/2001/XMLSchema#time", "time");
        map.put("http://www.w3.org/2001/XMLSchema#dateTime", "timestamp");
        map.put("http://www.w3.org/2001/XMLSchema#dateTimeStamp", "TIMESTAMPTZ");

        map.put("http://www.w3.org/2001/XMLSchema#gYear", "INT");
        map.put("http://www.w3.org/2001/XMLSchema#gMonth", "INT");
        map.put("http://www.w3.org/2001/XMLSchema#gDay", "INT");
        map.put("http://www.w3.org/2001/XMLSchema#gYearMonth", "TEXT");
        map.put("http://www.w3.org/2001/XMLSchema#gMonthDay", "TEXT");
        map.put("http://www.w3.org/2001/XMLSchema#duration", "TEXT");
        map.put("http://www.w3.org/2001/XMLSchema#yearMonthDuration", "TEXT");
        map.put("http://www.w3.org/2001/XMLSchema#dayTimeDuration", "TEXT");

        map.put("http://www.w3.org/2001/XMLSchema#byte", "char");
        map.put("http://www.w3.org/2001/XMLSchema#short", "char");
        map.put("http://www.w3.org/2001/XMLSchema#int", "INT");
        map.put("http://www.w3.org/2001/XMLSchema#long", "numeric");
        map.put("http://www.w3.org/2001/XMLSchema#unsignedbyte", "char");
        map.put("http://www.w3.org/2001/XMLSchema#unsignedshort", "char");
        map.put("http://www.w3.org/2001/XMLSchema#unsignedint", "INT");
        map.put("http://www.w3.org/2001/XMLSchema#unsignedlong", "numeric");


        map.put("http://www.w3.org/2001/XMLSchema#positiveInteger", "INT");
        map.put("http://www.w3.org/2001/XMLSchema#nonNegativeInteger", "INT");
        map.put("http://www.w3.org/2001/XMLSchema#negativeInteger", "INT");
        map.put("http://www.w3.org/2001/XMLSchema#nonPositiveInteger", "INT");

        map.put("http://www.w3.org/2001/XMLSchema#hexBinary", "TEXT");
        map.put("http://www.w3.org/2001/XMLSchema#base64Binary", "TEXT");

        map.put("http://www.w3.org/2001/XMLSchema#anyURI", "TEXT");
        map.put("http://www.w3.org/2001/XMLSchema#language", "TEXT");
        map.put("http://www.w3.org/2001/XMLSchema#normalizedString", "TEXT");
        map.put("http://www.w3.org/2001/XMLSchema#token", "TEXT");
        map.put("http://www.w3.org/2001/XMLSchema#NMTOKEN", "TEXT");
        map.put("http://www.w3.org/2001/XMLSchema#Name", "TEXT");
        map.put("http://www.w3.org/2001/XMLSchema#NCName", "TEXT");
    }

}

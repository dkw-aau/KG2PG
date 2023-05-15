package cs.utils;

import java.util.HashMap;

// This class maps RDF literal types to Postgres data types
public class TypesMapper {
    public HashMap<String, String> mappings = new HashMap<>();
    
    public void mapTypes() {
        //look at the table https://www.w3.org/TR/rdf11-concepts/
        mappings.put("http://www.w3.org/2001/XMLSchema#string", "TEXT");
        mappings.put("http://www.w3.org/2001/XMLSchema#boolean", "boolean");
        mappings.put("http://www.w3.org/2001/XMLSchema#decimal", "decimal");
        mappings.put("http://www.w3.org/2001/XMLSchema#integer", "INT");
        
        mappings.put("http://www.w3.org/2001/XMLSchema#double", "numeric");
        mappings.put("http://www.w3.org/2001/XMLSchema#float", "numeric");
        
        mappings.put("http://www.w3.org/2001/XMLSchema#date", "date");
        mappings.put("http://www.w3.org/2001/XMLSchema#time", "time");
        mappings.put("http://www.w3.org/2001/XMLSchema#dateTime", "timestamp");
        mappings.put("http://www.w3.org/2001/XMLSchema#dateTimeStamp", "TIMESTAMPTZ");
        
        mappings.put("http://www.w3.org/2001/XMLSchema#gYear", "INT");
        mappings.put("http://www.w3.org/2001/XMLSchema#gMonth", "INT");
        mappings.put("http://www.w3.org/2001/XMLSchema#gDay", "INT");
        mappings.put("http://www.w3.org/2001/XMLSchema#gYearMonth", "TEXT");
        mappings.put("http://www.w3.org/2001/XMLSchema#gMonthDay", "TEXT");
        mappings.put("http://www.w3.org/2001/XMLSchema#duration", "TEXT");
        mappings.put("http://www.w3.org/2001/XMLSchema#yearMonthDuration", "TEXT");
        mappings.put("http://www.w3.org/2001/XMLSchema#dayTimeDuration", "TEXT");
        
        mappings.put("http://www.w3.org/2001/XMLSchema#byte", "char");
        mappings.put("http://www.w3.org/2001/XMLSchema#short", "char");
        mappings.put("http://www.w3.org/2001/XMLSchema#int", "INT");
        mappings.put("http://www.w3.org/2001/XMLSchema#long", "numeric");
        mappings.put("http://www.w3.org/2001/XMLSchema#unsignedbyte", "char");
        mappings.put("http://www.w3.org/2001/XMLSchema#unsignedshort", "char");
        mappings.put("http://www.w3.org/2001/XMLSchema#unsignedint", "INT");
        mappings.put("http://www.w3.org/2001/XMLSchema#unsignedlong", "numeric");
        
        
        mappings.put("http://www.w3.org/2001/XMLSchema#positiveInteger", "INT");
        mappings.put("http://www.w3.org/2001/XMLSchema#nonNegativeInteger", "INT");
        mappings.put("http://www.w3.org/2001/XMLSchema#negativeInteger", "INT");
        mappings.put("http://www.w3.org/2001/XMLSchema#nonPositiveInteger", "INT");
        
        mappings.put("http://www.w3.org/2001/XMLSchema#hexBinary", "TEXT");
        mappings.put("http://www.w3.org/2001/XMLSchema#base64Binary", "TEXT");
        
        mappings.put("http://www.w3.org/2001/XMLSchema#anyURI", "TEXT");
        mappings.put("http://www.w3.org/2001/XMLSchema#language", "TEXT");
        mappings.put("http://www.w3.org/2001/XMLSchema#normalizedString", "TEXT");
        mappings.put("http://www.w3.org/2001/XMLSchema#token", "TEXT");
        mappings.put("http://www.w3.org/2001/XMLSchema#NMTOKEN", "TEXT");
        mappings.put("http://www.w3.org/2001/XMLSchema#Name", "TEXT");
        mappings.put("http://www.w3.org/2001/XMLSchema#NCName", "TEXT");
    }
}

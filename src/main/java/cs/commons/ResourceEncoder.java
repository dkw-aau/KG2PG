package cs.commons;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.HashMap;

public class ResourceEncoder {
    int counter;
    HashMap<Integer, Resource> table;
    HashMap<Resource, Integer> reverseTable;

    public ResourceEncoder() {
        this.counter = -1;
        this.table = new HashMap<>();
        this.reverseTable = new HashMap<>();
    }

    public int encodeAsResource(String val) {
        Resource resource = ResourceFactory.createResource(val);
        if (reverseTable.containsKey(resource)) {
            return reverseTable.get(resource);
        } else {
            this.counter++;
            table.put(counter, resource);
            reverseTable.put(resource, counter);
            return counter;
        }
    }

    public Resource decodeAsResource(int val) {
        return this.table.get(val);
    }

    public HashMap<Integer, Resource> getTable() {
        return table;
    }
}

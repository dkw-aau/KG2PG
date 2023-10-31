package cs.commons;

import cs.utils.Tuple2;

import java.util.*;

/**
 *
 */
public class EntityData {
    Set<Integer> classTypes; // O(T) number of types of this node
    Map<String, String> keyValue;
    
    public EntityData() {
        this.classTypes = new HashSet<>();
        this.keyValue = new HashMap<>();
    }
    
    public Set<Integer> getClassTypes() {
        return classTypes;
    }

    public Map<String, String> getKeyValue() {
        return keyValue;
    }
}

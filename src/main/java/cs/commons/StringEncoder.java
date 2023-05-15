package cs.commons;

import java.util.HashMap;
import java.util.Map;

public class StringEncoder {
    private final Map<String, Integer> encodingMap;
    private int nextId;

    public StringEncoder() {
        encodingMap = new HashMap<>();
        nextId = 0;
    }

    public int encode(String str) {
        if (encodingMap.containsKey(str)) {
            return encodingMap.get(str);
        } else {
            encodingMap.put(str, nextId);
            int encodedValue = nextId;
            nextId++;
            return encodedValue;
        }
    }

    public String decode(int encodedValue) {
        for (Map.Entry<String, Integer> entry : encodingMap.entrySet()) {
            if (entry.getValue() == encodedValue) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Invalid encoded value: " + encodedValue);
    }

    public int size() {
        return encodingMap.size();
    }
}

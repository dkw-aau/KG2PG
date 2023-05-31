package cs.commons;

import org.semanticweb.yars.nx.Node;

import java.util.HashMap;
import java.util.Map;

public class StringEncoder implements Encoder {
    int counter;
    HashMap<Integer, String> table;
    HashMap<String, Integer> reverseTable;

    public StringEncoder() {
        this.counter = -1;
        this.table = new HashMap<>();
        this.reverseTable = new HashMap<>();
    }

    public int encode(String val) {
        if (reverseTable.containsKey(val)) {
            return reverseTable.get(val);
        } else {
            this.counter++;
            table.put(counter, val);
            reverseTable.put(val, counter);
            return counter;
        }
    }

//    public boolean isEncoded(String val) {
//        return reverseTable.containsKey(val);
//    }


    public HashMap<Integer, String> getTable() {
        return table;
    }

    public String decode(int val) {
        return this.table.get(val);
    }

    @Override
    public int encodeNode(Node val) {
        return 0;
    }

    @Override
    public Node decodeNode(int val) {
        return null;
    }

    public HashMap<String, Integer> getRevTable() {
        return reverseTable;
    }
}

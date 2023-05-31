package cs.commons;

import org.semanticweb.yars.nx.Node;

public interface Encoder {

    int encode(String val);

    String decode(int val);

    int encodeNode(Node val);

    Node decodeNode(int val);
}

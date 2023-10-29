package cs.graphTranslation;

import cs.utils.Constants;
import cs.utils.TypesMapper;
import cs.utils.neo.Neo4jGraph;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataTransUpdatesNpm {
    TypesMapper typesMapper = new TypesMapper();
    Neo4jGraph neo4jGraph = new Neo4jGraph();

    public DataTransUpdatesNpm() {
    }

    public void run() {
        handleAddedTriples("/Users/kashifrabbani/Documents/GitHub/KG2PG/data/monotone/addedTriples.nt");
    }

    //create method to read rdf NT file which contains added triples
    public void handleAddedTriples(String filePath) {
        try {
            Files.lines(Path.of(filePath)).forEach(line -> {
                try {
                    Node[] nodes = NxParser.parseNodes(line);
                    Node entityNode = nodes[0];
                    Node predicateNode = nodes[1];
                    Node objectNode = nodes[2];
                    Resource propAsResource = ResourceFactory.createResource(nodes[1].getLabel());
                    // In case this is a new entity, a node will be created.
                    if (!isNodeExists(entityNode)) {
                        createNode(entityNode);
                    }
                    // In case the objectNode is an IRI, a node and an edge will be created.
                    if (isIri(objectNode)) {
                        if (predicateNode.toString().equals(Constants.RDF_TYPE)) {
                            System.out.println("Add Label for Node:" + entityNode.getLabel() + " " + objectNode.getLabel());
                        } else {
                            if (!isNodeExists(objectNode)) {
                                createNode(objectNode);
                                //TODO :: Create Edge
                                System.out.println("Create Edge for IRI:" + entityNode.getLabel() + " " + predicateNode.getLabel() + " " + objectNode.getLabel());
                            }
                        }
                    } else {
                        String value = nodes[2].toString();
                        Resource dataTypeResource = extractDataType(objectNode);
                        String dataType = dataTypeResource.getURI();
                        String dataTypeLocalName = dataTypeResource.getLocalName();
                        if (nodes[2] instanceof Literal) {
                            if (((Literal) nodes[2]).getDatatype() != null) {
                                value = nodes[2].getLabel();
                            }
                            if (((Literal) nodes[2]).getLanguageTag() != null) {
                                value = value.replaceAll("@" + ((Literal) nodes[2]).getLanguageTag(), "");
                            }
                        } else if ((ResourceFactory.createResource(nodes[2].toString())).isURIResource()) {
                            value = nodes[2].getLabel();
                            dataTypeLocalName = "IRI";
                            dataType = "IRI";
                        }

                        String cypherType = typesMapper.getMap().get(dataType);
                        if (cypherType == null) cypherType = "STRING";
                        String lineForLiteral = "id" + "|" + value + "|" + dataType + "|" + cypherType + "|" + dataTypeLocalName + ";LitNode";
                        System.out.println(lineForLiteral);
                        //TODO:: Create Edge
                        if (!predicateNode.toString().equals(Constants.RDF_TYPE)) {
                            System.out.println("Create Edge for Literal:" + entityNode.getLabel() + " " + predicateNode.getLabel() + " " + lineForLiteral);
                            createEdgeForLiteralNode(entityNode, predicateNode, value, dataType, cypherType, dataTypeLocalName);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //create method to read rdf NT file which contains deleted triples
    public void handleDeletedTriples() {
        try {
            Files.lines(Path.of("")).forEach(line -> {
                try {
                    Node[] nodes = NxParser.parseNodes(line);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //create method to read rdf NT file which contains updated triples
    public void handleUpdatedTriples() {
        try {
            Files.lines(Path.of("")).forEach(line -> {
                try {
                    Node[] nodes = NxParser.parseNodes(line);
                    //TODO:: 1 - Check if object is literal or IRI
                    // If IRI, then check if the node exists,
                    // Then construct a cypher query for update by ensuring that node with iri = nodes[0].getLabel() exists and edge with iri = nodes[1].getLabel() exists
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ****************************** Helper Methods ******************************

    private boolean isNodeExists(Node node) {
        boolean exists = neo4jGraph.nodeExistsWithIri(node.getLabel());
        System.out.println(node.getLabel() + " node exist? " + exists);
        return exists;
    }

    // Create method to create node in the PG using Cypher Query
    private void createNode(Node node) {
        //Creating Node
        System.out.println("Create Node for:" + node.getLabel());
        neo4jGraph.createNodeWithIri(node.getLabel());
    }

    // Create method to create edge in the PG using Cypher Query
    private void createEdgeForIriNode(Node entityNode, Node predicateNode, Node objectNode) {
        //TODO:: Create a cypher query to create an edge between entityNode and objectNode

    }

    private void createEdgeForLiteralNode(Node entityNode, Node predicateNode, String value, String dataType, String cypherType, String dataTypeLocalName) {
        String label = "LitNode";
        int id = 1; //TODO:: generate random ID
        //Create Literal Node in PG with Cypher Query
        //"id:ID|object_value|object_type|type|:LABEL"

    }

    private boolean isIri(Node node) {
        //Check if it starts with < and ends with >
        return node.toString().startsWith("<") && node.toString().endsWith(">");
    }

    private Resource extractDataType(Node node) {
        Resource literalDataType = ResourceFactory.createResource("http://www.w3.org/2001/XMLSchema#string");

        try {
            if (node instanceof Literal) {
                Literal objAsLiteral = (Literal) node;
                if (objAsLiteral.getDatatype() != null) {
                    literalDataType = ResourceFactory.createResource(objAsLiteral.getDatatype().getLabel());
                }
            } else {
                // Handle the case when the node is not a Literal
                //System.err.println("Error: Node is not a Literal");
                return literalDataType;
            }
        } catch (NullPointerException e) {
            // Handle NullPointerException here
            System.err.println("Error: Node is null or does not have a datatype");
            e.printStackTrace();
            return literalDataType;
        }

        return literalDataType;
    }

}

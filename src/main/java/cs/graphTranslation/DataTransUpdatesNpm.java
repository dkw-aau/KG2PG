package cs.graphTranslation;

import cs.utils.Constants;
import cs.utils.FilesUtil;
import cs.utils.TypesMapper;
import cs.utils.neo.Neo4jGraph;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class DataTransUpdatesNpm {
    TypesMapper typesMapper = new TypesMapper();
    Neo4jGraph neo4jGraph = new Neo4jGraph();
    Map<String, String> prefixMap;
    long totalLitNodeCount = 0;

    public DataTransUpdatesNpm() {
    }

    public void run() {
        readPrefixMapForOriginalGraph("/Users/kashifrabbani/Documents/GitHub/KG2PG/Output/test-monotone/v0/PG_PREFIX_MAP.csv");
        totalLitNodeCount = neo4jGraph.getTotalLiteralNodeCount();
        handleAddedTriples("/Users/kashifrabbani/Documents/GitHub/KG2PG/data/monotone/addedTriples.nt");
    }

    private void readPrefixMapForOriginalGraph(String prefixMapCsvFileAddress) {
        prefixMap = FilesUtil.readCsvToMap(prefixMapCsvFileAddress);
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
                            Resource objectAsResource = ResourceFactory.createResource(objectNode.getLabel());
                            String namespace = objectAsResource.getNameSpace();
                            String prefixedLabel;
                            if (prefixMap.containsKey(namespace)) {
                                prefixedLabel = prefixMap.get(namespace) + "_" + objectAsResource.getLocalName();
                                System.out.println("Adding Label for Node: " + entityNode.getLabel() + " " + prefixedLabel);
                            } else {
                                String newPrefix = "ns" + (prefixMap.size() + 1);
                                prefixedLabel = newPrefix + "_" + objectAsResource.getLocalName();
                                prefixMap.put(namespace, newPrefix);
                                System.out.println("Adding Label for Node: (Added new Namespace prefix -> )" + entityNode.getLabel() + " " + prefixedLabel);
                            }
                            neo4jGraph.addLabelToNodeWithIri(entityNode.getLabel(), prefixedLabel);
                        } else {
                            if (!isNodeExists(objectNode)) {
                                createNode(objectNode);
                            }
                            System.out.println("Create Edge for IRI:" + entityNode.getLabel() + " " + predicateNode.getLabel() + " " + objectNode.getLabel());
                            String prefixedEdge = getPrefixedEdge(propAsResource);
                            neo4jGraph.createEdgeBetweenTwoNodes(entityNode.getLabel(), objectNode.getLabel(), prefixedEdge, "property", predicateNode.getLabel());
                        }
                    } else {
                        String value = objectNode.toString();
                        Resource dataTypeResource = extractDataType(objectNode);
                        String dataType = dataTypeResource.getURI();
                        String dataTypeLocalName = dataTypeResource.getLocalName();
                        if (objectNode instanceof Literal) {
                            if (((Literal) objectNode).getDatatype() != null) {
                                value = objectNode.getLabel();
                            }
                            if (((Literal) objectNode).getLanguageTag() != null) {
                                value = value.replaceAll("@" + ((Literal) objectNode).getLanguageTag(), "");
                            }
                        } else if ((ResourceFactory.createResource(objectNode.toString())).isURIResource()) {
                            value = objectNode.getLabel();
                            dataTypeLocalName = "IRI";
                            dataType = "IRI";
                        }

                        String cypherType = typesMapper.getMap().get(dataType);
                        if (cypherType == null) cypherType = "STRING";
                        if (!predicateNode.toString().equals(Constants.RDF_TYPE)) {
                            String prefixedEdge = getPrefixedEdge(propAsResource);
                            createEdgeForLiteralNode(entityNode, predicateNode, value, dataType, cypherType, dataTypeLocalName, prefixedEdge);
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
    public void handleDeletedTriples(String filePath) {
        try {
            Files.lines(Path.of(filePath)).forEach(line -> {
                try {
                    Node[] nodes = NxParser.parseNodes(line);
                    Node entityNode = nodes[0];
                    Node predicateNode = nodes[1];
                    Node objectNode = nodes[2];
                    Resource propAsResource = ResourceFactory.createResource(predicateNode.getLabel());
                    if (isIri(objectNode)) {

                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO:: create method to read rdf NT file which contains updated triples
    public void handleUpdatedTriples(String filePath) {
        try {
            Files.lines(Path.of(filePath)).forEach(line -> {
                try {
                    Node[] nodes = NxParser.parseNodes(line);
                    Node entityNode = nodes[0];
                    Node predicateNode = nodes[1];
                    Node objectNode = nodes[2];
                    Resource propAsResource = ResourceFactory.createResource(predicateNode.getLabel());
                    if (isIri(objectNode)) {
                        String prefixedEdge = getPrefixedEdge(propAsResource);
                        //FIXME:: What to do?
                        //neo4jGraph.createEdgeBetweenTwoNodes(entityNode.getLabel(), objectNode.getLabel(), prefixedEdge, "property", predicateNode.getLabel());
                    } else {
                        // Update the object value and type
                        String value = objectNode.toString();
                        Resource dataTypeResource = extractDataType(objectNode);
                        String dataType = dataTypeResource.getURI();
                        String dataTypeLocalName = dataTypeResource.getLocalName();
                        if (objectNode instanceof Literal) {
                            if (((Literal) objectNode).getDatatype() != null) {
                                value = objectNode.getLabel();
                            }
                            if (((Literal) objectNode).getLanguageTag() != null) {
                                value = value.replaceAll("@" + ((Literal) objectNode).getLanguageTag(), "");
                            }
                        } else if ((ResourceFactory.createResource(objectNode.toString())).isURIResource()) {
                            value = objectNode.getLabel();
                            dataTypeLocalName = "IRI";
                            dataType = "IRI";
                        }

                        String cypherType = typesMapper.getMap().get(dataType);
                        if (cypherType == null) cypherType = "STRING";

                        if (!predicateNode.toString().equals(Constants.RDF_TYPE)) {
                            String prefixedEdge = getPrefixedEdge(propAsResource);
                            //FIXME:: What to do? Update the value and type of the literal node
                            //createEdgeForLiteralNode(entityNode, predicateNode, value, dataType, cypherType, dataTypeLocalName, prefixedEdge);

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

    // ****************************** Helper Methods ******************************
    @NotNull
    private String getPrefixedEdge(Resource propAsResource) {
        String prefixedEdge;
        if (prefixMap.containsKey(propAsResource.getNameSpace())) {
            prefixedEdge = prefixMap.get(propAsResource.getNameSpace()) + "_" + propAsResource.getLocalName();
        } else {
            String newPrefix = "ns" + (prefixMap.size() + 1);
            prefixedEdge = newPrefix + "_" + propAsResource.getLocalName();
            prefixMap.put(propAsResource.getNameSpace(), newPrefix);
        }
        return prefixedEdge;
    }

    private boolean isNodeExists(Node node) {
        boolean exists = neo4jGraph.nodeExistsWithIri(node.getLabel());
        System.out.println(node.getLabel() + " node exist? " + exists);
        return exists;
    }

    // Create method to create node in the PG using Cypher Query
    private void createNode(Node node) {
        System.out.println("Create Node for:" + node.getLabel());
        neo4jGraph.createNodeWithIri(node.getLabel());
    }

    private void createEdgeForLiteralNode(Node entityNode, Node predicateNode, String value, String dataType, String cypherType, String dataTypeLocalName, String prefixedEdge) {
        totalLitNodeCount++;
        int id = (int) totalLitNodeCount + 1;
        neo4jGraph.createLiteralObjectNode(id, dataType, value, cypherType);
        neo4jGraph.createEdgeBetweenAnIriAndLitNode(entityNode.getLabel(), id, prefixedEdge, "property", predicateNode.getLabel());
    }

    private boolean isIri(Node node) {
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

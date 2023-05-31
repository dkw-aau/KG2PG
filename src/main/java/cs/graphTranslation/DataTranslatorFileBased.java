package cs.graphTranslation;

import cs.commons.ResourceEncoder;
import cs.schemaTranslation.SchemaTranslator;
import cs.schemaTranslation.pgSchema.PgEdge;
import cs.schemaTranslation.pgSchema.PgNode;
import org.apache.commons.lang3.time.StopWatch;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import kotlin.Pair;

public class DataTranslatorFileBased {
    String rdfFilePath;
    Integer expectedNumberOfClasses;
    Integer expNoOfInstances;
    ResourceEncoder resourceEncoder;
    String typePredicate;

    // In the following the size of each data structure
    // N = number of distinct nodes in the graph
    // T = number of distinct types
    // P = number of distinct predicates

    Map<Node, EntityData> entityDataHashMap; // Size == N For every entity we save a number of summary information
    Map<Integer, Integer> classEntityCount; // Size == T
    SchemaTranslator schemaTranslator;

    public DataTranslatorFileBased(String filePath, int expNoOfClasses, int expNoOfInstances, String typePredicate, ResourceEncoder resourceEncoder, SchemaTranslator schemaTranslator) {
        this.rdfFilePath = filePath;
        this.expectedNumberOfClasses = expNoOfClasses;
        this.expNoOfInstances = expNoOfInstances;
        this.typePredicate = typePredicate;
        this.classEntityCount = new HashMap<>((int) ((expectedNumberOfClasses) / 0.75 + 1));
        this.entityDataHashMap = new HashMap<>((int) ((expNoOfInstances) / 0.75 + 1));
        this.resourceEncoder = resourceEncoder;
        this.schemaTranslator = schemaTranslator;
    }

    /**
     * ============================================= Run Parser ========================================
     */
    public void run() {
        entityExtraction();
        entitiesToPgNodes();
        entityConstraintsExtraction();
        System.out.println("STATS: \n\t" + "No. of Classes: " + classEntityCount.size());
    }


    /**
     * ============================================= Phase 1: Entity Extraction ========================================
     * Streaming over RDF (NT Format) triples <s,p,o> line by line to extract set of entity types and frequency of each entity.
     * =================================================================================================================
     */
    protected void entityExtraction() {
        System.out.println("invoked::firstPass()");
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Files.lines(Path.of(rdfFilePath)).forEach(line -> {
                try {
                    Node[] nodes = NxParser.parseNodes(line); // Get [S,P,O] as Node from triple
                    if (nodes[1].toString().equals(typePredicate)) { // Check if predicate is rdf:type or equivalent
                        // Track classes per entity
                        int objID = resourceEncoder.encodeAsResource(nodes[2].getLabel());
                        EntityData entityData = entityDataHashMap.get(nodes[0]);
                        if (entityData == null) {
                            entityData = new EntityData();
                        }
                        entityData.getClassTypes().add(objID);
                        entityDataHashMap.put(nodes[0], entityData);
                        classEntityCount.merge(objID, 1, Integer::sum);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        watch.stop();
    }


    private void entitiesToPgNodes() {
        entityDataHashMap.forEach(((node, entityData) -> {
            //System.out.println(node + " : " + entityData.getClassTypes());
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE (");
            entityData.getClassTypes().forEach(classID -> {
                sb.append(":").append(resourceEncoder.decodeAsResource(classID).getLocalName());
            });
            sb.append(" { iri : \"").append(node.getLabel()).append("\"})");
            System.out.println(sb);
        }));
    }


    protected void entityConstraintsExtraction() {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Set<Integer> pgEdgeSet = schemaTranslator.getPgSchema().getPgEdges();
            Files.lines(Path.of(rdfFilePath)).forEach(line -> {
                try {
                    // parsing <s,p,o> of triple from each line as node[0], node[1], and node[2]
                    Node[] nodes = NxParser.parseNodes(line);
                    Node entityNode = nodes[0];

                    if (!nodes[1].toString().equals(typePredicate)) {
                        Set<Integer> entityTypes = entityDataHashMap.get(entityNode).getClassTypes();
                        int propertyKey = resourceEncoder.encodeAsResource(nodes[1].toString());

                        System.out.println("propertyKey = " + propertyKey);
                        boolean literalFlag = false;
                        for (Integer pgEdge : pgEdgeSet) {
                            if (pgEdge.equals(propertyKey)) {
                                System.out.println("pgEdge = " + pgEdge);
                                PgEdge currEdge = PgEdge.getEdgeById(pgEdge);
                                //FIXME: Think again if you should use isProperty() or only isLiteral(). What would be the difference?
                                if (currEdge.isProperty() && currEdge.isLiteral()) {
                                    System.out.println("isKeyValueFlag = " + nodes[1].toString() + " - " + currEdge.getDataType());
                                    literalFlag = true;
                                    break;
                                }
                            }
                        }
                        if (literalFlag) {
                            System.out.println("literalFlag = " + true);
                            //TODO: Add the object value as key value property to the node
                        } else {
                            System.out.println("literalFlag = " + false);
                            //TODO: Add the object value as edge to the node with a match to a specific node (which should exist already)
                            String cypherQuery = """
                                    MATCH (s {id: "JaneDoe"}), (u {id: "MIT"})
                                    CREATE (s)-[:studiesAt]->(u)
                                    """;
                        }

                        //FIXME: The following code snippet is not required
                        for (int entity : entityTypes) {
                            Pair<Integer, Integer> pair = new Pair<>(entity, propertyKey);
                            if (schemaTranslator.getPgSchema().getNodeEdgeTarget().containsKey(pair)) {
                                System.out.println("pair = " + pair);
                                System.out.println("schemaTranslator.getPgSchema().getNodeEdgeTarget().get(pair) = " + schemaTranslator.getPgSchema().getNodeEdgeTarget().get(pair));
                            } else {
                                System.out.println("pair = " + pair);
                                System.out.println("___________________________");
                            }
                        }
                    }


//                    //Declaring required sets
//                    Set<Integer> objTypesIDs = new HashSet<>(10);
//                    Set<Tuple2<Integer, Integer>> prop2objTypeTuples = new HashSet<>(10);
//
//
//                    String objectType = extractObjectType(nodes[2].toString());
//                    int propID = stringEncoder.encode(nodes[1].getLabel());
//
//                    // object is an instance or entity of some class e.g., :Paris is an instance of :City & :Capital
//                    if (objectType.equals("IRI")) {
//                        objTypesIDs = parseIriTypeObject(objTypesIDs, prop2objTypeTuples, nodes, entityNode, propID);
//                    }
//                    // Object is of type literal, e.g., xsd:String, xsd:Integer, etc.
//                    else {
//                        parseLiteralTypeObject(objTypesIDs, entityNode, objectType, propID);
//                    }
//                    // for each type (class) of current entity -> append the property and object type in classToPropWithObjTypes HashMap
//                    updateClassToPropWithObjTypesMap(objTypesIDs, entityNode, propID);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        watch.stop();
    }
}

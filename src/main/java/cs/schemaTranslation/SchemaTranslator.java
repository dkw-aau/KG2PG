package cs.schemaTranslation;

import cs.Main;
import cs.commons.Reader;
import cs.commons.ResourceEncoder;
import cs.schemaTranslation.pgSchema.PgEdge;
import cs.schemaTranslation.pgSchema.PgNode;
import cs.schemaTranslation.pgSchema.PgSchema;
import cs.schemaTranslation.pgSchema.PgSchemaWriter;
import cs.utils.ConfigManager;
import kotlin.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.engine.constraint.*;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.PropertyShape;
import org.apache.jena.shacl.parser.ShaclParseException;
import org.apache.jena.shacl.parser.Shape;

import java.util.ArrayList;
import java.util.List;


/**
 * Translate SHACL shapes schema to Property Graph Schema (PG-Schema)
 */
public class SchemaTranslator {
    ResourceEncoder resourceEncoder;
    PgSchema pgSchema;

    public SchemaTranslator(ResourceEncoder encoder) {
        resourceEncoder = encoder;
        pgSchema = new PgSchema();
        Main.logger.info("SchemaTranslator initialized, reading SHACL shapes...");
        Shapes shapes = readShapes();
        Main.logger.info("SHACL shapes read successfully, parsing SHACL shapes...");
        parseShapes(shapes);
        Main.logger.info("SHACL shapes parsed successfully, writing PG-Schema to file...");
        writePgSchema();
    }

    private void writePgSchema() {
        PgSchemaWriter pgSchemaWriter = new PgSchemaWriter(resourceEncoder, pgSchema);
        pgSchemaWriter.parseSchema();
        pgSchemaWriter.writePgSchemaSyntaxToFile();
    }

    private Shapes readShapes() {
        Shapes shapes = null;
        try {
            Model shapesModel = Reader.readFileToModel(ConfigManager.getProperty("shapes_path"), "TURTLE");
            Main.logger.info("Size of ShapesModel: " + String.valueOf(shapesModel.size()));
            shapes = Shapes.parse(shapesModel);
        } catch (ShaclParseException e) {
            Main.logger.error("Error parsing SHACL shapes: " + e.getMessage());
        }
        //extractShNodeFromShaclShapes(shapesModel);
        return shapes;
    }

    //* Parse SHACL shapes and create PG-Schema
    private void parseShapes(Shapes shapes) {
        //TODO: Sometimes, node shapes do not have target, so we need to check if the target is null or not
        for (Shape t : shapes.getTargetShapes()) {
            String nodeTarget = "";
            for (Target target : t.getTargets()) {
                nodeTarget = target.getObject().getURI(); //  node shapes have only one target
            }
            int encodedTarget = resourceEncoder.encodeAsResource(nodeTarget);
            PgNode pgNode = null;
            if (pgSchema.getNodesToEdges().containsKey(encodedTarget)) {
                pgNode = PgNode.getNodeById(encodedTarget);
            } else {
                pgNode = new PgNode(encodedTarget);
            }
            pgNode.setNodeShapeIri(t.getShapeNode().getURI());
            pgSchema.addNode(pgNode);
            for (PropertyShape ps : t.getPropertyShapes()) {
                parsePropertyShapeConstraints(ps, pgNode);
            }
        }
    }

    /**
     * This method will parse the constraints of a property shape and transform them into PG-Schema constraints
     */
    private void parsePropertyShapeConstraints(PropertyShape ps, PgNode pgNode) {
        PgEdge pgEdge = new PgEdge(resourceEncoder.encodeAsResource(ps.getPath().toString().replace("<", "").replace(">", "")));
        pgSchema.addSourceEdge(pgNode, pgEdge);
        int minCount = -1;
        int maxCount = -1;
        for (Constraint constraint : ps.getConstraints()) {
            //System.out.println(constraint.getClass().getSimpleName());
            switch (constraint.getClass().getSimpleName()) {
                // ******   cardinality constraints
                case "MinCount" -> {
                    minCount = ((MinCount) constraint).getMinCount();

                }
                case "MaxCount" -> {
                    maxCount = ((MaxCount) constraint).getMaxCount();
                }

                //******  node kind constraints : IRI or Literal ******

                case "ClassConstraint" -> { //    sh:NodeKind sh:IRI ;     sh:class    ex:University or any other class IRI
                    parseClassConstraint(pgNode, pgEdge, (ClassConstraint) constraint);
                }
                case "DatatypeConstraint" -> { //    sh:NodeKind sh:Literal ;     sh:datatype xsd:string or any other primitive data type
                    Node dataTypeConstraint = ((DatatypeConstraint) constraint).getDatatype();
                    pgEdge.setDataType(dataTypeConstraint.getLocalName());
                    // Use id of pgNode and pgEdge and create a pair to store the boolean value of  pgNodeEdgeBooleanMap in PGSchema
                    Pair<Integer, Integer> nodeEdgePair = new Pair<>(pgNode.getId(), pgEdge.getId());
                    pgSchema.getPgNodeEdgeBooleanMap().put(nodeEdgePair, true);
                }

                // Multi Type Constraints: Homogenous (literals or IRIs) or Heterogeneous (literals and IRIs)
                case "ShOr" -> {
                    List<ClassConstraint> classConstraints = new ArrayList<>();
                    List<DatatypeConstraint> datatypeConstraints = new ArrayList<>();

                    for (Shape shape : ((ShOr) constraint).getOthers()) {
                        for (Constraint shOrConstraint : shape.getConstraints()) {
                            if (shOrConstraint.getClass().getSimpleName().equals("ClassConstraint")) { // sh:NodeKind sh:IRI ;
                                classConstraints.add((ClassConstraint) shOrConstraint);
                            } else if (shOrConstraint.getClass().getSimpleName().equals("DatatypeConstraint")) { // sh:NodeKind sh:Literal ;
                                DatatypeConstraint datatypeConstraint = (DatatypeConstraint) shOrConstraint;
                                datatypeConstraints.add(datatypeConstraint); //Node dataTypeConstraint = datatypeConstraint.getDatatype();
                            }
                        }
                    }
                    if (classConstraints.size() > 0 && datatypeConstraints.size() > 0) { // Heterogeneous (Literal and IRI) Multi Type Constraint
                        handleMultiDataTypeConstraints(pgNode, pgEdge, datatypeConstraints);
                        classConstraints.forEach(classConstraint -> parseClassConstraint(pgNode, pgEdge, classConstraint));
                    } else if (classConstraints.size() > 0) { // Homogeneous (IRI) Multi Type Constraint
                        classConstraints.forEach(classConstraint -> parseClassConstraint(pgNode, pgEdge, classConstraint));
                    } else if (datatypeConstraints.size() > 0) { // Homogeneous (Literal) Multi Type Constraint
                        handleMultiDataTypeConstraints(pgNode, pgEdge, datatypeConstraints);
                    }
                }

                /* case "ShNode" -> { ShNode shNode = ((ShNode) constraint); } */ //FIXME : Do you need this?
                /*case "InConstraint" -> { Node inConstraint = ((InConstraint) constraint).getComponent(); }*/ //FIXME : Do you need this?
                default -> { /*System.out.println("Default case: unhandled constraint: " + constraint); */}
            }
        }

        Pair<Integer, Integer> cardPair = new Pair<>(minCount, maxCount);
        Pair<Integer, Integer> nodeEdgePair = new Pair<>(pgNode.getId(), pgEdge.getId());
        if (pgSchema.getNodeEdgeTarget().get(nodeEdgePair) != null) {
            pgSchema.getNodeEdgeCardinalityMap().put(nodeEdgePair, cardPair);
        } else {
            pgSchema.getNodeEdgeCardinalityMap().put(nodeEdgePair, cardPair);
        }
    }

    /**
     * This method will create a new node for each datatype constraint
     */
    private void handleMultiDataTypeConstraints(PgNode pgNode, PgEdge pgEdge, List<DatatypeConstraint> datatypeConstraints) {
        datatypeConstraints.forEach(dtConstraint -> { //create a new node for each datatype constraint
            Node dataTypeConstraint = dtConstraint.getDatatype();
            dataTypeConstraint.getLocalName();
            PgNode dtPgNode = null;
            int dtPgNodeEncoded = resourceEncoder.encodeAsResource(dataTypeConstraint.getURI());
            if (PgNode.getNodeById(dtPgNodeEncoded) == null) {
                dtPgNode = new PgNode(dtPgNodeEncoded);
                pgSchema.addNode(dtPgNode);
            } else {
                dtPgNode = PgNode.getNodeById(dtPgNodeEncoded);
            }
            dtPgNode.setAbstract(true);
            pgSchema.addTargetEdge(pgNode, pgEdge, dtPgNode);
        });
    }

    private void parseClassConstraint(PgNode pgNode, PgEdge pgEdge, ClassConstraint constraint) {
        Node classConstraint = constraint.getExpectedClass();
        PgNode targetPgNode = initPgNode(resourceEncoder.encodeAsResource(classConstraint.getURI()));
        pgSchema.addTargetEdge(pgNode, pgEdge, targetPgNode);
    }

    private void extractShNodeFromShaclShapes(Model shapesModel) {
        // SPARQL query to find shapes with sh:node constraint
        String sparqlQuery = """
                PREFIX sh: <http://www.w3.org/ns/shacl#>
                SELECT ?shape ?node {
                  ?shape a sh:NodeShape ;
                         sh:node ?node .
                }""";

        // Create a Query from the SPARQL query string
        Query query = QueryFactory.create(sparqlQuery);

        try (QueryExecution qExec = QueryExecutionFactory.create(query, shapesModel)) {
            // Execute the query and get the ResultSet
            ResultSet resultSet = qExec.execSelect();

            // Process the ResultSet to get the shapes with sh:node constraint
            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.next();
                Resource shapeResource = solution.getResource("shape");
                Resource shapeNodeValue = solution.getResource("node");
                System.out.println("Shape with sh:node constraint: " + shapeResource + " with sh:node = " + shapeNodeValue);
            }
        }
    }

    public PgNode initPgNode(int nodeShapeId) {
        return new PgNode(nodeShapeId);
    }

    public PgSchema getPgSchema() {
        return pgSchema;
    }

}

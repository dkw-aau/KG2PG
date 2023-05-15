package cs.schemaTranslation;

import cs.Main;
import cs.commons.Reader;
import cs.commons.StringEncoder;
import cs.schemaTranslation.pgSchema.PgEdge;
import cs.schemaTranslation.pgSchema.PgNode;
import cs.schemaTranslation.pgSchema.PgSchema;
import cs.schemaTranslation.pgSchema.PgSchemaToNeo4J;
import cs.utils.ConfigManager;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.engine.constraint.*;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.PropertyShape;
import org.apache.jena.shacl.parser.Shape;


/**
 * Translate SHACL shapes schema to Property Graph Schema (PG-Schema)
 */
public class SchemaTranslator {
    StringEncoder encoder;
    PgSchema pgSchema;

    public SchemaTranslator() {
        encoder = new StringEncoder();
        pgSchema = new PgSchema();
        Shapes shapes = readShapes();
        parseShapes(shapes);
        PgSchemaToNeo4J pgSchemaToNeo4J = new PgSchemaToNeo4J(encoder, pgSchema);
        pgSchemaToNeo4J.generateNeo4jQueries();
    }

    private Shapes readShapes() {
        Model shapesModel = Reader.readFileToModel(ConfigManager.getProperty("shapes_path"), "TURTLE");
        Main.logger.info(String.valueOf(shapesModel.size()));
        return Shapes.parse(shapesModel);
    }

    private void parseShapes(Shapes shapes) {
        // At first convert all Node Shapes to PG-Nodes
        for (Shape t : shapes.getTargetShapes()) {
            String nodeTarget = "";
            for (Target target : t.getTargets())
                nodeTarget = target.getObject().getURI(); // our node shapes have only one target
            int encodedTarget = encoder.encode(nodeTarget);
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

    private void parsePropertyShapeConstraints(PropertyShape ps, PgNode pgNode) {
        PgEdge pgEdge = new PgEdge(encoder.encode(ps.getPath().toString()));
        pgSchema.addSourceEdge(pgNode, pgEdge);

        for (Constraint constraint : ps.getConstraints()) {
            //System.out.println(constraint.getClass().getSimpleName());
            switch (constraint.getClass().getSimpleName()) {
                case "ClassConstraint" -> {
                    parseClassConstraint(pgNode, pgEdge, (ClassConstraint) constraint);
                }
                case "ShNode" -> {
                    ShNode shNode = ((ShNode) constraint);
                }
                case "ShOr" -> {
                    for (Shape shape : ((ShOr) constraint).getOthers()) {
                        shape.getConstraints().forEach(shOrConstraint -> {
                            if (shOrConstraint.getClass().getSimpleName().equals("ClassConstraint")) {
                                parseClassConstraint(pgNode, pgEdge, (ClassConstraint) shOrConstraint);
                            }
                        });
                    }
                }
                case "MinCount" -> {
                    Integer minCount = ((MinCount) constraint).getMinCount();
                    pgEdge.setMinCount(minCount);
                }
                case "MaxCount" -> {
                    Integer maxCount = ((MaxCount) constraint).getMaxCount();
                    pgEdge.setMaxCount(maxCount);
                    pgEdge.handlePropertyType();
                }
                case "DatatypeConstraint" -> {
                    Node dataTypeConstraint = ((DatatypeConstraint) constraint).getDatatype();
                    pgEdge.setDataType(dataTypeConstraint.getLocalName());
                }
                case "InConstraint" -> { //FIXME : Do you need this?
                    Node inConstraint = ((InConstraint) constraint).getComponent();
                }
                case "NodeKind" -> { //FIXME : check if this is correct
                    NodeKindConstraint nodeKindConstraint = ((NodeKindConstraint) constraint);
                    //nodeKindConstraint.getKind();
                }
                default -> {
                    System.out.println("Default case: unhandled constraint: " + constraint);
                }
            }
        }

    }

    private void parseClassConstraint(PgNode pgNode, PgEdge pgEdge, ClassConstraint constraint) {
        Node classConstraint = constraint.getExpectedClass();
        PgNode targetPgNode = initPgNode(encoder.encode(classConstraint.getURI()));
        pgSchema.addTargetEdge(pgNode, pgEdge, targetPgNode);
    }

    public PgNode initPgNode(int nodeShapeId) {
        return new PgNode(nodeShapeId);
    }
}

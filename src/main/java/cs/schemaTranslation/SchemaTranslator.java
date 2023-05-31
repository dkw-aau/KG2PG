package cs.schemaTranslation;

import cs.Main;
import cs.commons.Reader;
import cs.commons.ResourceEncoder;
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

import java.util.Set;


/**
 * Translate SHACL shapes schema to Property Graph Schema (PG-Schema)
 */
public class SchemaTranslator {
    ResourceEncoder resourceEncoder;
    PgSchema pgSchema;

    public PgSchema getPgSchema() {
        return pgSchema;
    }

    public SchemaTranslator(ResourceEncoder encoder) {
        resourceEncoder = encoder;
        pgSchema = new PgSchema();
        Shapes shapes = readShapes();
        parseShapes(shapes);
        pgSchema.postProcessPgSchema();
        System.out.println(pgSchema.toString());

        convertToNeo4jQueries();
    }

    private void convertToNeo4jQueries() {
        PgSchemaToNeo4J pgSchemaToNeo4J = new PgSchemaToNeo4J(resourceEncoder, pgSchema);
        pgSchemaToNeo4J.generateNeo4jQueries();
    }

    private Shapes readShapes() {
        Model shapesModel = Reader.readFileToModel(ConfigManager.getProperty("shapes_path"), "TURTLE");
        Main.logger.info(String.valueOf(shapesModel.size()));
        return Shapes.parse(shapesModel);
    }

    //* Parse SHACL shapes and create PG-Schema
    private void parseShapes(Shapes shapes) {
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

    private void parsePropertyShapeConstraints(PropertyShape ps, PgNode pgNode) {
        PgEdge pgEdge = new PgEdge(resourceEncoder.encodeAsResource(ps.getPath().toString()));
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
                }
                case "DatatypeConstraint" -> {
                    Node dataTypeConstraint = ((DatatypeConstraint) constraint).getDatatype();
                    pgEdge.setDataType(dataTypeConstraint.getLocalName());
                    pgEdge.setLiteral(true);
                }
                /*case "InConstraint" -> { //FIXME : Do you need this?
                    Node inConstraint = ((InConstraint) constraint).getComponent();
                }*/
                default -> {
                    System.out.println("Default case: unhandled constraint: " + constraint);
                }
            }
        }

        if (pgEdge.getMinCount() != null && pgEdge.getMaxCount() != null) {
            pgEdge.handlePropertyType();
        }
    }

    private void parseClassConstraint(PgNode pgNode, PgEdge pgEdge, ClassConstraint constraint) {
        Node classConstraint = constraint.getExpectedClass();
        PgNode targetPgNode = initPgNode(resourceEncoder.encodeAsResource(classConstraint.getURI()));
        pgSchema.addTargetEdge(pgNode, pgEdge, targetPgNode);
    }

    public PgNode initPgNode(int nodeShapeId) {
        return new PgNode(nodeShapeId);
    }
}

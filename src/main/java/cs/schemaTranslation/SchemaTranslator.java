package cs.schemaTranslation;

import cs.Main;
import cs.commons.Reader;
import cs.commons.ResourceEncoder;
import cs.schemaTranslation.pgSchema.PgEdge;
import cs.schemaTranslation.pgSchema.PgNode;
import cs.schemaTranslation.pgSchema.PgSchema;
import cs.schemaTranslation.pgSchema.PgSchemaToNeo4J;
import cs.utils.ConfigManager;
import cs.utils.Constants;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.engine.constraint.*;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.PropertyShape;
import org.apache.jena.shacl.parser.Shape;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Translate SHACL shapes schema to Property Graph Schema (PG-Schema)
 */
public class SchemaTranslator {
    ResourceEncoder resourceEncoder;
    PgSchema pgSchema;

    public SchemaTranslator(ResourceEncoder encoder) {
        resourceEncoder = encoder;
        pgSchema = new PgSchema();
        Shapes shapes = readShapes();
        parseShapes(shapes);
        pgSchema.postProcessPgSchema();
        convertToNeo4jQueries();
    }

    private void convertToNeo4jQueries() {
        PgSchemaToNeo4J pgSchemaToNeo4J = new PgSchemaToNeo4J(resourceEncoder, pgSchema);
        pgSchemaToNeo4J.generateCypherQueries();
        pgSchemaToNeo4J.writePgSchemaCypherQueriesToFile();
        //pgSchemaToNeo4J.executeQueriesOverNeo4j();
    }

    private Shapes readShapes() {
        Model shapesModel = Reader.readFileToModel(ConfigManager.getProperty("shapes_path"), "TURTLE");
        Main.logger.info(String.valueOf(shapesModel.size()));
        Shapes shapes = Shapes.parse(shapesModel);
        extractShNodeFromShaclShapes(shapesModel);
        return shapes;
    }

    //* Parse SHACL shapes and create PG-Schema
    private void parseShapes(Shapes shapes) {
        //TODO: Sometimes, node shapes do not have target, s we need to check if the target is null or not
        for (Shape t : shapes.getTargetShapes()) {
            //TODO: for hierarchies, find out all shacl shapes having sh:node and use their values to build hierarchies
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
                // ******   cardinality constraints
                case "MinCount" -> {
                    Integer minCount = ((MinCount) constraint).getMinCount();
                    pgEdge.setMinCount(minCount);
                }
                case "MaxCount" -> {
                    Integer maxCount = ((MaxCount) constraint).getMaxCount();
                    pgEdge.setMaxCount(maxCount);
                }

                //******  node kind constraints : IRI or Literal ******

                case "ClassConstraint" -> { //    sh:NodeKind sh:IRI ;     sh:class    ex:University or any other class IRI
                    parseClassConstraint(pgNode, pgEdge, (ClassConstraint) constraint);
                }
                case "DatatypeConstraint" -> { //    sh:NodeKind sh:Literal ;     sh:datatype xsd:string or any other primitive data type
                    Node dataTypeConstraint = ((DatatypeConstraint) constraint).getDatatype();
                    pgEdge.setDataType(dataTypeConstraint.getLocalName());
                    pgEdge.setLiteral(true);
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
                        System.out.println("Heterogeneous (Literal and IRI) Multi Type Constraint");
                        datatypeConstraints.forEach(dtConstraint -> { //create a new node for each datatype constraint
                            Node dataTypeConstraint = dtConstraint.getDatatype();
                            dataTypeConstraint.getLocalName();

                            PgNode dtPgNode = null;
                            int dtPgNodeEncoded = resourceEncoder.encodeAsResource(dataTypeConstraint.getURI());
                            if (pgSchema.getNodesToEdges().containsKey(dtPgNodeEncoded)) {
                                dtPgNode = PgNode.getNodeById(dtPgNodeEncoded);
                            } else {
                                dtPgNode = new PgNode(dtPgNodeEncoded);
                                pgSchema.addNode(dtPgNode);
                            }
                            //TODO: Set the type of the PG Node as Abstract
                            dtPgNode.setType(dataTypeConstraint.getLocalName());
                            pgSchema.addTargetEdge(pgNode, pgEdge, dtPgNode);
                        });
                        classConstraints.forEach(classConstraint -> parseClassConstraint(pgNode, pgEdge, classConstraint));

                    } else if (classConstraints.size() > 0) { // Homogeneous (IRI) Multi Type Constraint
                        System.out.println("Homogeneous (IRI) Multi Type Constraint");
                        classConstraints.forEach(classConstraint -> parseClassConstraint(pgNode, pgEdge, classConstraint));
                    } else if (datatypeConstraints.size() > 0) { // Homogeneous (Literal) Multi Type Constraint
                        System.out.println("Homogeneous (Literal) Multi Type Constraint");
                        //TODO: Solution 1: Create a new node for each datatype constraint

                        //TODO: Solution 2: Create edge with datatype constraint

                    }
                }

                case "ShNode" -> {
                    ShNode shNode = ((ShNode) constraint);
                }

                /*case "InConstraint" -> { //FIXME : Do you need this?
                    Node inConstraint = ((InConstraint) constraint).getComponent();
                }*/
                default -> {
                    //System.out.println("Default case: unhandled constraint: " + constraint);
                }
            }
        }

        if (pgEdge.getMinCount() != null && pgEdge.getMaxCount() != null) {
            boolean status = pgEdge.handlePropertyType();
            if (status) {
                pgNode.addPropId(pgEdge.getId());
            }
        }
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

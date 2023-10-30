package cs.utils;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;

public class RDFComparison {
    public static void main(String[] args) {
        String file_v0 = "/Users/kashifrabbani/Documents/GitHub/KG2PG/data/monotone/runningExampleGraph_v0.nt";
        String file_v1 = "/Users/kashifrabbani/Documents/GitHub/KG2PG/data/monotone/runningExampleGraph_v1.nt";

        // Create two models for the RDF data
        Model modelA = FileManager.get().loadModel(file_v0, "N-TRIPLES");
        Model modelB = FileManager.get().loadModel(file_v1, "N-TRIPLES");

        // Compare the models and print the differences
        Model differenceAvsB = modelA.difference(modelB);
        Model differenceBvsA = modelB.difference(modelA);

        // Create models to store added, deleted, and updated triples in file B and A
        Model addedTriplesB = ModelFactory.createDefaultModel();
        Model deletedTriplesB = ModelFactory.createDefaultModel();
        Model updatedTriplesB = ModelFactory.createDefaultModel();
        //Model addedTriplesA = ModelFactory.createDefaultModel();
        //Model deletedTriplesA = ModelFactory.createDefaultModel();
        Model updatedTriplesA = ModelFactory.createDefaultModel();

        // Iterate through differenceBvsA to find added, deleted, and updated triples in file B
        StmtIterator iterBvsA = differenceBvsA.listStatements();
        while (iterBvsA.hasNext()) {
            Statement stmtBvsA = iterBvsA.nextStatement();
            Statement stmtAvsB = differenceAvsB.getProperty(stmtBvsA.getSubject(), stmtBvsA.getPredicate());

            if (stmtAvsB == null) {
                // Triple in differenceBvsA but not in differenceAvsB (added in file B)
                addedTriplesB.add(stmtBvsA);
            } else {
                // Triple in both differenceBvsA and differenceAvsB (possibly updated in file B)
                if (!stmtAvsB.getObject().equals(stmtBvsA.getObject())) {
                    // Object values are different, indicating an update
                    updatedTriplesB.add(stmtBvsA);
                }
            }
        }

        // Iterate through differenceAvsB to find deleted triples in file B
        StmtIterator iterAvsB = differenceAvsB.listStatements();
        while (iterAvsB.hasNext()) {
            Statement stmtAvsB = iterAvsB.nextStatement();
            Statement stmtBvsA = differenceBvsA.getProperty(stmtAvsB.getSubject(), stmtAvsB.getPredicate());

            if (stmtBvsA == null) {
                // Triple in differenceAvsB but not in differenceBvsA (deleted in file B)
                deletedTriplesB.add(stmtAvsB);
            }
        }

        // Iterate through differenceAvsB to find added, deleted, and updated triples in file A
        StmtIterator iterAvsA = differenceAvsB.listStatements();
        while (iterAvsA.hasNext()) {
            Statement stmtAvsA = iterAvsA.nextStatement();
            Statement stmtBvsA = differenceBvsA.getProperty(stmtAvsA.getSubject(), stmtAvsA.getPredicate());

            if (stmtBvsA == null) {
                // Triple in differenceAvsB but not in differenceBvsA (added in file A)
                //addedTriplesA.add(stmtAvsA);
            } else {
                // Triple in both differenceAvsB and differenceBvsA (possibly updated in file A)
                if (!stmtBvsA.getObject().equals(stmtAvsA.getObject())) {
                    // Object values are different, indicating an update
                    updatedTriplesA.add(stmtAvsA);
                }
            }
        }

        // Iterate through differenceBvsA to find deleted triples in file A
//        StmtIterator iterBvsAforA = differenceBvsA.listStatements();
//        while (iterBvsAforA.hasNext()) {
//            Statement stmtBvsAforA = iterBvsAforA.nextStatement();
//            Statement stmtAvsB = differenceAvsB.getProperty(stmtBvsAforA.getSubject(), stmtBvsAforA.getPredicate());
//
//            if (stmtAvsB == null) {
//                // Triple in differenceBvsA but not in differenceAvsB (deleted in file A)
//                deletedTriplesA.add(stmtBvsAforA);
//            }
//        }

        // Print or process added, deleted, and updated triples in files A and B
       // System.out.println("Added Triples in File A:");
        //addedTriplesA.write(System.out, "N-TRIPLES");
        //System.out.println("Deleted Triples in File A:");
        //deletedTriplesA.write(System.out, "N-TRIPLES");


        System.out.println("Added Triples in File B");
        addedTriplesB.write(System.out, "N-TRIPLES");
        System.out.println("Deleted Triples in File B wrt A");
        deletedTriplesB.write(System.out, "N-TRIPLES");
        System.out.println("Updated Triples in File B wrt A with New Values:");
        updatedTriplesB.write(System.out, "N-TRIPLES");

        System.out.println("Updated Triples in File B wrt A with Old Values:");
        updatedTriplesA.write(System.out, "N-TRIPLES");
    }
}

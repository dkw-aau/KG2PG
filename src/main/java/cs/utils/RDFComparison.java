package cs.utils;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RDFComparison {

    public RDFComparison(String file_v0, String file_v1) {
        findDiff(file_v0, file_v1);
    }

    public void findDiff(String file_v0, String file_v1) {
        // Create a model for each file
        Model modelA = ModelFactory.createDefaultModel();
        Model modelB = ModelFactory.createDefaultModel();

        // Use streaming to load the N-Triples files
        try {
            RDFDataMgr.read(modelA, new FileInputStream(file_v0), Lang.NTRIPLES);
            RDFDataMgr.read(modelB, new FileInputStream(file_v1), Lang.NTRIPLES);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


        // Create two models for the RDF data
        //Model modelA = FileManager.get().loadModel(file_v0, "N-TRIPLES");
        //Model modelB = FileManager.get().loadModel(file_v1, "N-TRIPLES");

        // Compare the models and print the differences
        Model differenceAvsB = modelA.difference(modelB);
        Model differenceBvsA = modelB.difference(modelA);

        // Create models to store added, deleted, and updated triples in file B and A
        Model addedTriplesB = ModelFactory.createDefaultModel();
        Model deletedTriplesB = ModelFactory.createDefaultModel();
        Model updatedTriplesB = ModelFactory.createDefaultModel();
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
                    updatedTriplesA.add(stmtAvsB); // Store the updated triple from file A
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

        // Define output file paths for added, deleted, and updated triples in File B
        String addedTriplesBPath = ConfigManager.getProperty("output_file_path") + "addedTriplesB.nt";
        String deletedTriplesBPath = ConfigManager.getProperty("output_file_path") + "deletedTriplesB.nt";
        String updatedTriplesBPath = ConfigManager.getProperty("output_file_path") + "updatedTriplesB.nt";
        String updatedTriplesAPath = ConfigManager.getProperty("output_file_path") + "updatedTriplesA.nt";

        // Write added, deleted, and updated triples to separate output files
        try {
            FileOutputStream addedTriplesBFile = new FileOutputStream(addedTriplesBPath);
            FileOutputStream deletedTriplesBFile = new FileOutputStream(deletedTriplesBPath);
            FileOutputStream updatedTriplesBFile = new FileOutputStream(updatedTriplesBPath);
            FileOutputStream updatedTriplesAFile = new FileOutputStream(updatedTriplesAPath);

            addedTriplesB.write(addedTriplesBFile, "N-TRIPLES");
            System.out.println("Added Triples in File B written to " + addedTriplesBPath);
            deletedTriplesB.write(deletedTriplesBFile, "N-TRIPLES");
            System.out.println("Deleted Triples in File B wrt A written to " + deletedTriplesBPath);
            updatedTriplesB.write(updatedTriplesBFile, "N-TRIPLES");
            System.out.println("Updated Triples in File B wrt A with New Values written to " + updatedTriplesBPath);
            updatedTriplesA.write(updatedTriplesAFile, "N-TRIPLES");
            System.out.println("Updated Triples in File B wrt A with Old Values written to " + updatedTriplesAPath);

            addedTriplesBFile.close();
            deletedTriplesBFile.close();
            updatedTriplesBFile.close();
            updatedTriplesAFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

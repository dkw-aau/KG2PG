package cs.utils.graphdb;

import org.eclipse.rdf4j.common.app.AppConfiguration;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPQueryEvaluationException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class helps in querying GraphDB
 */
public class GraphDBUtils {
    KBManagement kbManager;
    Repository repository;
    RepositoryConnection repositoryConnection;
    
    public GraphDBUtils() {
        this.kbManager = new KBManagement();
        this.repository = kbManager.initGraphDBRepository();
        this.repositoryConnection = repository.getConnection();
        AppConfiguration appConfiguration = new AppConfiguration();
    }
    
    public GraphDBUtils(String repositoryUrl, String repositoryName) {
        this.kbManager = new KBManagement();
        this.repository = kbManager.initGraphDBRepository(repositoryUrl, repositoryName);
        this.repositoryConnection = repository.getConnection();
        AppConfiguration appConfiguration = new AppConfiguration();
    }
    
    public ValueFactory getValueFactory() {
        return repositoryConnection.getValueFactory();
    }
    
    public List<BindingSet> runSelectQuery(String query) {
        List<BindingSet> result = new ArrayList<>();
        try {
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult classesQueryResult = tupleQuery.evaluate();
            classesQueryResult.forEach(result::add);
            classesQueryResult.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (repositoryConnection.isActive())
                repositoryConnection.rollback();
        }
        return result;
    }
    
    public int runSelectQueryCountOutputRows(String query) {
        int counter = 0;
        try {
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            tupleQuery.setMaxExecutionTime(600);
            try {
                TupleQueryResult classesQueryResult = tupleQuery.evaluate();
                counter = (int) classesQueryResult.stream().count();
                classesQueryResult.close();
            } catch (HTTPQueryEvaluationException exception) {
                System.out.println(exception.getLocalizedMessage());
                counter = -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (repositoryConnection.isActive())
                repositoryConnection.rollback();
        }
        return counter;
    }
    
    public Boolean runAskQuery(String query) {
        Boolean result = null;
        try {
            BooleanQuery queryResult = repositoryConnection.prepareBooleanQuery(query);
            result = queryResult.evaluate();
        } catch (Exception e) {
            e.printStackTrace();
            if (repositoryConnection.isActive())
                repositoryConnection.rollback();
        }
        return result;
    }
    
    public void runConstructQuery(String query, String address) {
        try {
            GraphQuery queryResult = repositoryConnection.prepareGraphQuery(query);
            GraphQueryResult resultantTriples = queryResult.evaluate();
            
            FileWriter fileWriter = new FileWriter(address, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            resultantTriples.forEach(statement -> {
                printWriter.println("<" + statement.getSubject() + "> <" + statement.getPredicate() + "> <" + statement.getObject() + "> .");
            });
            
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (repositoryConnection.isActive())
                repositoryConnection.rollback();
        }
    }
    
    public void runGraphQuery(String query, String address) {
        try {
            GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, query);
            
            GraphQueryResult graphQueryResult = graphQuery.evaluate();
            
            OutputStream out = new FileOutputStream(address, true);
            RDFWriter writer = new NTriplesWriter(out);
            graphQuery.evaluate(writer);
            
            graphQueryResult.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (repositoryConnection.isActive())
                repositoryConnection.rollback();
        }
    }
    
    public List<BindingSet> runSelectQueryWithTimeOut(String query) {
        List<BindingSet> result = new ArrayList<>();
        try {
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            tupleQuery.setMaxExecutionTime(300);
            TupleQueryResult classesQueryResult = tupleQuery.evaluate();
            classesQueryResult.forEach(result::add);
            classesQueryResult.close();
        } catch (Exception e) {
            if (repositoryConnection.isActive())
                repositoryConnection.rollback();
        }
        return result;
    }
    
    public void updateQueryExecutor(String query) {
        try {
            repositoryConnection.begin();
            Update updateOperation = repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, query);
            updateOperation.execute();
            repositoryConnection.commit();
            //repositoryConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (repositoryConnection.isActive())
                repositoryConnection.rollback();
        }
    }
}
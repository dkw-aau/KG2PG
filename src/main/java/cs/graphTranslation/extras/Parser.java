//package cs.graphTranslation.utils;
//
//import cs.graphTranslation.utils.GraphTransUtils;
//import cs.utils.ConfigManager;
//import n10s.CommonProcedures;
//import n10s.ConfiguredStatementHandler;
//import n10s.RDFImportException;
//import n10s.RDFToLPGStatementProcessor;
//import n10s.graphconfig.GraphConfig;
//import n10s.graphconfig.RDFParserConfig;
//import n10s.rdf.stream.StarFormatStatementStreamer;
//import n10s.rdf.stream.StatementStreamer;
//import n10s.result.StreamedStatement;
//import org.apache.commons.compress.archivers.ArchiveEntry;
//import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
//import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
//import org.apache.commons.io.IOUtils;
//import org.eclipse.rdf4j.model.*;
//import org.eclipse.rdf4j.model.vocabulary.RDF;
//import org.eclipse.rdf4j.rio.*;
//import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
//import org.eclipse.rdf4j.rio.helpers.StatementCollector;
//import org.neo4j.graphdb.QueryExecutionException;
//import org.neo4j.procedure.Name;
//
//import java.io.*;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.net.URLConnection;
//import java.nio.charset.Charset;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//import java.util.stream.Stream;
//import java.util.zip.GZIPInputStream;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipInputStream;
//import java.net.*;
//
//import static n10s.graphconfig.GraphConfig.GRAPHCONF_RDFTYPES_AS_LABELS;
//import static n10s.graphconfig.GraphConfig.GRAPHCONF_RDFTYPES_AS_LABELS_AND_NODES;
//
//public class Parser {
//
//
//
//    public Parser() {
//
//        streamer();
//    }
//
//    private void streamer() {
//        Map<String, Object> props = new HashMap<>();
//        cs.graphTranslation.utils.RDFParserConfig conf = new cs.graphTranslation.utils.RDFParserConfig(props);
//        GraphTransUtils graphTransUtils = new GraphTransUtils(conf);
//
//
//        // Create an RDFParser for the chosen format
//        RDFParser rdfParser = Rio.createParser(RDFFormat.NTRIPLES);
//
//        // Configure the RDFParser (optional)
//
//        // Create a StatementCollector to collect parsed RDF statements
//        StatementCollector collector = new StatementCollector();
//        rdfParser.setRDFHandler(collector);
//        // Provide RDF data as an InputStream (e.g., from a file)
//        try (InputStream inputStream = new FileInputStream(Objects.requireNonNull(ConfigManager.getProperty("dataset_path")))) {
//            // Parse RDF data from the input stream
//            rdfParser.parse(inputStream, "");
//            // Get the parsed RDF statements
//            collector.getStatements().forEach(statement -> {
//                // Do something interesting with the statement here...
//                //System.out.println(statement.toString());
//                graphTransUtils.handleStatement(statement);
//            });
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        graphTransUtils.statements.forEach(statement -> {
//            System.out.println(statement.toString());
//        });
//
//    }
//
//
//
//}

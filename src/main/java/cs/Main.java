package cs;

import cs.schemaTranslation.SchemaTranslator;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class Main {
    public static String configPath;
    public static Logger logger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

    public static void main(String[] args) {
        configPath = args[0];
        logger.setLevel(Level.INFO);
        //schema translation
        new SchemaTranslator();
        //graph data translation
    }
}
package uk.ac.cam.cares.jps.util;

import com.cmclinnovations.stack.clients.blazegraph.BlazegraphEndpointConfig;
import com.cmclinnovations.stack.clients.docker.ContainerClient;
import com.cmclinnovations.stack.clients.postgis.PostGISEndpointConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.cam.cares.jps.DataBridgeAgent;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A client that retrieve configuration files.
 *
 * @author qhouyee
 */
public class ConfigStore {
    private static final Logger LOGGER = LogManager.getLogger(DataBridgeAgent.class);
    private static final String NO_PROPERTIES_MSG = "No endpoint.properties file detected! Please place the file in the config directory.";
    private static final String INACCESSIBLE_CLIENT_PROPERTIES_MSG = "File could not be accessed! See error message for more details: ";
    private static final String PROPERTIES_FILEPATH = System.getProperty("user.dir") + "/config/endpoint.properties";
    private static final String VAL_TRANSFER_IN = "in";
    private static final String VAL_TRANSFER_OUT = "out";
    private static final String SRC_SPARQL_ENDPOINT = "sparql.src.endpoint";
    private static final String SRC_DB_URL = "src.db.url";
    private static final String SRC_DB_USER = "src.db.user";
    private static final String SRC_DB_PASSWORD = "src.db.password";
    private static final String TARGET_SPARQL_ENDPOINT = "sparql.target.endpoint";
    private static final String TARGET_DB_URL = "target.db.url";
    private static final String TARGET_DB_USER = "target.db.user";
    private static final String TARGET_DB_PASSWORD = "target.db.password";


    /**
     * An overloaded method for non-stack transfers to retrieve the SQL database properties stored in the properties file.
     *
     * @return An array of these endpoints.
     */
    public static String[] retrieveSQLConfig() {
        return retrieveSQLConfig(null, "");
    }

    /**
     * Retrieves SQL database properties stored in the properties file or as a stack database.
     *
     * @param stackDatabase The stack database name passed as a parameter to the GET request.
     * @return An array of these endpoints.
     */
    public static String[] retrieveSQLConfig(String stackDatabase, String transferKey) {
        StringBuilder missingPropertiesErrorMessage = new StringBuilder();
        try (InputStream input = new FileInputStream(PROPERTIES_FILEPATH)) {
            Properties prop = new Properties();
            String[] config = new String[6];
            LOGGER.debug("Retrieving configuration from " + PROPERTIES_FILEPATH + "...");
            prop.load(input);
            if (transferKey.isEmpty()) {
                LOGGER.info("Retrieving the details for source and target databases from file...");
                config[0] = validateProperties(prop, SRC_DB_URL, missingPropertiesErrorMessage);
                config[1] = validateProperties(prop, SRC_DB_USER, missingPropertiesErrorMessage);
                config[2] = validateProperties(prop, SRC_DB_PASSWORD, missingPropertiesErrorMessage);
                config[3] = validateProperties(prop, TARGET_DB_URL, missingPropertiesErrorMessage);
                config[4] = validateProperties(prop, TARGET_DB_USER, missingPropertiesErrorMessage);
                config[5] = validateProperties(prop, TARGET_DB_PASSWORD, missingPropertiesErrorMessage);
            } else if (transferKey.equals(VAL_TRANSFER_IN)) {
                LOGGER.info("Retrieving the details for source database from file...");
                config[0] = validateProperties(prop, SRC_DB_URL, missingPropertiesErrorMessage);
                config[1] = validateProperties(prop, SRC_DB_USER, missingPropertiesErrorMessage);
                config[2] = validateProperties(prop, SRC_DB_PASSWORD, missingPropertiesErrorMessage);
                LOGGER.info("Retrieving the details for the stack database to be targeted...");
                retrieveDatabase(stackDatabase, config, false);
            } else if (transferKey.equals(VAL_TRANSFER_OUT)) {
                LOGGER.info("Retrieving the details for the stack database as a source...");
                retrieveDatabase(stackDatabase, config, true);
                LOGGER.info("Retrieving the details for target database from file...");
                config[3] = validateProperties(prop, TARGET_DB_URL, missingPropertiesErrorMessage);
                config[4] = validateProperties(prop, TARGET_DB_USER, missingPropertiesErrorMessage);
                config[5] = validateProperties(prop, TARGET_DB_PASSWORD, missingPropertiesErrorMessage);
            }
            String missingMessage = missingPropertiesErrorMessage.toString();
            if (!missingMessage.isEmpty()) {
                LOGGER.error("Missing Properties:\n" + missingMessage);
                throw new JPSRuntimeException("Missing Properties:\n" + missingMessage);
            }
            LOGGER.info("All required configurations have been retrieved!");
            return config;
        } catch (FileNotFoundException e) {
            LOGGER.error(NO_PROPERTIES_MSG);
            throw new JPSRuntimeException(NO_PROPERTIES_MSG);
        } catch (IOException e) {
            LOGGER.error(INACCESSIBLE_CLIENT_PROPERTIES_MSG + e);
            throw new JPSRuntimeException(INACCESSIBLE_CLIENT_PROPERTIES_MSG + e);
        }
    }

    /**
     * Retrieves the SQL database within this stack as either the source or target.
     *
     * @param stackDatabase The stack database name passed as a parameter to the GET request.
     * @param config        The configuration array to store the results.
     * @param isSource      Indicates if the stack database is the source or target.
     */
    private static void retrieveDatabase(String stackDatabase, String[] config, boolean isSource) {
        ContainerClient client = new ContainerClient();
        PostGISEndpointConfig postConfig = client.readEndpointConfig("postgis", PostGISEndpointConfig.class);
        if (isSource) {
            config[0] = postConfig.getJdbcURL(stackDatabase);
            config[1] = postConfig.getUsername();
            config[2] = postConfig.getPassword();
        } else {
            config[3] = postConfig.getJdbcURL(stackDatabase);
            config[4] = postConfig.getUsername();
            config[5] = postConfig.getPassword();
        }
    }

    /**
     * An overloaded method for non-stack transfers to retrieve SPARQL endpoints stored in the properties file.
     *
     * @return An array of these endpoints.
     */
    public static String[] retrieveSPARQLConfig() {
        return retrieveSPARQLConfig(null, "");
    }

    /**
     * Retrieves SPARQL endpoints stored in the properties file or as a stack endpoint.
     *
     * @param stackNamespace The stack namespace endpoint passed as a parameter to the GET request.
     * @param transferKey    The value of the transfer parameter to the GET request.
     * @return An array of these endpoints.
     */
    public static String[] retrieveSPARQLConfig(String stackNamespace, String transferKey) {
        StringBuilder missingPropertiesErrorMessage = new StringBuilder();
        try (InputStream input = new FileInputStream(PROPERTIES_FILEPATH)) {
            Properties prop = new Properties();
            String[] config = new String[2];
            LOGGER.debug("Retrieving configuration from " + PROPERTIES_FILEPATH + "...");
            prop.load(input);
            if (transferKey.isEmpty()) {
                LOGGER.info("Retrieving source and target endpoints from file...");
                config[0] = validateProperties(prop, SRC_SPARQL_ENDPOINT, missingPropertiesErrorMessage);
                config[1] = validateProperties(prop, TARGET_SPARQL_ENDPOINT, missingPropertiesErrorMessage);
            } else if (transferKey.equals(VAL_TRANSFER_IN)) {
                LOGGER.info("Retrieving source endpoint from file...");
                config[0] = validateProperties(prop, SRC_SPARQL_ENDPOINT, missingPropertiesErrorMessage);
                LOGGER.info("Retrieving stack endpoint for target...");
                config[1] = retrieveStackEndpoint(stackNamespace);
            } else if (transferKey.equals(VAL_TRANSFER_OUT)) {
                LOGGER.info("Retrieving stack endpoint for source...");
                config[0] = retrieveStackEndpoint(stackNamespace);
                LOGGER.info("Retrieving target endpoint from file...");
                config[1] = validateProperties(prop, TARGET_SPARQL_ENDPOINT, missingPropertiesErrorMessage);
            }
            String missingMessage = missingPropertiesErrorMessage.toString();
            if (!missingMessage.isEmpty()) {
                LOGGER.error("Missing Properties:\n" + missingMessage);
                throw new JPSRuntimeException("Missing Properties:\n" + missingMessage);
            }
            LOGGER.info("All required configurations have been retrieved!");
            return config;
        } catch (FileNotFoundException e) {
            LOGGER.error(NO_PROPERTIES_MSG);
            throw new JPSRuntimeException(NO_PROPERTIES_MSG);
        } catch (IOException e) {
            LOGGER.error(INACCESSIBLE_CLIENT_PROPERTIES_MSG + e);
            throw new JPSRuntimeException(INACCESSIBLE_CLIENT_PROPERTIES_MSG + e);
        }
    }

    /**
     * Retrieves the SPARQL endpoint within this stack.
     *
     * @param stackNamespace The stack namespace endpoint passed as a parameter to the GET request.
     */
    private static String retrieveStackEndpoint(String stackNamespace) {
        ContainerClient client = new ContainerClient();
        BlazegraphEndpointConfig blazeConfig = client.readEndpointConfig("blazegraph", BlazegraphEndpointConfig.class);
        return blazeConfig.getServiceUrl() + "/namespace/" + stackNamespace + "/sparql";
    }

    /**
     * Validates the client properties, and return their value if it exists.
     *
     * @param prop                          A Properties object containing the required properties.
     * @param propertyKey                   The property key associated with the value.
     * @param missingPropertiesErrorMessage An error message that will be written if there is no property.
     * @return The value of the endpoints.
     */
    private static String validateProperties(Properties prop, String propertyKey, StringBuilder missingPropertiesErrorMessage) {
        if (prop.getProperty(propertyKey) == null || prop.getProperty(propertyKey).isEmpty()) {
            missingPropertiesErrorMessage.append(propertyKey + " is missing! Please add the input to endpoint.properties.\n");
            LOGGER.error(propertyKey + " is missing! Please add the input to endpoint.properties.");
        } else {
            return prop.getProperty(propertyKey);
        }
        return "";
    }

    public static String[] retrieveTSClientConfig(String stackNamespace, String stackDatabase) {
        LOGGER.info("Retrieveing TS Client parameters");
        String[] config = new String[7];
        String[] dbConfig;
        String[] kbConfig;

        if (stackDatabase == null) {
            dbConfig = retrieveSQLConfig();
        } else {
            dbConfig = retrieveSQLConfig(stackDatabase, "in");
        }

        if (stackNamespace == null) {
            kbConfig = retrieveSPARQLConfig();
        } else {
            kbConfig = retrieveSPARQLConfig(stackNamespace, "in");
        }

        config[0] = dbConfig[4]; //dbusrName
        config[1] = dbConfig[5]; //dbPassword
        config[2] = dbConfig[3]; //dbURL
        config[3] = null; //bgUsrName -- is this needed?
        config[4] = null; //bgPassword -- is this needed?
        config[5] = kbConfig[1]; //updateEndpoiunt
        config[6] = kbConfig[1]; //query Enpoint - Assume the same?
        LOGGER.debug("CONFIG: " + config);
        return config;
    }
}

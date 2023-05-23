package uk.ac.cam.cares.jps.util;

import org.junit.jupiter.api.Test;
import uk.ac.cam.cares.jps.TestConfigUtils;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ConfigStoreTest {
    private static final String srcSparql = "http://localhost:9999/blazegraph/namespace/test/sparql";
    private static final String targetSparql = "http://host.docker.internal:9999/blazegraph/namespace/target/sparql";
    private static final String srcDb = "jdbc:postgresql://localhost:5432/db";
    private static final String srcUser = "postgres";
    private static final String srcPass = "pass1";
    private static final String tgtDb = "jdbc:postgresql://host.docker.internal:5432/db";
    private static final String tgtUser = "user";
    private static final String tgtPass = "pass2";
    private static final String securedSrcSparql = "http://" + srcUser + ":" + srcPass + "@localhost:9999/blazegraph/namespace/test/sparql";
    private static final String securedTargetSparql = "http://" + tgtUser + ":" + tgtPass + "@host.docker.internal:9999/blazegraph/namespace/target/sparql";

    @Test
    void testRetrieveSPARQLConfigNoFile() {
        // Verify right exception is thrown
        JPSRuntimeException thrownError = assertThrows(JPSRuntimeException.class, ConfigStore::retrieveSPARQLConfig);
        assertEquals("No endpoint.properties file detected! Please place the file in the config directory.", thrownError.getMessage());
    }

    @Test
    void testOverloadedRetrieveSPARQLConfigMissingInputs() throws IOException {
        File config = TestConfigUtils.genSampleSPARQLConfigFile(true, srcSparql, targetSparql);
        try {
            // Execute method and ensure right error is thrown
            JPSRuntimeException thrownError = assertThrows(JPSRuntimeException.class, ConfigStore::retrieveSPARQLConfig);
            assertEquals("Missing Properties:\n" +
                    "sparql.src.endpoint is missing! Please add the input to endpoint.properties.\n", thrownError.getMessage());
        } finally {
            // Always delete generated config file
            config.delete();
        }
    }

    @Test
    void testOverloadedRetrieveSPARQLConfigEmptyTargetProperties() throws IOException {
        File config = TestConfigUtils.genSampleSPARQLConfigFile(false, srcSparql, "");
        try {
            // Execute method and ensure right error is thrown
            JPSRuntimeException thrownError = assertThrows(JPSRuntimeException.class, ConfigStore::retrieveSPARQLConfig);
            assertEquals("Missing Properties:\n" +
                    "sparql.target.endpoint is missing! Please add the input to endpoint.properties.\n", thrownError.getMessage());
        } finally {
            // Always delete generated config file
            config.delete();
        }
    }

    @Test
    void testOverloadedRetrieveSPARQLConfig() throws IOException {
        File config = TestConfigUtils.genSampleSPARQLConfigFile(false, srcSparql, targetSparql);
        try {
            // Execute method
            String[] result = ConfigStore.retrieveSPARQLConfig();
            // Verify results are expected
            assertEquals(srcSparql, result[0]);
            assertEquals(targetSparql, result[1]);
        } finally {
            // Always delete generated config file
            config.delete();
        }
    }

    @Test
    void testOverloadedRetrieveSPARQLConfigWithCredentials() throws IOException {
        File config = TestConfigUtils.genSampleSPARQLConfigFile(false, srcSparql, targetSparql, srcUser, srcPass, tgtUser, tgtPass);
        try {
            // Execute method
            String[] result = ConfigStore.retrieveSPARQLConfig();
            // Verify results are expected
            assertEquals(securedSrcSparql, result[0]);
            assertEquals(securedTargetSparql, result[1]);
        } finally {
            // Always delete generated config file
            config.delete();
        }
    }

    @Test
    void testRetrieveSPARQLConfigEmptyTargetProperties() throws IOException {
        File config = TestConfigUtils.genSampleSPARQLConfigFile(false, srcSparql, "");
        try {
            // Execute method and ensure right error is thrown
            JPSRuntimeException thrownError = assertThrows(JPSRuntimeException.class, () -> ConfigStore.retrieveSPARQLConfig(null, ""));
            assertEquals("Missing Properties:\n" +
                    "sparql.target.endpoint is missing! Please add the input to endpoint.properties.\n", thrownError.getMessage());
        } finally {
            // Always delete generated config file
            config.delete();
        }
    }

    @Test
    void testRetrieveSPARQLConfig() throws IOException {
        File config = TestConfigUtils.genSampleSPARQLConfigFile(false, srcSparql, targetSparql);
        try {
            // Execute method
            String[] result = ConfigStore.retrieveSPARQLConfig(null, "");
            // Verify results are expected
            assertEquals(srcSparql, result[0]);
            assertEquals(targetSparql, result[1]);
        } finally {
            // Always delete generated config file
            config.delete();
        }
    }

    @Test
    void testRetrieveSPARQLConfigWithCredentials() throws IOException {
        File config = TestConfigUtils.genSampleSPARQLConfigFile(false, srcSparql, targetSparql, srcUser, srcPass, "", "");
        try {
            // Execute method
            String[] result = ConfigStore.retrieveSPARQLConfig(null, "");
            // Verify results are expected
            assertEquals(securedSrcSparql, result[0]);
            assertEquals(targetSparql, result[1]); // If no optional values are provided, it should return non-secured endpoint
        } finally {
            // Always delete generated config file
            config.delete();
        }
    }

    @Test
    void testRetrieveSQLConfigNoFile() {
        // Verify right exception is thrown
        JPSRuntimeException thrownError = assertThrows(JPSRuntimeException.class, ConfigStore::retrieveSQLConfig);
        assertEquals("No endpoint.properties file detected! Please place the file in the config directory.", thrownError.getMessage());
    }

    @Test
    void testOverloadedRetrieveSQLConfigMissingSourceInputs() throws IOException {
        File config = TestConfigUtils.genSampleSQLConfigFile(false, srcDb, srcUser, srcPass, tgtDb, tgtUser, tgtPass);
        try {
            // Execute method and ensure right error is thrown
            JPSRuntimeException thrownError = assertThrows(JPSRuntimeException.class, ConfigStore::retrieveSQLConfig);
            assertEquals("Missing Properties:\n" +
                    "src.db.user is missing! Please add the input to endpoint.properties.\n" +
                    "src.db.password is missing! Please add the input to endpoint.properties.\n", thrownError.getMessage());
        } finally {
            // Always delete generated config file
            config.delete();
        }
    }

    @Test
    void testOverloadedRetrieveSQLConfig() throws IOException {
        File config = TestConfigUtils.genSampleSQLConfigFile(true, srcDb, srcUser, srcPass, tgtDb, tgtUser, tgtPass);
        try {
            // Execute method
            String[] result = ConfigStore.retrieveSQLConfig();
            // Verify results are expected
            assertEquals(srcDb, result[0]);
            assertEquals(srcUser, result[1]);
            assertEquals(srcPass, result[2]);
            assertEquals(tgtDb, result[3]);
            assertEquals(tgtUser, result[4]);
            assertEquals(tgtPass, result[5]);
        } finally {
            // Always delete generated config file
            config.delete();
        }
    }

    @Test
    void testRetrieveSQLConfigEmptyTargetUrlProperties() throws IOException {
        File config = TestConfigUtils.genSampleSQLConfigFile(false, srcDb, srcUser, srcPass, "", "", "");
        try {
            // Execute method and ensure right error is thrown
            JPSRuntimeException thrownError = assertThrows(JPSRuntimeException.class, () -> ConfigStore.retrieveSQLConfig(null, ""));
            assertEquals("Missing Properties:\n" +
                    "src.db.user is missing! Please add the input to endpoint.properties.\n" +
                    "src.db.password is missing! Please add the input to endpoint.properties.\n" +
                    "target.db.url is missing! Please add the input to endpoint.properties.\n" +
                    "target.db.user is missing! Please add the input to endpoint.properties.\n" +
                    "target.db.password is missing! Please add the input to endpoint.properties.\n", thrownError.getMessage());
        } finally {
            // Always delete generated config file
            config.delete();
        }
    }

    @Test
    void testRetrieveSQLConfigEmptyTargetUserProperties() throws IOException {
        File config = TestConfigUtils.genSampleSQLConfigFile(false, srcDb, srcUser, srcPass, tgtDb, "", "");
        try {
            // Execute method and ensure right error is thrown
            JPSRuntimeException thrownError = assertThrows(JPSRuntimeException.class, () -> ConfigStore.retrieveSQLConfig(null, ""));
            assertEquals("Missing Properties:\n" +
                    "src.db.user is missing! Please add the input to endpoint.properties.\n" +
                    "src.db.password is missing! Please add the input to endpoint.properties.\n" +
                    "target.db.user is missing! Please add the input to endpoint.properties.\n" +
                    "target.db.password is missing! Please add the input to endpoint.properties.\n", thrownError.getMessage());
        } finally {
            // Always delete generated config file
            config.delete();
        }
    }

    @Test
    void testRetrieveSQLConfigEmptyTargetPassProperties() throws IOException {
        File config = TestConfigUtils.genSampleSQLConfigFile(false, srcDb, srcUser, srcPass, tgtDb, tgtUser, "");
        try {
            // Execute method and ensure right error is thrown
            JPSRuntimeException thrownError = assertThrows(JPSRuntimeException.class, () -> ConfigStore.retrieveSQLConfig(null, ""));
            assertEquals("Missing Properties:\n" +
                    "src.db.user is missing! Please add the input to endpoint.properties.\n" +
                    "src.db.password is missing! Please add the input to endpoint.properties.\n" +
                    "target.db.password is missing! Please add the input to endpoint.properties.\n", thrownError.getMessage());
        } finally {
            // Always delete generated config file
            config.delete();
        }
    }

    @Test
    void testRetrieveSQLConfig() throws IOException {
        File config = TestConfigUtils.genSampleSQLConfigFile(true, srcDb, srcUser, srcPass, tgtDb, tgtUser, tgtPass);
        try {
            // Execute method
            String[] result = ConfigStore.retrieveSQLConfig(null, "");
            // Verify results are expected
            assertEquals(srcDb, result[0]);
            assertEquals(srcUser, result[1]);
            assertEquals(srcPass, result[2]);
            assertEquals(tgtDb, result[3]);
            assertEquals(tgtUser, result[4]);
            assertEquals(tgtPass, result[5]);
        } finally {
            // Always delete generated config file
            config.delete();
        }
    }

    @Test
    void testRetrieveTSClientConfigNoFile() {
        // Verify right exception is thrown
        JPSRuntimeException thrownError = assertThrows(JPSRuntimeException.class, () -> ConfigStore.retrieveTSClientConfig("", ""));
        assertEquals("No endpoint.properties file detected! Please place the file in the config directory.", thrownError.getMessage());
    }

    @Test
    void testRetrieveTSClientConfigMissingProperties() throws IOException {
        File config = TestConfigUtils.genSampleTimeSeriesConfigFile(false, srcDb, srcUser, srcPass, srcSparql, "", "");
        try {
            // Execute method and ensure right error is thrown
            JPSRuntimeException thrownError = assertThrows(JPSRuntimeException.class, () -> ConfigStore.retrieveTSClientConfig("", ""));
            assertEquals("Missing Properties:\n" +
                    "src.db.user is missing! Please add the input to endpoint.properties.\n" +
                    "src.db.password is missing! Please add the input to endpoint.properties.\n", thrownError.getMessage());
        } finally {
            // Always delete generated config file
            config.delete();
        }
    }

    @Test
    void testRetrieveTSClientConfigOptionalSparqlCredentials() throws IOException {
        File config = TestConfigUtils.genSampleTimeSeriesConfigFile(true, srcDb, srcUser, srcPass, srcSparql, "", "");
        try {
            // Execute method
            String[] result = ConfigStore.retrieveTSClientConfig("", "");
            // Verify results are expected
            assertEquals(srcDb, result[0]);
            assertEquals(srcUser, result[1]);
            assertEquals(srcPass, result[2]);
            assertEquals(srcSparql, result[3]);
        } finally {
            // Always delete generated config file
            config.delete();
        }
    }

    @Test
    void testRetrieveTSClientConfig() throws IOException {
        File config = TestConfigUtils.genSampleTimeSeriesConfigFile(true, srcDb, srcUser, srcPass, srcSparql, srcUser, srcPass);
        try {
            // Execute method
            String[] result = ConfigStore.retrieveTSClientConfig("", "");
            // Verify results are expected
            assertEquals(srcDb, result[0]);
            assertEquals(srcUser, result[1]);
            assertEquals(srcPass, result[2]);
            assertEquals(securedSrcSparql, result[3]);
        } finally {
            // Always delete generated config file
            config.delete();
        }
    }
}
package uk.ac.cam.cares.jps;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class TestConfigUtils {
    private static final String srcSparqlKey = "sparql.src.endpoint";
    private static final String srcSparqlUser = "sparql.src.user";
    private static final String srcSparqlPass = "sparql.src.password";
    private static final String targetSparqlKey = "sparql.target.endpoint";
    private static final String targetSparqlUser = "sparql.target.user";
    private static final String targetSparqlPass = "sparql.target.password";
    private static final String srcDBUrl = "src.db.url";
    private static final String srcDBUser = "src.db.user";
    private static final String srcDBPass = "src.db.password";
    private static final String targetDBUrl = "target.db.url";
    private static final String targetDBUser = "target.db.user";
    private static final String targetDBPass = "target.db.password";

    public static File genSampleSPARQLConfigFile(boolean isEmpty, String srcSparql, String tgtSparql) throws IOException {
        return genSampleSPARQLConfigFile(isEmpty, srcSparql, tgtSparql, "", "", "", "");
    }

    public static File genSampleSPARQLConfigFile(boolean isEmpty, String srcSparql, String tgtSparql, String srcUser, String srcPass, String tgtUser, String tgtPass) throws IOException {
        File file = new File(System.getProperty("user.dir") + "/config/endpoint.properties");
        // Check if the directory exists, create it if it doesn't
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        PrintWriter writer = new PrintWriter(file);
        if (isEmpty) {
            writer.println(srcSparqlKey + "=");
        } else {
            writer.println(srcSparqlKey + "=" + srcSparql);
        }
        writer.println(targetSparqlKey + "=" + tgtSparql);
        // Optional credential properties
        if (!srcUser.isEmpty()) writer.println(srcSparqlUser + "=" + srcUser);
        if (!srcUser.isEmpty()) writer.println(srcSparqlPass + "=" + srcPass);
        if (!srcUser.isEmpty()) writer.println(targetSparqlUser + "=" + tgtUser);
        if (!srcUser.isEmpty()) writer.println(targetSparqlPass + "=" + tgtPass);
        writer.close();
        return file;
    }

    public static File genSampleSQLConfigFile(boolean isComplete, String srcDb, String srcDbUser, String srcDbPass,
                                              String tgtDb, String tgtDbUser, String tgtDbPass) throws IOException {
        File file = new File(System.getProperty("user.dir") + "/config/endpoint.properties");
        // Check if the directory exists, create it if it doesn't
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        PrintWriter writer = new PrintWriter(file);
        writer.println(srcDBUrl + "=" + srcDb);
        writer.println(targetDBUrl + "=" + tgtDb);
        writer.println(targetDBUser + "=" + tgtDbUser);
        writer.println(targetDBPass + "=" + tgtDbPass);
        if (isComplete) {
            writer.println(srcDBUser + "=" + srcDbUser);
            writer.println(srcDBPass + "=" + srcDbPass);
        }
        writer.close();
        return file;
    }

    public static File genSampleTimeSeriesConfigFile(boolean isComplete, String srcDb, String srcDbUser, String srcDbPass,
                                                     String srcSparql, String srcUser, String srcPass) throws IOException {
        File file = new File(System.getProperty("user.dir") + "/config/endpoint.properties");
        // Check if the directory exists, create it if it doesn't
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        PrintWriter writer = new PrintWriter(file);
        writer.println(srcDBUrl + "=" + srcDb);
        writer.println(srcSparqlKey + "=" + srcSparql);
        if (isComplete) {
            writer.println(srcDBUser + "=" + srcDbUser);
            writer.println(srcDBPass + "=" + srcDbPass);
        }
        // Optional sparql credential properties
        if (!srcUser.isEmpty()) writer.println(srcSparqlUser + "=" + srcUser);
        if (!srcUser.isEmpty()) writer.println(srcSparqlPass + "=" + srcPass);
        writer.close();
        return file;
    }
}

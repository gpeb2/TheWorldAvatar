package uk.ac.cam.cares.jps.agent.gfaagent;

import uk.ac.cam.cares.jps.base.agent.JPSAgent;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(urlPatterns = {"/osm", "/floors", "/estimate"})
public class BuildingFloorAgent extends JPSAgent{
    
    
    private EndpointConfig endpointConfig = new EndpointConfig();

    private String dbName;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    public String floorsCsv;
    private String osmSchema;
    private String osmPoint;
    private String osmPolygon;

    public synchronized void init() {
        this.dbName = endpointConfig.getDbName();
        this.dbUrl = endpointConfig.getDbUrl(dbName);
        this.dbUser = endpointConfig.getDbUser();
        this.dbPassword = endpointConfig.getDbPassword();       
        this.floorsCsv = endpointConfig.getFilepath();
        this.osmSchema = endpointConfig.getOSMSchema();
        this.osmPoint = endpointConfig.getOSMPoints();
        this.osmPolygon = endpointConfig.getOSMPolygons();
    }

    @Override
    public JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
        return processRequestParameters(requestParams);
    }

    @Override
    public JSONObject processRequestParameters(JSONObject requestParams) {

        try {
            
            //integrate floors data: 1. query osm address 2. match address from HDB csv 3. store floors data
            IntegrateFloors integrateFloors = new IntegrateFloors(dbUrl, dbUser, dbPassword, osmSchema, osmPoint, osmPolygon);
            integrateFloors.addFloorAccuracyColumn();
            integrateFloors.matchAddress(floorsCsv);
            integrateFloors.roughFloorDate();
    
        } catch (Exception e) {
            e.printStackTrace();
            throw new JPSRuntimeException(e);
        }

        return requestParams;
    }
}

package com.cmclinnovations.dispersion;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgis.Point;
import uk.ac.cam.cares.jps.base.query.RemoteRDBStoreClient;
import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;

@WebServlet(urlPatterns = { "/CreateVirtualSensors" })
public class CreateVirtualSensors extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(CreateVirtualSensors.class);
    private QueryClient queryClient;
    private DispersionPostGISClient dispersionPostGISClient;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("Received POST request to create new virtual sensors. ");

        double lat = Double.parseDouble(req.getParameter("lat"));
        double lng = Double.parseDouble(req.getParameter("lng"));

        Point virtualSensorLocation = new Point(lng, lat);
        virtualSensorLocation.setSrid(4326);

        // Retrieve the iri of the scope containing the virtual sensor location for each
        // virtual sensor.
        // For now, each virtual sensor location is only allowed to lie within one
        // scope.
        // Need to check if there is already a virtual sensor at any of the input
        // locations. If so, the existing sensor should not be duplicated.
        List<String> vsScopeList = new ArrayList<>();
        List<Point> vsLocationsInScope = new ArrayList<>();

        try (Connection conn = dispersionPostGISClient.getConnection()) {
            List<String> scopeIriList = new ArrayList<>();
            scopeIriList = queryClient.getScopesIncludingPoint(virtualSensorLocation);
            if (scopeIriList.size() == 1 && !dispersionPostGISClient.sensorExists(virtualSensorLocation, conn)) {
                vsScopeList.add(scopeIriList.get(0));
                vsLocationsInScope.add(virtualSensorLocation);
            } else if (scopeIriList.isEmpty()) {
                LOGGER.warn(" The specified virtual sensor location " +
                        "at {} does not fall within any existing scope. No sensor will be created at this location.",
                        virtualSensorLocation.toString());
            } else if (scopeIriList.size() > 1) {
                LOGGER.warn(
                        " The specified virtual sensor location at {} is contained within more than one scope polygon."
                                +
                                " No sensor will be created at this location.",
                        virtualSensorLocation.toString());
            }

            if (!vsLocationsInScope.isEmpty()) {
                if (!dispersionPostGISClient.tableExists(Config.SENSORS_TABLE_NAME, conn))
                    queryClient.initialiseVirtualSensorAgent();
                queryClient.initializeVirtualSensors(vsScopeList, vsLocationsInScope);

            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }

    }

    @Override
    public void init() throws ServletException {
        EndpointConfig endpointConfig = Config.ENDPOINT_CONFIG;
        RemoteStoreClient storeClient = new RemoteStoreClient(endpointConfig.getKgurl(), endpointConfig.getKgurl());

        dispersionPostGISClient = new DispersionPostGISClient(endpointConfig.getDburl(),
                endpointConfig.getDbuser(),
                endpointConfig.getDbpassword());
        RemoteRDBStoreClient remoteRDBStoreClient = new RemoteRDBStoreClient(endpointConfig.getDburl(),
                endpointConfig.getDbuser(), endpointConfig.getDbpassword());
        TimeSeriesClient<Long> tsClient = new TimeSeriesClient<>(storeClient, Long.class);
        TimeSeriesClient<Instant> tsClientInstant = new TimeSeriesClient<>(storeClient,
                Instant.class);
        queryClient = new QueryClient(storeClient, remoteRDBStoreClient, tsClient, tsClientInstant);
    }

}

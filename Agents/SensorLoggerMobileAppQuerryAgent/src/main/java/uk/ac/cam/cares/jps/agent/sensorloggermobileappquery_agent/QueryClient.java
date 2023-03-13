package uk.ac.cam.cares.jps.agent.sensorloggermobileappquery_agent;

import org.eclipse.rdf4j.model.vocabulary.GEOF;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.PropertyPaths;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.json.JSONArray;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.geom.Polygon;
import org.postgis.Point;
import org.apache.jena.geosparql.implementation.parsers.wkt.WKTReader;


import it.unibz.inf.ontop.model.vocabulary.GEO;
import uk.ac.cam.cares.jps.agent.sensorloggermobileappquery_agent.objects.PersonGPSPoint;
import uk.ac.cam.cares.jps.base.derivation.DerivationSparql;
import uk.ac.cam.cares.jps.base.query.RemoteRDBStoreClient;
import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeries;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QueryClient {
    private static final Logger LOGGER = LogManager.getLogger(QueryClient.class);

    private RemoteStoreClient storeClient;
    private RemoteStoreClient ontopStoreClient;
    private RemoteRDBStoreClient rdbStoreClient;
    private TimeSeriesClient<OffsetDateTime> tsClient;
    private TimeSeriesClient<Instant> tsClientInstant;

    // prefixes
    private static final String ONTO_EMS = "https://www.theworldavatar.com/kg/ontoems/";
    public static final String PREFIX_DISP = "http://www.theworldavatar.com/kg/ontosensorloggermobileapp/";
    static final String OM_STRING = "http://www.ontology-of-units-of-measure.org/resource/om-2/";
    private static final Prefix P_OM = SparqlBuilder.prefix("om",iri(OM_STRING));
    private static final Prefix P_DISP = SparqlBuilder.prefix("disp", iri(PREFIX_DISP));
    private static final Prefix P_GEO = SparqlBuilder.prefix("geo", iri(GEO.PREFIX));
    private static final Prefix P_GEOF = SparqlBuilder.prefix("geof", iri(GEOF.NAMESPACE));
    private static final Prefix P_EMS = SparqlBuilder.prefix("ems", iri(ONTO_EMS));

    // classes
    public static final String REPORTING_STATION = "https://www.theworldavatar.com/kg/ontoems/ReportingStation";
    public static final String NX = PREFIX_DISP + "nx";
    public static final String NY = PREFIX_DISP + "ny";
    public static final String SCOPE = PREFIX_DISP + "Scope";
    public static final String SIMULATION_TIME = PREFIX_DISP + "SimulationTime";
    public static final String NO_X = PREFIX_DISP + "NOx";
    public static final String UHC = PREFIX_DISP + "uHC";
    public static final String CO = PREFIX_DISP + "CO";
    public static final String SO2 = PREFIX_DISP + "SO2";
    public static final String PM10 = PREFIX_DISP + "PM10";
    public static final String PM25 = PREFIX_DISP + "PM2.5";
    public static final String DENSITY = OM_STRING + "Density";
    public static final String TEMPERATURE = OM_STRING + "Temperature";
    public static final String MASS_FLOW = OM_STRING + "MassFlow";
    private static final Iri SHIP = P_DISP.iri("Ship");
    static final String PREFIX = "http://www.theworldavatar.com/kg/ontosensorloggermobileapp/kb/";

    // weather types
    private static final String CLOUD_COVER = ONTO_EMS + "CloudCover";
    private static final String AIR_TEMPERATURE = ONTO_EMS + "AirTemperature";
    private static final String RELATIVE_HUMIDITY = ONTO_EMS + "RelativeHumidity";
    private static final String WIND_SPEED = ONTO_EMS + "WindSpeed";
    private static final String WIND_DIRECTION = ONTO_EMS + "WindDirection";

    // IRI of units used
    private static final Iri UNIT_DEGREE = P_OM.iri("degree");
    private static final Iri UNIT_CELCIUS = P_OM.iri("degreeCelsius");
    private static final Iri UNIT_MS = P_OM.iri("metrePerSecond-Time");
    private static final Iri UNIT_PERCENTAGE = P_OM.iri("PercentageUnit");

    // Location type
    private static final Iri LOCATION = P_DISP.iri("Location");

    // outputs (belongsTo)
    private static final String DISPERSION_MATRIX = PREFIX_DISP + "DispersionMatrix";
    private static final String DISPERSION_LAYER = PREFIX_DISP + "DispersionLayer";
    private static final String SHIPS_LAYER = PREFIX_DISP + "ShipsLayer";

    // properties
    private static final Iri HAS_PROPERTY = P_DISP.iri("hasProperty");
    private static final Iri HAS_VALUE = P_OM.iri("hasValue");
    private static final Iri HAS_NUMERICALVALUE = P_OM.iri("hasNumericalValue");
    private static final Iri HAS_QUANTITY = P_OM.iri("hasQuantity");
    private static final Iri HAS_UNIT = P_OM.iri("hasUnit");
    private static final Iri HAS_GEOMETRY = P_GEO.iri("hasGeometry");
    private static final Iri AS_WKT = P_GEO.iri("asWKT");
    private static final Iri IS_DERIVED_FROM = iri(DerivationSparql.derivednamespace + "isDerivedFrom");
    private static final Iri BELONGS_TO = iri(DerivationSparql.derivednamespace + "belongsTo");
    private static final Iri REPORTS = P_EMS.iri("reports");

    // fixed units for each measured property
    private static final Map<String, Iri> UNIT_MAP = new HashMap<>();
    static {
        UNIT_MAP.put(CLOUD_COVER, UNIT_PERCENTAGE);
        UNIT_MAP.put(AIR_TEMPERATURE, UNIT_CELCIUS);
        UNIT_MAP.put(RELATIVE_HUMIDITY, UNIT_PERCENTAGE);
        UNIT_MAP.put(WIND_SPEED, UNIT_MS);
        UNIT_MAP.put(WIND_DIRECTION, UNIT_DEGREE);
    }

    QueryClient(RemoteStoreClient storeClient, RemoteStoreClient ontopStoreClient, RemoteRDBStoreClient rdbStoreClient) {
        this.storeClient = storeClient;
        this.ontopStoreClient = ontopStoreClient;
        this.tsClient = new TimeSeriesClient<>(storeClient, OffsetDateTime.class);
        this.tsClientInstant = new TimeSeriesClient<>(storeClient, Instant.class);
        this.rdbStoreClient = rdbStoreClient;
    }

    int getMeasureValueAsInt(String instance) {
        SelectQuery query = Queries.SELECT().prefix(P_OM);
        Variable value = query.var();
        GraphPattern gp = iri(instance).has(PropertyPaths.path(HAS_VALUE, HAS_NUMERICALVALUE), value);
        query.where(gp);
        JSONArray queryResult = storeClient.executeQuery(query.getQueryString());
        return Integer.parseInt(queryResult.getJSONObject(0).getString(value.getQueryString().substring(1)));
    }

    long getMeasureValueAsLong(String instance) {
        SelectQuery query = Queries.SELECT().prefix(P_OM);
        Variable value = query.var();
        GraphPattern gp = iri(instance).has(PropertyPaths.path(HAS_VALUE, HAS_NUMERICALVALUE), value);
        query.where(gp);
        JSONArray queryResult = storeClient.executeQuery(query.getQueryString());
        return Long.parseLong(queryResult.getJSONObject(0).getString(value.getQueryString().substring(1)));
    }

    List<PersonGPSPoint> getPersonGPSPointsWithinTimeAndScopeViaTSClient(OffsetDateTime LowerBound, OffsetDateTime UpperBound, Geometry scope) {

//        Map<String,String> measureToShipMap = getMeasureToShipMap();
        List<String> measures = new ArrayList<>();
        measures.add("https://www.theworldavatar.com/kg/measure_605a09c9-d6c5-4ba7-bc28-fe595d698b41_geom_location_aa0f99ba-6ff7-4a23-baac-b6d63a651f91");

        List<PersonGPSPoint> personGPSPoints = new ArrayList<>();
        try (Connection conn = rdbStoreClient.getConnection()) {
            measures.stream().forEach(measure -> {

                TimeSeries<OffsetDateTime> ts = tsClient.getTimeSeriesWithinBounds(List.of(measure), LowerBound, UpperBound, conn);

                if (ts.getValuesAsPoint(measure).size() > 1) {
                    LOGGER.warn("More than 1 point within this time interval");
                } else if (ts.getValuesAsPoint(measure).isEmpty()) {
                    return;
                }

                try {
                    for (int i=0; i<ts.getValuesAsPoint(measure).size(); i++){
                        // this is to convert from org.postgis.Point to the Geometry class
                        Point postgisPoint = ts.getValuesAsPoint(measure).get(i);
                        String wktLiteral = postgisPoint.getTypeString() + postgisPoint.getValue();
                        OffsetDateTime timestamp = ts.getTimes().get(i);

                        Geometry point = new org.locationtech.jts.io.WKTReader().read(wktLiteral);

                        if (scope.covers(point)) {
                            // measureToShipMap.get(measure) gives the iri
                            PersonGPSPoint personGPSPoint = new PersonGPSPoint(measure);
                            personGPSPoint.setLocation(postgisPoint);
                            personGPSPoint.setTime(timestamp);
                            personGPSPoints.add(personGPSPoint);
                            

                        }
                    }
                } catch (ParseException e) {
                    LOGGER.error("Failed to parse WKT literal of point");
                    LOGGER.error(e.getMessage());
                    return;
                }

            });

        } catch (SQLException e) {
            LOGGER.error("Probably failed at closing connection");
            LOGGER.error(e.getMessage());
        }

        return personGPSPoints;
    }

    



    /**
     * the result is the geo:wktLiteral type with IRI of SRID in front
     * @param scopeIri
     * @return
     */
    Polygon getScopeFromOntop(String scopeIri) {
        SelectQuery query = Queries.SELECT();
        Variable scope = query.var();

        query.prefix(P_GEO).where(iri(scopeIri).has(PropertyPaths.path(HAS_GEOMETRY, AS_WKT), scope));

        JSONArray queryResult = ontopStoreClient.executeQuery(query.getQueryString());
        String wktLiteral = queryResult.getJSONObject(0).getString(scope.getQueryString().substring(1));
        Geometry scopePolygon = WKTReader.extract(wktLiteral).getGeometry();
        scopePolygon.setSRID(4326);
        return (Polygon) scopePolygon;
    }


    void initialiseAgent() {
        Iri hasType = iri("http://www.theworldavatar.com/ontology/ontoagent/MSM.owl#hasType");

        Iri partIri = iri(PREFIX + UUID.randomUUID());


        ModifyQuery modify = Queries.MODIFY();

        modify.insert(partIri.has(hasType, SCOPE));

        storeClient.executeUpdate(modify.getQueryString());
    }


    private static final Iri SCOPEIRI = P_DISP.iri("Scope");
    void initialiseScopeDerivation(String scopeIri) {
        ModifyQuery modify = Queries.MODIFY();
        modify.insert(iri(scopeIri).isA(SCOPEIRI));


        List<String> inputs = new ArrayList<>();
        inputs.add(scopeIri);

    }





//    Map<String,String> getMeasureToShipMap() {
//        SelectQuery query = Queries.SELECT();
//
//        Variable ship = query.var();
//        Variable locationMeasure = query.var();
//        Variable property = query.var();
//
//        GraphPattern gp = GraphPatterns.and(ship.isA(SHIP).andHas(HAS_PROPERTY, property),
//                property.isA(LOCATION).andHas(HAS_VALUE, locationMeasure));
//
//        query.where(gp).prefix(P_OM,P_DISP);
//
//        JSONArray queryResult = storeClient.executeQuery(query.getQueryString());
//
//        Map<String,String> locationMeasureToShipMap = new HashMap<>();
//        for (int i = 0; i < queryResult.length(); i++) {
//            String locationMeasureIri = queryResult.getJSONObject(i).getString(locationMeasure.getQueryString().substring(1));
//
//            String shipIri = queryResult.getJSONObject(i).getString(ship.getQueryString().substring(1));
//
//            locationMeasureToShipMap.put(locationMeasureIri, shipIri);
//        }
//
//        return locationMeasureToShipMap;
//    }








}


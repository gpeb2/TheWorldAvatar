package com.cmclinnovations.aermod;

import org.eclipse.rdf4j.model.vocabulary.GEOF;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.PropertyPaths;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.json.JSONArray;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.postgis.Point;
import org.apache.jena.geosparql.implementation.parsers.wkt.WKTReader;

import com.cmclinnovations.aermod.sparqlbuilder.GeoSPARQL;
import com.cmclinnovations.aermod.objects.Ship;
import com.cmclinnovations.aermod.objects.WeatherData;

import it.unibz.inf.ontop.model.vocabulary.GEO;
import uk.ac.cam.cares.jps.base.derivation.DerivationSparql;
import uk.ac.cam.cares.jps.base.derivation.ValuesPattern;
import uk.ac.cam.cares.jps.base.query.RemoteRDBStoreClient;
import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeries;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QueryClient {
    private static final Logger LOGGER = LogManager.getLogger(QueryClient.class);

    private RemoteStoreClient storeClient;
    private RemoteStoreClient ontopStoreClient;
    private RemoteRDBStoreClient rdbStoreClient;
    private TimeSeriesClient<Long> tsClientLong;
    private TimeSeriesClient<Instant> tsClientInstant;

    // prefixes
    private static final String ONTO_EMS = "https://www.theworldavatar.com/kg/ontoems/";
    public static final String PREFIX_DISP = "http://www.theworldavatar.com/kg/dispersion/";
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
    private static final Iri UNIT_FRACTION = P_OM.iri("RatioUnit"); 
    private static final Iri UNIT_PERCENTAGE = P_OM.iri("PercentageUnit");

    // properties
    private static final Iri HAS_VALUE = P_OM.iri("hasValue");
    private static final Iri HAS_NUMERICALVALUE = P_OM.iri("hasNumericalValue");
    private static final Iri HAS_QUANTITY = P_OM.iri("hasQuantity");
    private static final Iri HAS_UNIT = P_OM.iri("hasUnit");
    private static final Iri HAS_LOCATION = P_DISP.iri("hasLocation");
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

    public QueryClient(RemoteStoreClient storeClient, RemoteStoreClient ontopStoreClient, RemoteRDBStoreClient rdbStoreClient) {
        this.storeClient = storeClient;
        this.ontopStoreClient = ontopStoreClient;
        this.tsClientLong = new TimeSeriesClient<>(storeClient, Long.class);
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

    List<Ship> getShipsWithinTimeAndScopeViaTsClient(long simulationTime, Geometry scope) {
        long simTimeUpperBound = simulationTime + 1800; // +30 minutes
        long simTimeLowerBound = simulationTime - 1800; // -30 minutes

        Map<String,String> measureToShipMap = getMeasureToShipMap();
        List<String> measures = new ArrayList<>(measureToShipMap.keySet());

        List<Ship> ships = new ArrayList<>();
        try (Connection conn = rdbStoreClient.getConnection()) {
            measures.stream().forEach(measure -> {
                TimeSeries<Long> ts = tsClientLong.getTimeSeriesWithinBounds(List.of(measure), simTimeLowerBound, simTimeUpperBound, conn);
                if (ts.getValuesAsPoint(measure).size() > 1) {
                    LOGGER.warn("More than 1 point within this time inverval");
                } else if (ts.getValuesAsPoint(measure).isEmpty()) {
                    return;
                }

                try {
                    Point postgisPoint = ts.getValuesAsPoint(measure).get(0);
                    String wktLiteral = postgisPoint.getTypeString() + postgisPoint.getValue();

                    Geometry point = new org.locationtech.jts.io.WKTReader().read(wktLiteral);
                    
                    if (scope.contains(point)) {
                        // measureToShipMap.get(measure) gives the iri
                        ships.add(new Ship(measureToShipMap.get(measure)));
                    }
                } catch (ParseException e) {
                    LOGGER.error("Failed to parse WKT literal of point");
                    LOGGER.error(e.getMessage());
                }
                
            });
        } catch (SQLException e) {
            LOGGER.error("Probably failed at closing connection");
            LOGGER.error(e.getMessage());
        }

        return ships;
    }

    /**
     * the result is the geo:wktLiteral type with IRI of SRID in front
     * @param scopeIri
     * @return
     */
    Geometry getScopeFromOntop(String scopeIri) {
        SelectQuery query = Queries.SELECT();
        Variable scope = query.var();
        
        query.prefix(P_GEO).where(iri(scopeIri).has(PropertyPaths.path(HAS_GEOMETRY, AS_WKT), scope));

        JSONArray queryResult = ontopStoreClient.executeQuery(query.getQueryString());
        String wktLiteral = queryResult.getJSONObject(0).getString(scope.getQueryString().substring(1));
        return WKTReader.extract(wktLiteral).getGeometry();
    }

    Map<String,String> getMeasureToShipMap() {
        SelectQuery query = Queries.SELECT();

        Variable ship = query.var();
        Variable locationMeasure = query.var();
        GraphPattern gp = ship.isA(SHIP).andHas(PropertyPaths.path(HAS_LOCATION,HAS_VALUE), locationMeasure);

        query.where(gp).prefix(P_OM,P_DISP);

        JSONArray queryResult = storeClient.executeQuery(query.getQueryString());

        Map<String,String> locationMeasureToShipMap = new HashMap<>();
        for (int i = 0; i < queryResult.length(); i++) {
            String locationMeasureIri = queryResult.getJSONObject(i).getString(locationMeasure.getQueryString().substring(1));
            String shipIri = queryResult.getJSONObject(i).getString(ship.getQueryString().substring(1));
            locationMeasureToShipMap.put(locationMeasureIri, shipIri);
        }

        return locationMeasureToShipMap;
    }

    void setEmissions(List<Ship> ships) {
        SelectQuery query = Queries.SELECT();

        Variable derivation = query.var();
        Variable ship = query.var();
        Variable entity = query.var();
        Variable entityType = query.var();
        Variable quantity = query.var();
        Variable quantityType = query.var();
        Variable numericalValue = query.var();

        ValuesPattern shipValues = new ValuesPattern(ship, ships.stream().map(s -> iri(s.getIri())).collect(Collectors.toList()));

        ValuesPattern entityTypeValues = new ValuesPattern(entityType, List.of(iri(NO_X), iri(UHC), iri(CO), iri(SO2), iri(PM10), iri(PM25)));
        
        GraphPattern gp = GraphPatterns.and(derivation.has(IS_DERIVED_FROM, ship), entity.has(BELONGS_TO, derivation).andIsA(entityType)
        .andHas(HAS_QUANTITY, quantity), quantity.isA(quantityType).andHas(PropertyPaths.path(HAS_VALUE, HAS_NUMERICALVALUE),numericalValue));

        query.where(gp,shipValues,entityTypeValues).prefix(P_OM);

        JSONArray queryResult = storeClient.executeQuery(query.getQueryString());

        // create a look up map to get ship object based on IRI
        Map<String, Ship> iriToShipMap = new HashMap<>();
        ships.stream().forEach(s -> iriToShipMap.put(s.getIri(), s));

        for (int i = 1; i < queryResult.length(); i++) {
            String shipIri = queryResult.getJSONObject(i).getString(ship.getQueryString().substring(1));
            double literalValue = queryResult.getJSONObject(i).getDouble(numericalValue.getQueryString().substring(1));
            Ship shipObject = iriToShipMap.get(shipIri);

            String entityTypeIri = queryResult.getJSONObject(i).getString(entityType.getQueryString().substring(1));
            String quantityTypeIri = queryResult.getJSONObject(i).getString(quantityType.getQueryString().substring(1));

            if (entityTypeIri.contentEquals(PM10) && quantityTypeIri.contentEquals(MASS_FLOW)) {
                // PM10 flowrate
                shipObject.getChimney().setPM10(literalValue);
            } else if (entityTypeIri.contentEquals(PM25) && quantityTypeIri.contentEquals(MASS_FLOW)) {
                // PM2.5 flowrate
                shipObject.getChimney().setPM25(literalValue);
            } else if (entityTypeIri.contentEquals(PM25) && quantityTypeIri.contentEquals(DENSITY)) {
                // particle density
                shipObject.getChimney().setParticleDensity(literalValue);
            }
        }
    }

    /**
     * returns derivation IRIs for each ship by querying
     * <derivation> isDerivedFrom <ship>
     * @param ships
     * @return
     */
    List<String> getDerivationsOfShips(List<Ship> ships) {
        SelectQuery query = Queries.SELECT();

        Variable derivation = query.var();
        Variable ship = query.var();
        Iri isDerivedFrom = iri(DerivationSparql.derivednamespace + "isDerivedFrom");
        ValuesPattern vp = new ValuesPattern(ship, ships.stream().map(s -> iri(s.getIri())).collect(Collectors.toList()));
        GraphPattern gp = derivation.has(isDerivedFrom, ship);

        query.where(gp,vp);

        JSONArray queryResult = storeClient.executeQuery(query.getQueryString());
        
        List<String> derivations = new ArrayList<>();
        for (int i = 0; i < queryResult.length(); i++) {
            derivations.add(queryResult.getJSONObject(i).getString(derivation.getQueryString().substring(1)));
        }

        return derivations;
    }

    WeatherData getWeatherData(String station, long timestamp) {
        SelectQuery query = Queries.SELECT();

        Variable weatherType = query.var();
        Variable quantity = query.var();
        Variable measure = query.var();
        Variable weatherUnit = query.var();

        // RDF types for weather data
        List<String> weatherTypeList = List.of(CLOUD_COVER, AIR_TEMPERATURE, RELATIVE_HUMIDITY, WIND_SPEED, WIND_DIRECTION);

        ValuesPattern vp = new ValuesPattern(weatherType, weatherUnit);
        weatherTypeList.stream().forEach(type -> vp.addValuePairForMultipleVariables(iri(type), UNIT_MAP.get(type)));

        GraphPattern gp = GraphPatterns.and(iri(station).has(REPORTS, quantity),
        quantity.isA(weatherType).andHas(HAS_VALUE, measure), measure.has(HAS_UNIT, weatherUnit));

        query.prefix(P_OM, P_EMS).where(gp,vp);

        Map<String, String> typeToMeasureMap = new HashMap<>();
        JSONArray queryResult = storeClient.executeQuery(query.getQueryString());
        for (int i = 0; i < queryResult.length(); i++) {
            String measureIri = queryResult.getJSONObject(i).getString(measure.getQueryString().substring(1));
            String weatherTypeIri = queryResult.getJSONObject(i).getString(weatherType.getQueryString().substring(1));

            typeToMeasureMap.put(weatherTypeIri, measureIri);
        }

        TimeSeries<Instant> ts;
        try (Connection conn = rdbStoreClient.getConnection()) {
            Instant timestampAsInstant = Instant.ofEpochSecond(timestamp);
            ts = tsClientInstant.getTimeSeriesWithinBounds(typeToMeasureMap.values().stream().collect(Collectors.toList()), timestampAsInstant, timestampAsInstant, conn);
        } catch (SQLException e) {
            String errmsg = "Failed to obtain time series from weather station";
            LOGGER.fatal(errmsg);
            throw new RuntimeException(errmsg, e);
        }

        WeatherData weatherData = new WeatherData();
        weatherTypeList.stream().forEach(type -> {
            double value = ts.getValuesAsDouble(typeToMeasureMap.get(type)).get(0);
            switch (type) {
                case CLOUD_COVER:
                    weatherData.setCloudCoverInPercentage(value);
                    break;
                case AIR_TEMPERATURE:
                    weatherData.setTemperatureInCelcius(value);
                    break;
                case RELATIVE_HUMIDITY:
                    weatherData.setHumidityInPercentage(value);
                    break;
                case WIND_SPEED:
                    weatherData.setWindSpeedInMetreSecond(value);
                    break;
                case WIND_DIRECTION:
                    weatherData.setWindDirectionInDegrees(value);
                    break;
                default:
                    LOGGER.error("Unknown weather RDF type: <{}>", type);
            }
        });
        return weatherData;
    }

    @Deprecated
    List<String> getShipsWithinTimeAndScopeViaKG(long simulationTime, String scopeIri) {
        long simTimeUpperBound = simulationTime + 1800; // +30 minutes
        long simTimeLowerBound = simulationTime - 1800; // -30 minutes

        Map<String,String> measureToShipMap = getMeasureToShipMap();
        List<String> locationMeasures = new ArrayList<>(measureToShipMap.keySet());

        Map<String, String> geometryToMeasureMap = getGeometryToMeasureMap(locationMeasures);
        List<String> geometryIrisWithinTimeBounds = getGeometriesWithinTimeBounds(new ArrayList<>(geometryToMeasureMap.keySet()), simTimeLowerBound, simTimeUpperBound);
        String scopeGeometry = getScopeGeometry(scopeIri);
        List<String> geometriesWithinScopeAndTime = getGeometriesWithinScope(scopeGeometry, geometryIrisWithinTimeBounds);

        List<String> shipsWithinTimeAndScope = new ArrayList<>();
        geometriesWithinScopeAndTime.forEach(g -> 
            shipsWithinTimeAndScope.add(measureToShipMap.get(geometryToMeasureMap.get(g)))
        );

        return shipsWithinTimeAndScope;
    }

    /**
     * to ontop
     * @param scopeIri
     */
    @Deprecated
    String getScopeGeometry(String scopeIri) {
        SelectQuery query = Queries.SELECT();
        Variable scopeGeometry = query.var();
        query.prefix(P_GEO).where(iri(scopeIri).has(HAS_GEOMETRY, scopeGeometry));

        JSONArray queryResult = ontopStoreClient.executeQuery(query.getQueryString());
        return queryResult.getJSONObject(0).getString(scopeGeometry.getQueryString().substring(1));
    }

    /**
     * from ontop
     * @param locationMeasures
     * @return
     */
    @Deprecated
    Map<String, String> getGeometryToMeasureMap(List<String> locationMeasures) {
        SelectQuery query = Queries.SELECT();
        Variable locationMeasure = query.var();
        Variable geometry = query.var();

        ValuesPattern vp = new ValuesPattern(locationMeasure, locationMeasures.stream().map(Rdf::iri).collect(Collectors.toList()));
        GraphPattern gp = locationMeasure.has(HAS_GEOMETRY,geometry);

        query.prefix(P_GEO).where(vp,gp);

        JSONArray queryResult = ontopStoreClient.executeQuery(query.getQueryString());

        Map<String, String> geometryToMeasureMap = new HashMap<>();
        for (int i = 0; i < queryResult.length(); i++) {
            String geometryIri = queryResult.getJSONObject(i).getString(geometry.getQueryString().substring(1));
            String measureIri = queryResult.getJSONObject(i).getString(locationMeasure.getQueryString().substring(1));
            geometryToMeasureMap.put(geometryIri, measureIri);
        }

        return geometryToMeasureMap;
    }

    /**
     * from ontop
     * @param geometryIris
     * @param simTimeLowerBound
     * @param simTimeUpperBound
     */
    @Deprecated
    List<String> getGeometriesWithinTimeBounds(List<String> geometryIris, long simTimeLowerBound, long simTimeUpperBound) {
        SelectQuery query = Queries.SELECT();

        Variable shipTime = query.var();
        Variable geometry = query.var();

        ValuesPattern vp = new ValuesPattern(geometry, geometryIris.stream().map(Rdf::iri).collect(Collectors.toList()));
        GraphPattern gp = GraphPatterns.and(vp,geometry.has(P_DISP.iri("hasTime"), shipTime));

        query.prefix(P_GEO,P_GEOF,P_OM,P_DISP).
        where(gp.filter
        (Expressions.and(Expressions.gt(shipTime, simTimeLowerBound), Expressions.lt(shipTime, simTimeUpperBound))));

        JSONArray queryResult = ontopStoreClient.executeQuery(query.getQueryString());
        List<String> geometriesWithinTimeBounds = new ArrayList<>();

        for (int i = 0; i < queryResult.length(); i++) {
            geometriesWithinTimeBounds.add(queryResult.getJSONObject(i).getString(geometry.getQueryString().substring(1)));
        }

        return geometriesWithinTimeBounds;
    }

    @Deprecated
    List<String> getGeometriesWithinScope(String scopeGeometry, List<String> shipGeometriesWithinTimeBounds) {
        SelectQuery query = Queries.SELECT();
        Variable shipGeometry = query.var();
        Variable scopeWkt = query.var();
        Variable shipWkt = query.var();

        ValuesPattern vp = new ValuesPattern(shipGeometry, shipGeometriesWithinTimeBounds.stream().map(Rdf::iri).collect(Collectors.toList()));

        GraphPattern gp = GraphPatterns.and(vp, shipGeometry.has(AS_WKT, shipWkt), iri(scopeGeometry).has(AS_WKT, scopeWkt));

        query.where(gp.filter(Expressions.and(GeoSPARQL.sfIntersects(shipWkt, scopeWkt)))).prefix(P_GEO,P_GEOF);

        JSONArray queryResult = ontopStoreClient.executeQuery(query.getQueryString());
        List<String> geometriesWithinScope = new ArrayList<>();
        for (int i = 0; i < queryResult.length(); i++) {
            geometriesWithinScope.add(queryResult.getJSONObject(i).getString(shipGeometry.getQueryString().substring(1)));
        }
        return geometriesWithinScope;
    }
}

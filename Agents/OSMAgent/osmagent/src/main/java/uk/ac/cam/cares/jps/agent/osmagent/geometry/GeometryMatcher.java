package uk.ac.cam.cares.jps.agent.osmagent.geometry;

import uk.ac.cam.cares.jps.agent.osmagent.geometry.object.*;
import uk.ac.cam.cares.jps.base.query.RemoteRDBStoreClient;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.util.Map;
import org.json.JSONArray;

public class GeometryMatcher {
    public static String GEO_DB = "GEODB";
    public static String GEO_USER = "GEOUSER";
    public static String GEO_PASSWORD = "GEOPASSWORD";
    public static String OSM_DB = "OSMDB";
    public static String OSM_USER = "OSMUSER";
    public static String OSM_PASSWORD = "OSMPASSWORD";

    private double threshold = 0.7;

    private Map<String, GeoObject> geoObjects;

    private String osmDb;
    private String osmUser;
    private String osmPassword;
    private String tableName;

    private RemoteRDBStoreClient geoClient;
    private RemoteRDBStoreClient osmClient;

    public GeometryMatcher(Map<String, String> configs, String table) {
        tableName = table;
        geoObjects = GeoObject.getGeoObjects(configs.get(GEO_DB), configs.get(GEO_USER), configs.get(GEO_PASSWORD));
        osmDb = configs.get(OSM_DB);
        osmUser = configs.get(OSM_USER);
        osmPassword = configs.get(OSM_PASSWORD);
        geoClient = new RemoteRDBStoreClient(configs.get(GEO_DB), configs.get(GEO_USER), configs.get(GEO_PASSWORD));
        osmClient = new RemoteRDBStoreClient(configs.get(OSM_DB), configs.get(OSM_USER), configs.get(OSM_PASSWORD));
    }

    public void matchGeometry() throws ParseException {
        WKTReader wktReader = new WKTReader();

        Map<Integer, OSMObject> osmObjects = OSMObject.getOSMObject(osmDb, osmUser, osmPassword, tableName, "building IS NOT NULL AND iri IS NULL");


        String update = "UPDATE " + tableName + " SET iri = CASE";

        for (Map.Entry<Integer, OSMObject> entry : osmObjects.entrySet()){
            OSMObject osmObject = entry.getValue();

            if (osmObject.getGeometry().contains("POINT")) {
                String query = booleanQuery(osmObject);

                JSONArray result = geoClient.executeQuery(query);

                if (!result.isEmpty()) {
                    osmObject.setIri(result.getJSONObject(0).getString("iri"));
                }
            }
            else {
                String query = areaQuery(osmObject);

                JSONArray matchResult = geoClient.executeQuery(query);

                float matchedArea = matchResult.getJSONObject(0).getFloat("area");

                Geometry geometry = wktReader.read(osmObject.getGeometry());

                if (matchedArea / geometry.getArea() >= threshold) {
                    osmObject.setIri(matchResult.getJSONObject(0).getString("iri"));
                }
            }

            if (!osmObject.getIri().isEmpty()) {
                geoObjects.remove(osmObject.getIri());
                update += " " + caseOgcFid(osmObject.getOgcfid(), osmObject.getIri());
            }
        }

        update += " ELSE iri END";

        if (update.contains("WHEN") && update.contains("THEN")) {
            osmClient.executeUpdate(update);
        }

        update = "UPDATE " + tableName + " SET iri = CASE";

        osmObjects = OSMObject.getOSMObject(osmDb, osmUser, osmPassword, tableName, "iri IS NULL");

        boolean flag = checkIfPoints(tableName);

        for (Map.Entry<String, GeoObject> entry : geoObjects.entrySet()) {
            GeoObject geoObject = entry.getValue();

            String query = queryFromOSM(tableName, geoObject, flag, threshold);

            JSONArray result = osmClient.executeQuery(query);

            String iriUpdate = "";

            if (!result.isEmpty()) {
                OSMObject osmObject = osmObjects.get(result.getJSONObject(0).get("id"));
                iriUpdate = caseOgcFid(osmObject.getOgcfid(), geoObject.getUrival());
                for (int i = 1; i < result.length(); i++) {
                    osmObject = osmObjects.get(result.getJSONObject(i).getInt("id"));
                    iriUpdate = insertUpdate(iriUpdate, osmObject.getOgcfid());
                }
            }

            if (!iriUpdate.isEmpty()) {
                update += " " + iriUpdate;
            }
        }

        update += " ELSE iri END";

        if (update.contains("WHEN") && update.contains("THEN")) {
            osmClient.executeUpdate(update);
        }
    }

    private boolean checkIfPoints(String table) {
        boolean flag = false;
        String query = "SELECT ST_ASText(\"geometryProperty\") as geostring FROM " + table + " LIMIT 1";
        JSONArray result = osmClient.executeQuery(query);

        if (!result.isEmpty()) {
            flag = result.getJSONObject(0).getString("geostring").contains("POINT") ? true : false;
        }

        return flag;
    }
    private String queryFromOSM(String table, GeoObject geoObject, Boolean flag, Double threshold) {
        String query;
        Integer srid = geoObject.getSrid();
        String geometry = geoObject.getGeometry();

        if (flag) {
            query = "SELECT ogc_fid AS id FROM " + table + " WHERE public.ST_Intersects(public.ST_GeomFromText(\'" + geometry + "\'," + srid + ")," +
                    "public.ST_Transform(\"geometryProperty\"," + srid + ")) AND iri IS NULL";
        }
        else {
            String subQuery = "(SELECT ogc_fid, public.ST_Area(public.ST_Intersection(public.ST_GeomFromText(\'" + geometry + "\'," + srid +
                    "),public.ST_Transform(\"geometryProperty\"," + srid + "))) AS matchedarea, public.ST_Area(\"geometryProperty\") AS area FROM " + table + " WHERE iri is NULL) AS q";

            query = "SELECT q.ogc_fid AS id FROM " + subQuery + " WHERE (matchedarea / area) >= " + threshold;
        }

        return query;
    }

    private String subQuery() {
        return " (" + GeoObject.getQuery() + ") AS q";
    }

    private String booleanQuery(OSMObject osmObject) {
        String osmObjectString = transformString(geometryString(osmObject.getGeometry(), osmObject.getSrid()));

        return "SELECT q.urival AS iri FROM " +  subQuery() + " WHERE public.ST_Intersects(q.geometry," + osmObjectString + ")";
    }

    private String areaQuery(OSMObject osmObject) {
        String osmObjectString = transformString(geometryString(osmObject.getGeometry(), osmObject.getSrid()));

        String query =  "SELECT public.ST_Area(public.ST_Intersection(q.geometry," + osmObjectString + ")) AS area, q.urival AS iri";
        query += " FROM " + subQuery();

        return "SELECT area, iri FROM (" + query + ") AS intersection ORDER BY intersection.area DESC LIMIT 1";
    }

    private String geometryString(String geometry, Integer srid) {
        return "public.ST_GeomFromText(\'" + geometry + "\'," + srid + ")";
    }

    private String transformString(String geometry) {
        return "public.ST_Transform(" + geometry + ",ST_Srid(q.geometry))";
    }

    private String insertUpdate(String query, Integer ogcFid) {
       String[] split = query.split("THEN");

       return split[0] + "OR ogc_fid = " + ogcFid + " THEN" + split[1];
    }

    private String caseOgcFid(Integer ogcFid, String iri) {
        return "WHEN ogc_fid = " + ogcFid + " THEN \'" + iri + "\'";
    }
}

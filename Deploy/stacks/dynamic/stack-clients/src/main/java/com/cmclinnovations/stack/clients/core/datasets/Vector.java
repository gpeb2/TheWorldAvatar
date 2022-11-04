package com.cmclinnovations.stack.clients.core.datasets;

import java.nio.file.Path;

import com.cmclinnovations.stack.clients.gdal.GDALClient;
import com.cmclinnovations.stack.clients.gdal.Ogr2OgrOptions;
import com.cmclinnovations.stack.clients.geoserver.GeoServerClient;
import com.cmclinnovations.stack.clients.geoserver.GeoServerVectorSettings;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Vector extends DataSubset {

    @JsonProperty
    private Ogr2OgrOptions ogr2ogrOptions = new Ogr2OgrOptions();

    @JsonProperty
    private GeoServerVectorSettings geoServerSettings = new GeoServerVectorSettings();

    @Override
    public void loadData(GDALClient gdalClient, String datasetDir, String database) {
        Path dirPath = Path.of(datasetDir, getSubdirectory());
        ogr2ogrOptions.setSchema(getSchema());
        gdalClient.uploadVectorFilesToPostGIS(database, getTable(), dirPath.toString(), ogr2ogrOptions, false);
    }

    @Override
    public void createLayer(GeoServerClient geoServerClient, String dataSubsetDir, String workspaceName,
            String database) {
        geoServerClient.createPostGISLayer(dataSubsetDir, workspaceName, database, getSchema(), getName(),
                geoServerSettings);
    }

}

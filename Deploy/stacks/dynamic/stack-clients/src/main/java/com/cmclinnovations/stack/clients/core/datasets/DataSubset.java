package com.cmclinnovations.stack.clients.core.datasets;

import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @Type(value = Tabular.class, names = { "Tabular", "tabular" }),
        @Type(value = Vector.class, names = { "Vector", "vector" }),
        @Type(value = Raster.class, names = { "Raster", "raster" }),
        @Type(value = RDF.class, names = { "Triples", "triples", "RDF", "rdf", "Quads", "quads" }),
        @Type(value = TBoxCSV.class, names = { "TBoxCSV", "TboxCSV", "tboxcsv", }) })
public abstract class DataSubset {
    private String name;
    @JsonProperty
    private Path subdirectory;

    @JsonProperty
    private boolean skip;

    @JsonProperty
    private String description;

    // for dcat cataloging purposes
    private boolean exists; // used to determine whether this exists in the catalog
    private String iri;

    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return (null != description) ? description : getName();
    }

    public Path getSubdirectory() {
        return (null != subdirectory) ? subdirectory : Path.of("");
    }

    public boolean isSkip() {
        return skip;
    }

    public boolean usesPostGIS() {
        return false;
    }

    public boolean usesBlazegraph() {
        return false;
    }

    public boolean usesGeoServer() {
        return false;
    }

    public void load(Dataset dataset) {
        if (!skip) {
            loadInternal(dataset);
        }
    }

    abstract void loadInternal(Dataset dataset);

    public void setExists(boolean exists) {
        this.exists = exists;
    }
    public boolean getExists() {
        return exists;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }
    public String getIri() {
        return iri;
    }
}

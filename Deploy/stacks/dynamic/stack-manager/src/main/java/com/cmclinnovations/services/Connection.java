package com.cmclinnovations.services;

import java.net.URI;
import java.net.URL;

import com.cmclinnovations.jackson.deserialisers.URIDeserialiser;
import com.cmclinnovations.jackson.deserialisers.URLDeserialiser;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Connection {

    @JsonDeserialize(using = URLDeserialiser.class)
    private URL url;
    @JsonDeserialize(using = URIDeserialiser.class)
    private URI uri;
    @JsonDeserialize(using = URIDeserialiser.class)
    private URI externalPath;

    public URL getUrl() {
        return url;
    }

    public URI getUri() {
        return uri;
    }

    public URI getExternalPath() {
        return externalPath;
    }

}

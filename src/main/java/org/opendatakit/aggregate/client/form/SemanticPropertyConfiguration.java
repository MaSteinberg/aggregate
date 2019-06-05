package org.opendatakit.aggregate.client.form;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author Markus Steinberg
 */
public class SemanticPropertyConfiguration implements Serializable {
    private static final long serialVersionUID = 3180598340579147616L;

    private String endpoint;
    private String query;

    @JsonProperty("Endpoint")
    public String getEndpoint() {
        return endpoint;
    }

    @JsonProperty("Endpoint")
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @JsonProperty("Query")
    public String getQuery() {
        return query;
    }

    @JsonProperty("Query")
    public void setQuery(String query) {
        this.query = query;
    }
}

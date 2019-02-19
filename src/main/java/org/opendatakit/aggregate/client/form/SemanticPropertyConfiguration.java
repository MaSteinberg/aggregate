package org.opendatakit.aggregate.client.form;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SemanticPropertyConfiguration {
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

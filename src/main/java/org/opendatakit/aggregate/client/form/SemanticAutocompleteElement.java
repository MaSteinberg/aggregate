package org.opendatakit.aggregate.client.form;

public class SemanticAutocompleteElement {
    private String uri;
    private String displayName;

    public SemanticAutocompleteElement(String uri, String displayName) {
        this.uri = uri;
        this.displayName = displayName;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}

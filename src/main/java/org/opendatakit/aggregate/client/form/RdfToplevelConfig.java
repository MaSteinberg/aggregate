package org.opendatakit.aggregate.client.form;

import java.util.List;

public class RdfToplevelConfig {
    private List<String> availableProperties;
    private List<String> templates;

    public List<String> getAvailableProperties() {
        return availableProperties;
    }

    public void setAvailableProperties(List<String> availableProperties) {
        this.availableProperties = availableProperties;
    }

    public List<String> getTemplates() {
        return templates;
    }

    public void setTemplates(List<String> templates) {
        this.templates = templates;
    }
}

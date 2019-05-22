package org.opendatakit.aggregate.client.form;

import java.util.List;
import java.util.Map;

/*
Class used for deserialization of the TemplateExportConfig.yml
 */
public class TemplateToplevelConfig {
    private Map<String, SemanticPropertyConfiguration> availableProperties;
    private List<String> templates;

    public Map<String, SemanticPropertyConfiguration> getAvailableProperties() {
        return availableProperties;
    }

    public void setAvailableProperties(Map<String, SemanticPropertyConfiguration> availableProperties) {
        this.availableProperties = availableProperties;
    }

    public List<String> getTemplates() {
        return templates;
    }

    public void setTemplates(List<String> templates) {
        this.templates = templates;
    }
}

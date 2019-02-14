package org.opendatakit.aggregate.client.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RdfExportOptions implements Serializable {
    private static final long serialVersionUID = 3805983057947175416L;

    private List<String> availableProperties;
    private Map<String, TemplateProperties> templates;

    // Getters and Setters
    public void setTemplates(Map<String, TemplateProperties> templates) {
        this.templates = templates;
    }

    public Map<String, TemplateProperties> getTemplates() {
        return templates;
    }

    public void setAvailableProperties(List<String> availableProperties) {
        this.availableProperties = availableProperties;
    }

    public List<String> getAvailableProperties() {
        return availableProperties;
    }

    public List<String> getOptionalProperties(String templateName){
        //Returns null if "templateName" is not registered
        return this.templates.get(templateName).getOptionalProperties();
    }

    public List<String> getRequiredMetrics(String templateName){
        //Returns null if "templateName" is not registered
        return this.templates.get(templateName).getRequiredProperties();
    }

    // Utility function to conveniently get the List of names of all registered templates
    // Should be ignored during both Serialization and Deserialization
    @JsonIgnore
    public Set<String> getRegisteredTemplateList(){
        return this.templates.keySet();
    }

    // Utility function returning only the information relevant to a given template name
    @JsonIgnore
    public TemplateProperties getTemplateMetrics(String templateName){
        return this.templates.get(templateName);
    }
}

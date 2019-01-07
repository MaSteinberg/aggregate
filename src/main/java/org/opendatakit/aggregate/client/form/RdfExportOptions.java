package org.opendatakit.aggregate.client.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RdfExportOptions implements Serializable {
    private static final long serialVersionUID = 3805983057947175416L;

    private List<String> availableMetrics;
    private Map<String, TemplateMetrics> templates;

    // Getters and Setters
    public void setTemplates(Map<String, TemplateMetrics> templates) {
        this.templates = templates;
    }

    public Map<String, TemplateMetrics> getTemplates() {
        return templates;
    }

    public void setAvailableMetrics(List<String> availableMetrics) {
        this.availableMetrics = availableMetrics;
    }

    public List<String> getAvailableMetrics() {
        return availableMetrics;
    }

    public List<String> getOptionalMetrics(String templateName){
        //Returns null if "templateName" is not registered
        return this.templates.get(templateName).getOptionalMetrics();
    }

    public List<String> getRequiredMetrics(String templateName){
        //Returns null if "templateName" is not registered
        return this.templates.get(templateName).getRequiredMetrics();
    }

    // Utility function to conveniently get the List of names of all registered templates
    // Should be ignored during both Serialization and Deserialization
    @JsonIgnore
    public Set<String> getRegisteredTemplateList(){
        return this.templates.keySet();
    }

    // Utility function returning only the information relevant to a given template name
    @JsonIgnore
    public TemplateMetrics getTemplateMetrics(String templateName){
        return this.templates.get(templateName);
    }
}

package org.opendatakit.aggregate.client.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RdfExportOptions implements Serializable {
    private static final long serialVersionUID = 3805983057947175416L;

    private List<String> availableMetrics;
    private Map<String, TemplateMetrics> templates;

    public void setTemplates(Map<String, TemplateMetrics> templates) {
        this.templates = templates;
    }

    public void setAvailableMetrics(List<String> availableMetrics) {
        this.availableMetrics = availableMetrics;
    }

    public List<String> getAvailableMetrics() {
        return availableMetrics;
    }

    public Set<String> getRegisteredTemplates(){
        return this.templates.keySet();
    }

    public List<String> getOptionalMetrics(String templateName){
        //Returns null if "templateName" is not registered
        return this.templates.get(templateName).getOptionalMetrics();
    }

    public List<String> getRequiredMetrics(String templateName){
        //Returns null if "templateName" is not registered
        return this.templates.get(templateName).getRequiredMetrics();
    }
}

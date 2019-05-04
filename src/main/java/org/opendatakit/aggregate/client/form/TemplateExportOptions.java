package org.opendatakit.aggregate.client.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
Class used to communicate the full RDF Export configuration to both the frontend and the REST-API used by ODK Build
 */
public class TemplateExportOptions implements Serializable {
    private static final long serialVersionUID = 3805983057947175416L;

    private Map<String, SemanticPropertyConfiguration> availableProperties;
    private Map<String, ExportTemplateConfig> templates;

    //Necessary so that GWT can properly (de-)serialize the object
    private TemplateExportOptions() {}

    public TemplateExportOptions(Map<String, SemanticPropertyConfiguration> availableProperties, Map<String, ExportTemplateConfig> templates) {
        this.availableProperties = availableProperties;
        this.templates = templates;
    }

    public Map<String, SemanticPropertyConfiguration> getAvailableProperties() {
        return availableProperties;
    }

    public void setAvailableProperties(Map<String, SemanticPropertyConfiguration> availableProperties) {
        this.availableProperties = availableProperties;
    }

    public Map<String, ExportTemplateConfig> getTemplates() {
        return templates;
    }

    @JsonIgnore
    public Set<String> getRegisteredTemplateIds(){
        return this.templates.keySet();
    }

    @JsonIgnore
    public List<String> getRegisteredTemplatesDisplayNames(){
        List<String> res = new ArrayList<>();
        for(ExportTemplateConfig config : templates.values()){
            res.add(config.getDisplayName());
        }
        return res;
    }

    @JsonIgnore
    public String getTemplateDisplayName(String templateId){
        return this.templates.get(templateId).getDisplayName();
    }
}

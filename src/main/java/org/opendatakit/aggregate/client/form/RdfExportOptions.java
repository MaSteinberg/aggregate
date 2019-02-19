package org.opendatakit.aggregate.client.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RdfExportOptions implements Serializable {
    private static final long serialVersionUID = 3805983057947175416L;

    private Map<String, SemanticPropertyConfiguration> availableProperties;
    private Map<String, RdfTemplateConfig> templates;

    //Necessary so that GWT can properly (de-)serialize the object
    private RdfExportOptions() {}

    public RdfExportOptions(Map<String, SemanticPropertyConfiguration> availableProperties, Map<String, RdfTemplateConfig> templates) {
        this.availableProperties = availableProperties;
        this.templates = templates;
    }

    public Map<String, SemanticPropertyConfiguration> getAvailableProperties() {
        return availableProperties;
    }

    public void setAvailableProperties(Map<String, SemanticPropertyConfiguration> availableProperties) {
        this.availableProperties = availableProperties;
    }

    public Map<String, RdfTemplateConfig> getTemplates() {
        return templates;
    }

    @JsonIgnore
    public Set<String> getRegisteredTemplateIds(){
        return this.templates.keySet();
    }

    @JsonIgnore
    public List<String> getRegisteredTemplatesDisplayNames(){
        List<String> res = new ArrayList<>();
        for(RdfTemplateConfig config : templates.values()){
            res.add(config.getDisplayName());
        }
        return res;
    }

    @JsonIgnore
    public String getTemplateDisplayName(String templateId){
        return this.templates.get(templateId).getDisplayName();
    }
}

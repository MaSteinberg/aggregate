package org.opendatakit.aggregate.client.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RdfExportOptions implements Serializable {
    private static final long serialVersionUID = 3805983057947175416L;

    private List<String> availableProperties;
    private Map<String, RdfTemplateConfig> templates;

    //Necessary so that GWT can properly (de-)serialize the object
    private RdfExportOptions() {}

    public RdfExportOptions(List<String> availableProperties, Map<String, RdfTemplateConfig> templates) {
        this.availableProperties = availableProperties;
        this.templates = templates;
    }

    public List<String> getAvailableProperties() {
        return availableProperties;
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

package org.opendatakit.aggregate.client.form;

import java.io.Serializable;
import java.util.ArrayList;

public class RdfExportOptions implements Serializable {
    private static final long serialVersionUID = 3805983057947175416L;
    private ArrayList<String> templateGroups;

    public RdfExportOptions() {
        this.templateGroups = new ArrayList<>();
    }

    public ArrayList<String> getTemplateGroups() {
        return templateGroups;
    }

    public void setTemplateGroups(ArrayList<String> templateGroups) {
        this.templateGroups = templateGroups;
    }

    public void addTemplateGroup(String templateGroupName) {
        this.templateGroups.add(templateGroupName);
    }
}

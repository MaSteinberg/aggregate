package org.opendatakit.aggregate.client.form;

import java.io.Serializable;
import java.util.List;

public class TemplateMetrics implements Serializable {
    private List<String> optionalMetrics;
    private List<String> requiredMetrics;

    public List<String> getOptionalMetrics() {
        return optionalMetrics;
    }

    public void setOptionalMetrics(List<String> optionalMetrics) {
        this.optionalMetrics = optionalMetrics;
    }

    public List<String> getRequiredMetrics() {
        return requiredMetrics;
    }

    public void setRequiredMetrics(List<String> requiredMetrics) {
        this.requiredMetrics = requiredMetrics;
    }
}

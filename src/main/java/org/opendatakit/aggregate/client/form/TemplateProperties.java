package org.opendatakit.aggregate.client.form;

import java.io.Serializable;
import java.util.List;

/**
 * @author Markus Steinberg
 */
public class TemplateProperties implements Serializable {
    private List<String> optionalProperties;
    private List<String> requiredProperties;

    public List<String> getOptionalProperties() {
        return optionalProperties;
    }

    public void setOptionalProperties(List<String> optionalProperties) {
        this.optionalProperties = optionalProperties;
    }

    public List<String> getRequiredProperties() {
        return requiredProperties;
    }

    public void setRequiredProperties(List<String> requiredProperties) {
        this.requiredProperties = requiredProperties;
    }
}

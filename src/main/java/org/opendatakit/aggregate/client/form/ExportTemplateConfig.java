package org.opendatakit.aggregate.client.form;

import java.io.Serializable;

/*
Class used for the deserialization of a single template config.yml
 */
public class ExportTemplateConfig implements Serializable {
    private static final long serialVersionUID = 3805298395471795416L;

    private String displayName;
    private String filetype;
    private TemplateProperties templateProperties;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public TemplateProperties getTemplateProperties() {
        return templateProperties;
    }

    public void setTemplateProperties(TemplateProperties templateProperties) {
        this.templateProperties = templateProperties;
    }
}

package org.opendatakit.aggregate.format.structure.flexibleExport.models;

/**
 * @author Markus Steinberg
 */
public class TopLevelModel {
    public String toplevelIdentifier;
    public String formId;
    public String formName;
    public String formDescription;
    public String formCreationDate;
    public String formCreationUser;
    public String lastUpdate;
    public String formVersion;

    public TopLevelModel(String toplevelIdentifier, String formId, String formName, String formDescription, String formCreationDate, String formCreationUser, String lastUpdate, String formVersion) {
        this.toplevelIdentifier = toplevelIdentifier;
        this.formId = formId;
        this.formName = formName;
        this.formDescription = formDescription;
        this.formCreationDate = formCreationDate;
        this.formCreationUser = formCreationUser;
        this.lastUpdate = lastUpdate;
        this.formVersion = formVersion;
    }
}

package org.opendatakit.aggregate.format.structure.rdf.models;

import org.opendatakit.aggregate.form.IForm;

import java.util.Date;

public class TopLevelModel {
    public String id;
    public String name;
    public String description;
    public Date creationDate;
    public String creationUser;
    public Date lastUpdate;
    public String version;

    public TopLevelModel(IForm form){
        this.id = form.getFormId();
        this.name = form.getViewableName();
        this.description = form.getDescription();
        this.creationDate = form.getCreationDate();
        this.creationUser = form.getCreationUser();
        this.lastUpdate = form.getLastUpdateDate();
        this.version = form.getMajorMinorVersionString();
    }
}

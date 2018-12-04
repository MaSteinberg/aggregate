package org.opendatakit.aggregate.format.structure.rdf.models;

import java.util.Date;

public class TopLevelModel {
    public String id;
    public String name;
    public String description;
    public String creationDate;
    public String creationUser;
    public String lastUpdate;
    public String version;

    public TopLevelModel(String id, String name, String description, String creationDate, String creationUser, String lastUpdate, String version) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.creationUser = creationUser;
        this.lastUpdate = lastUpdate;
        this.version = version;
    }
}

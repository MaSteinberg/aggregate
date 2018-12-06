package org.opendatakit.aggregate.format.structure.rdf.models;

public class ColumnModel {
    public TopLevelModel topLevelModel;
    public String columnHeader;
    public String columnEntityIdentifier;

    public ColumnModel(TopLevelModel topLevelModel, String columnHeader, String columnEntityIdentifier) {
        this.topLevelModel = topLevelModel;
        this.columnHeader = columnHeader;
        this.columnEntityIdentifier = columnEntityIdentifier;
    }
}

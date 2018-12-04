package org.opendatakit.aggregate.format.structure.rdf.models;

public class ColumnModel {
    public TopLevelModel topLevelModel;
    public String columnHeader;

    public ColumnModel(TopLevelModel topLevelModel, String columnHeader) {
        this.topLevelModel = topLevelModel;
        this.columnHeader = columnHeader;
    }
}

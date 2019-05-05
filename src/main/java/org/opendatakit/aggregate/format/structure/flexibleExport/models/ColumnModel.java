package org.opendatakit.aggregate.format.structure.flexibleExport.models;

public class ColumnModel {
    public TopLevelModel topLevelModel;
    public String columnHeader;
    public String columnIdentifier;
    public boolean isFirstColumn;
    public boolean isLastColumn;

    public ColumnModel(TopLevelModel topLevelModel, String columnHeader, String columnIdentifier, boolean isFirstColumn, boolean isLastColumn) {
        this.topLevelModel = topLevelModel;
        this.columnHeader = columnHeader;
        this.columnIdentifier = columnIdentifier;
        this.isFirstColumn = isFirstColumn;
        this.isLastColumn = isLastColumn;
    }
}

package org.opendatakit.aggregate.format.structure.flexibleExport.models;

public class RowModel {
    public TopLevelModel topLevelModel;
    public String rowId;
    public String rowEntityIdentifier;
    public boolean isFirstRow;

    public RowModel(TopLevelModel topLevelModel, String rowId, String rowEntityIdentifier, boolean isFirstRow) {
        this.topLevelModel = topLevelModel;
        this.rowId = rowId;
        this.rowEntityIdentifier = rowEntityIdentifier;
        this.isFirstRow = isFirstRow;
    }
}


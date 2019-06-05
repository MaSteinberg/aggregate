package org.opendatakit.aggregate.format.structure.flexibleExport.models;

/**
 * @author Markus Steinberg
 */
public class RowModel {
    public TopLevelModel topLevelModel;
    public String rowId;
    public String rowIdentifier;
    public boolean isFirstRow;

    public RowModel(TopLevelModel topLevelModel, String rowId, String rowIdentifier, boolean isFirstRow) {
        this.topLevelModel = topLevelModel;
        this.rowId = rowId;
        this.rowIdentifier = rowIdentifier;
        this.isFirstRow = isFirstRow;
    }
}


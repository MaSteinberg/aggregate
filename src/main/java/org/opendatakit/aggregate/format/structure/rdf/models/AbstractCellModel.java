package org.opendatakit.aggregate.format.structure.rdf.models;

public abstract class AbstractCellModel {
    public TopLevelModel topLevelModel;
    public ColumnModel columnModel;
    public RowModel rowModel;

    public AbstractCellModel(TopLevelModel topLevelModel, ColumnModel columnModel, RowModel rowModel) {
        this.topLevelModel = topLevelModel;
        this.columnModel = columnModel;
        this.rowModel = rowModel;
    }
}

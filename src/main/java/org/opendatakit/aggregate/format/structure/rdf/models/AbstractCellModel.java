package org.opendatakit.aggregate.format.structure.rdf.models;

public abstract class AbstractCellModel {
    public ColumnModel columnModel;
    public RowModel rowModel;

    public AbstractCellModel(ColumnModel columnModel, RowModel rowModel) {
        this.columnModel = columnModel;
        this.rowModel = rowModel;
    }
}

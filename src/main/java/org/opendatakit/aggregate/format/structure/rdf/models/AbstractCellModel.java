package org.opendatakit.aggregate.format.structure.rdf.models;

public abstract class AbstractCellModel {
    public ColumnModel columnModel;
    public RowModel rowModel;
    public String cellEntityIdentifier;

    public AbstractCellModel(ColumnModel columnModel, RowModel rowModel, String cellEntityIdentifier) {
        this.columnModel = columnModel;
        this.rowModel = rowModel;
        this.cellEntityIdentifier = cellEntityIdentifier;
    }
}

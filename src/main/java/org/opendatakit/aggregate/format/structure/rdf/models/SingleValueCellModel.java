package org.opendatakit.aggregate.format.structure.rdf.models;

public class SingleValueCellModel extends AbstractCellModel{
    String cellValue;

    public SingleValueCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier) {
        super(columnModel, rowModel, cellEntityIdentifier);
        this.cellValue = cellValue;
    }
}

package org.opendatakit.aggregate.format.structure.rdf.models;

public class SingleValueCellModel extends AbstractCellModel{
    String cellValue;

    public SingleValueCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue) {
        super(columnModel, rowModel);
        this.cellValue = cellValue;
    }
}

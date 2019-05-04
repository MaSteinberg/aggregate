package org.opendatakit.aggregate.format.structure.flexibleExport.models;

import java.util.Map;

public class SingleValueCellModel extends AbstractCellModel{
    String cellValue;

    public SingleValueCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue,
                                String cellEntityIdentifier, Map<String, String> semantics) {
        super(columnModel, rowModel, cellEntityIdentifier, semantics);
        this.cellValue = turtleEncodeLiteral(cellValue);
    }
}

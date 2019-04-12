package org.opendatakit.aggregate.format.structure.rdf.models;

import java.util.Map;

public class SingleValueCellModel extends AbstractCellModel{
    String cellValue;

    public SingleValueCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue,
                                String cellEntityIdentifier, CellFlags cellFlags, Map<String, String> semantics) {
        super(columnModel, rowModel, cellEntityIdentifier, cellFlags, semantics);
        this.cellValue = turtleEncodeLiteral(cellValue);
    }
}

package org.opendatakit.aggregate.format.structure.rdf.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiValueCellModel extends AbstractCellModel {
    public List<String> cellValues;

    public MultiValueCellModel(ColumnModel columnModel, RowModel rowModel, ArrayList<String> cellValues, String cellEntityIdentifier, Map<String, String> semantics) {
        super(columnModel, rowModel, cellEntityIdentifier, semantics);
        for (int i = 0; i < cellValues.size(); i++) {
            cellValues.set(i, turtleEncodeLiteral(cellValues.get(i)));
        }
        this.cellValues = cellValues;
    }

    public MultiValueCellModel(ColumnModel columnModel, RowModel rowModel, String cellEntityIdentifier, Map<String, String> semantics) {
        super(columnModel, rowModel, cellEntityIdentifier, semantics);
        this.cellValues = null;
    }
}

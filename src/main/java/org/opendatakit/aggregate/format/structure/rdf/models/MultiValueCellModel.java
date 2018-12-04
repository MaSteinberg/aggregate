package org.opendatakit.aggregate.format.structure.rdf.models;

import java.util.List;

public class MultiValueCellModel extends AbstractCellModel {
    public List<String> cellValues;

    public MultiValueCellModel(TopLevelModel topLevelModel, ColumnModel columnModel, RowModel rowModel, List<String> cellValues) {
        super(columnModel, rowModel);
        this.cellValues = cellValues;
    }
}

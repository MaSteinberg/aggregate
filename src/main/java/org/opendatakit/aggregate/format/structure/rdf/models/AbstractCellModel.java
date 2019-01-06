package org.opendatakit.aggregate.format.structure.rdf.models;

import java.util.Map;

public abstract class AbstractCellModel {
    public ColumnModel columnModel;
    public RowModel rowModel;
    public String cellEntityIdentifier;
    public Map<String, String> semantics;

    public AbstractCellModel(ColumnModel columnModel, RowModel rowModel, String cellEntityIdentifier, Map<String, String> semantics) {
        this.columnModel = columnModel;
        this.rowModel = rowModel;
        this.cellEntityIdentifier = cellEntityIdentifier;
        this.semantics = semantics;
    }
}

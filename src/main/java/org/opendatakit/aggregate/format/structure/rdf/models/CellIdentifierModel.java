package org.opendatakit.aggregate.format.structure.rdf.models;

public class CellIdentifierModel {
    public String columnHeader;
    public String rowId;

    public CellIdentifierModel(String columnHeader, String rowId) {
        this.columnHeader = columnHeader;
        this.rowId = rowId;
    }
}

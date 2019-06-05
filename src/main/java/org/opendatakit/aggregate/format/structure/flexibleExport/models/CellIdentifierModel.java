package org.opendatakit.aggregate.format.structure.flexibleExport.models;

/**
 * @author Markus Steinberg
 */
public class CellIdentifierModel {
    public String columnHeader;
    public String rowId;

    public CellIdentifierModel(String columnHeader, String rowId) {
        this.columnHeader = columnHeader;
        this.rowId = rowId;
    }
}

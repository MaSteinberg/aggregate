package org.opendatakit.aggregate.format.structure.rdf.models;

import org.opendatakit.aggregate.form.IForm;

public class CellModel {
    public TopLevelModel tlModel;
    public ColumnModel columnModel;
    public RowModel rowModel;
    public String cellValue;

    public CellModel(TopLevelModel tlm, ColumnModel cm, RowModel rm, String cellValue){
        this.tlModel = tlm;
        this.rowModel = rm;
        this.columnModel = cm;
        this.cellValue = cellValue;
    }
}

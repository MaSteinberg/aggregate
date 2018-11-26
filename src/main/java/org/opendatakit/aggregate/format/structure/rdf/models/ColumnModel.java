package org.opendatakit.aggregate.format.structure.rdf.models;

import org.opendatakit.aggregate.form.IForm;

public class ColumnModel {
    public TopLevelModel tlModel;
    public String columnHeader;

    public ColumnModel(IForm form, String columnHeader){
        this.tlModel = new TopLevelModel(form);
        this.columnHeader = columnHeader;
    }

    public ColumnModel(TopLevelModel tlm, String columnHeader){
        this.tlModel = tlm;
        this.columnHeader = columnHeader;
    }
}

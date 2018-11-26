package org.opendatakit.aggregate.format.structure.rdf.models;

import org.opendatakit.aggregate.form.IForm;

import java.util.Date;

public class RowModel {
    public TopLevelModel tlModel;
    public String rowId;
    public String creator;
    public Date creationDate;
    public Date lastUpdateDate;
    public Date submissionDate;
    public Date markedAsCompleteDate;
    public Date startTime;
    public Date endTime;
    public Date today;
    public String phone;
    public String subscriberId;
    public String simSerial;
    public String deviceId;

    public RowModel(TopLevelModel tlm){
        this.tlModel = tlm;
    }

    public RowModel(IForm form){
        this.tlModel = new TopLevelModel(form);
    }
}

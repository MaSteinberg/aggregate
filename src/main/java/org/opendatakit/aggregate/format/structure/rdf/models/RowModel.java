package org.opendatakit.aggregate.format.structure.rdf.models;

import org.opendatakit.aggregate.form.IForm;

import java.util.Date;

public class RowModel {
    public TopLevelModel topLevelModel;
    public String rowId;
    public String rowEntityIdentifier;

    public RowModel(TopLevelModel topLevelModel, String rowId, String rowEntityIdentifier) {
        this.topLevelModel = topLevelModel;
        this.rowId = rowId;
        this.rowEntityIdentifier = rowEntityIdentifier;
    }
}


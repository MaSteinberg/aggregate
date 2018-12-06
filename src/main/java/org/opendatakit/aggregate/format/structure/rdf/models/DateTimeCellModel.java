package org.opendatakit.aggregate.format.structure.rdf.models;

public class DateTimeCellModel extends AbstractCellModel {
    String date;
    String time;

    public DateTimeCellModel(ColumnModel columnModel, RowModel rowModel, String date, String time, String cellEntityIdentifier) {
        super(columnModel, rowModel, cellEntityIdentifier);
        this.date = date;
        this.time = time;
    }

    public DateTimeCellModel(ColumnModel columnModel, RowModel rowModel, String cellEntityIdentifier) {
        super(columnModel, rowModel, cellEntityIdentifier);
        this.date = null;
        this.time = null;
    }
}

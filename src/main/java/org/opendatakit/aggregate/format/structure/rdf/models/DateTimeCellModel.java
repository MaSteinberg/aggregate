package org.opendatakit.aggregate.format.structure.rdf.models;

public class DateTimeCellModel extends AbstractCellModel {
    String date;
    String time;

    public DateTimeCellModel(TopLevelModel topLevelModel, ColumnModel columnModel, RowModel rowModel, String date, String time) {
        super(topLevelModel, columnModel, rowModel);
        this.date = date;
        this.time = time;
    }
}

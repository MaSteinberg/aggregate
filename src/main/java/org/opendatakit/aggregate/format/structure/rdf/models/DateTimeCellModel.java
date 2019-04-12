package org.opendatakit.aggregate.format.structure.rdf.models;

import java.util.Map;

public class DateTimeCellModel extends AbstractCellModel {
    String date;
    String time;

    public DateTimeCellModel(ColumnModel columnModel, RowModel rowModel, String date, String time,
                             String cellEntityIdentifier, CellFlags cellFlags, Map<String, String> semantics) {
        super(columnModel, rowModel, cellEntityIdentifier, cellFlags, semantics);
        this.date = turtleEncodeLiteral(date);
        this.time = turtleEncodeLiteral(time);
    }

    public DateTimeCellModel(ColumnModel columnModel, RowModel rowModel,
                             String cellEntityIdentifier, CellFlags cellFlags, Map<String, String> semantics) {
        super(columnModel, rowModel, cellEntityIdentifier, cellFlags, semantics);
        this.date = null;
        this.time = null;
    }
}

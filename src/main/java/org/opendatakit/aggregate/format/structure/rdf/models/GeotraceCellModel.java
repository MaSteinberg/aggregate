package org.opendatakit.aggregate.format.structure.rdf.models;

import java.util.List;

public class GeotraceCellModel extends AbstractCellModel {
    List<GeotraceElement> locationList;

    public GeotraceCellModel(ColumnModel columnModel, RowModel rowModel, List<GeotraceElement> locationList, String cellEntityIdentifier) {
        super(columnModel, rowModel, cellEntityIdentifier);
        this.locationList = locationList;
    }

    public GeotraceCellModel(ColumnModel columnModel, RowModel rowModel, String cellEntityIdentifier) {
        super(columnModel, rowModel, cellEntityIdentifier);
        this.locationList = null;
    }
}

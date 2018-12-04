package org.opendatakit.aggregate.format.structure.rdf.models;

import java.util.List;

public class GeotraceCellModel extends AbstractCellModel {
    List<GeotraceElement> locationList;

    public GeotraceCellModel(ColumnModel columnModel, RowModel rowModel, List<GeotraceElement> locationList) {
        super(columnModel, rowModel);
        this.locationList = locationList;
    }

    public GeotraceCellModel(ColumnModel columnModel, RowModel rowModel) {
        super(columnModel, rowModel);
        this.locationList = null;
    }
}

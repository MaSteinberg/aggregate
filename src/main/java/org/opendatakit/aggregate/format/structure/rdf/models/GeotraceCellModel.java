package org.opendatakit.aggregate.format.structure.rdf.models;

import java.util.List;
import java.util.Map;

public class GeotraceCellModel extends AbstractCellModel {
    List<GeotraceElement> locationList;

    public GeotraceCellModel(ColumnModel columnModel, RowModel rowModel,
                             List<GeotraceElement> locationList,
                             String cellEntityIdentifier, CellFlags cellFlags, Map<String, String> semantics) {
        super(columnModel, rowModel, cellEntityIdentifier, cellFlags, semantics);
        for (GeotraceElement e: locationList) {
            e.location.accuracy = turtleEncodeLiteral(e.location.accuracy);
            e.location.latitude = turtleEncodeLiteral(e.location.latitude);
            e.location.longitude = turtleEncodeLiteral(e.location.longitude);
            e.location.altitude = turtleEncodeLiteral(e.location.altitude);
        }
        this.locationList = locationList;
    }

    public GeotraceCellModel(ColumnModel columnModel, RowModel rowModel,
                             String cellEntityIdentifier, CellFlags cellFlags, Map<String, String> semantics) {
        super(columnModel, rowModel, cellEntityIdentifier, cellFlags, semantics);
        this.locationList = null;
    }
}

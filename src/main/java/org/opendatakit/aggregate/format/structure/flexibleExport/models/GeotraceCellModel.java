package org.opendatakit.aggregate.format.structure.flexibleExport.models;

import java.util.List;
import java.util.Map;

/**
 * @author Markus Steinberg
 */
public class GeotraceCellModel extends AbstractCellModel {
    List<GeotraceElement> locationList;

    public GeotraceCellModel(ColumnModel columnModel, RowModel rowModel,
                             List<GeotraceElement> locationList,
                             String cellEntityIdentifier, Map<String, String> semantics) {
        super(columnModel, rowModel, cellEntityIdentifier, semantics);
        for (GeotraceElement e: locationList) {
            e.location.accuracy = turtleEncodeLiteral(e.location.accuracy);
            e.location.latitude = turtleEncodeLiteral(e.location.latitude);
            e.location.longitude = turtleEncodeLiteral(e.location.longitude);
            e.location.altitude = turtleEncodeLiteral(e.location.altitude);
        }
        this.locationList = locationList;
    }

    public GeotraceCellModel(ColumnModel columnModel, RowModel rowModel,
                             String cellEntityIdentifier, Map<String, String> semantics) {
        super(columnModel, rowModel, cellEntityIdentifier, semantics);
        this.locationList = null;
    }
}

package org.opendatakit.aggregate.format.structure.flexibleExport.models;

import java.util.Map;

/**
 * @author Markus Steinberg
 */
public class GeolocationCellModel extends AbstractCellModel {
    public Location location;

    public GeolocationCellModel(ColumnModel columnModel, RowModel rowModel,
                                String latitude, String longitude, String altitude, String accuracy,
                                String cellEntityIdentifier, Map<String, String> semantics) {
        super(columnModel, rowModel, cellEntityIdentifier, semantics);
        this.location = new Location(turtleEncodeLiteral(latitude),
                                        turtleEncodeLiteral(longitude),
                                        turtleEncodeLiteral(altitude),
                                        turtleEncodeLiteral(accuracy));
    }

    public GeolocationCellModel(ColumnModel columnModel, RowModel rowModel,
                                String cellEntityIdentifier, Map<String, String> semantics) {
        super(columnModel, rowModel, cellEntityIdentifier, semantics);
        this.location = null;
    }
}

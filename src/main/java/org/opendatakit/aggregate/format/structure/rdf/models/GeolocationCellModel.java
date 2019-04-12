package org.opendatakit.aggregate.format.structure.rdf.models;

import java.util.Map;

public class GeolocationCellModel extends AbstractCellModel {
    public Location location;

    public GeolocationCellModel(ColumnModel columnModel, RowModel rowModel,
                                String latitude, String longitude, String altitude, String accuracy,
                                String cellEntityIdentifier, CellFlags cellFlags, Map<String, String> semantics) {
        super(columnModel, rowModel, cellEntityIdentifier, cellFlags, semantics);
        this.location = new Location(turtleEncodeLiteral(latitude),
                                        turtleEncodeLiteral(longitude),
                                        turtleEncodeLiteral(altitude),
                                        turtleEncodeLiteral(accuracy));
    }

    public GeolocationCellModel(ColumnModel columnModel, RowModel rowModel,
                                String cellEntityIdentifier, CellFlags cellFlags, Map<String, String> semantics) {
        super(columnModel, rowModel, cellEntityIdentifier, cellFlags, semantics);
        this.location = null;
    }
}

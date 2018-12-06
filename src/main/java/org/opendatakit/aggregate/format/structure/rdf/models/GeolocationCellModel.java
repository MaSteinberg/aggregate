package org.opendatakit.aggregate.format.structure.rdf.models;

public class GeolocationCellModel extends AbstractCellModel {
    public Location location;

    public GeolocationCellModel(ColumnModel columnModel, RowModel rowModel, String latitude, String longitude, String altitude, String accuracy, String cellEntityIdentifier) {
        super(columnModel, rowModel, cellEntityIdentifier);
        this.location = new Location(latitude, longitude, altitude, accuracy);
    }

    public GeolocationCellModel(ColumnModel columnModel, RowModel rowModel, String cellEntityIdentifier) {
        super(columnModel, rowModel, cellEntityIdentifier);
        this.location = null;
    }
}

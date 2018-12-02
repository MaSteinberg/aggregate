package org.opendatakit.aggregate.format.structure.rdf.models;

public class GeolocationCellModel extends AbstractCellModel {
    public Location location;

    public GeolocationCellModel(TopLevelModel topLevelModel, ColumnModel columnModel, RowModel rowModel, String latitude, String longitude, String altitude, String accuracy) {
        super(topLevelModel, columnModel, rowModel);
        this.location = new Location(latitude, longitude, altitude, accuracy);
    }
}

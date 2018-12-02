package org.opendatakit.aggregate.format.structure.rdf.models;

public class GeolocationCellModel extends AbstractCellModel {
    public String longitude;
    public String latitude;
    public String altitude;
    public String accuracy;

    public GeolocationCellModel(TopLevelModel topLevelModel, ColumnModel columnModel, RowModel rowModel, String latitude, String longitude, String altitude, String accuracy) {
        super(topLevelModel, columnModel, rowModel);
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = accuracy;
    }
}

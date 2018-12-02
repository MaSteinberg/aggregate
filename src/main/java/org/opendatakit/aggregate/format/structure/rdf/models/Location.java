package org.opendatakit.aggregate.format.structure.rdf.models;

public class Location {
    public String latitude;
    public String longitude;
    public String altitude;
    public String accuracy;

    public Location(String latitude, String longitude, String altitude, String accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = accuracy;
    }
}

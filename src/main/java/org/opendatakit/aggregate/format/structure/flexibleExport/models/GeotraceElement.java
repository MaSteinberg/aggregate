package org.opendatakit.aggregate.format.structure.flexibleExport.models;

public class GeotraceElement {
    int pathElementIndex;
    Location location;

    public GeotraceElement(int pathElementIndex, Location location) {
        this.pathElementIndex = pathElementIndex;
        this.location = location;
    }
}
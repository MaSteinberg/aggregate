package org.opendatakit.aggregate.format.structure.rdf.models;

public class GeopathElement {
    int index;
    Location location;

    public GeopathElement(int index, Location location) {
        this.index = index;
        this.location = location;
    }
}

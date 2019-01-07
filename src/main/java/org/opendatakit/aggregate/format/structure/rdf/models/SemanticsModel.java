package org.opendatakit.aggregate.format.structure.rdf.models;

public class SemanticsModel {
    public String value;
    public Boolean isLiteral;

    SemanticsModel(String value, Boolean isLiteral) {
        this.value = value;
        this.isLiteral = isLiteral;
    }
}

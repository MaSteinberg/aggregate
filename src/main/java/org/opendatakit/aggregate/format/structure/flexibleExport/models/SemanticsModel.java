package org.opendatakit.aggregate.format.structure.flexibleExport.models;

/**
 * @author Markus Steinberg
 */
public class SemanticsModel {
    public String value;
    public Boolean isLiteral;

    SemanticsModel(String value, Boolean isLiteral) {
        this.value = value;
        this.isLiteral = isLiteral;
    }
}

package org.opendatakit.aggregate.format.structure.rdf.models;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCellModel {
    public ColumnModel columnModel;
    public RowModel rowModel;
    public String cellEntityIdentifier;
    public Map<String, SemanticsModel> semantics;

    AbstractCellModel(ColumnModel columnModel, RowModel rowModel, String cellEntityIdentifier, Map<String, String> semanticsValueMap) {
        this.columnModel = columnModel;
        this.rowModel = rowModel;
        this.cellEntityIdentifier = cellEntityIdentifier;
        //The template has to distinguish between RDF-resources and literals so for each value we have to set a flag
        //containing that information
        this.semantics = new HashMap<>();
        for(Map.Entry<String, String> entry : semanticsValueMap.entrySet()){
            SemanticsModel property;
            if(entry.getValue().startsWith("_onto_")){
                //Remove _onto_ prefix and replace "__" with ":" since ":" is not allowed in single-choice-questions
                String val = StringUtils.removeStart(entry.getValue(), "_onto_").replaceFirst("__", ":");
                property = new SemanticsModel(val, false);
            } else{
                property = new SemanticsModel(entry.getValue(), true);
            }
            this.semantics.put(entry.getKey(), property);
        }
    }
}

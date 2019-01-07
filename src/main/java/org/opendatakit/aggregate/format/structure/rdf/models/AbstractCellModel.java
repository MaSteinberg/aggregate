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
            SemanticsModel metric;
            if(entry.getValue().startsWith("_onto_")){
                metric = new SemanticsModel(StringUtils.removeStart(entry.getValue(), "_onto_"), false);

            } else{
                metric = new SemanticsModel(entry.getValue(), true);
            }
            this.semantics.put(entry.getKey(), metric);
        }
    }
}

package org.opendatakit.aggregate.format.structure.flexibleExport.models;

import org.apache.commons.lang3.StringUtils;
import org.opendatakit.aggregate.format.structure.flexibleExport.TemplateFormatterWithFilters;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCellModel {
    public ColumnModel columnModel;
    public RowModel rowModel;
    public String cellEntityIdentifier;
    public Map<String, SemanticsModel> semantics;

    AbstractCellModel(ColumnModel columnModel, RowModel rowModel,
                      String cellEntityIdentifier, Map<String, String> semanticsValueMap) {
        this.columnModel = columnModel;
        this.rowModel = rowModel;
        this.cellEntityIdentifier = cellEntityIdentifier;
        //The template has to distinguish between RDF-resources and literals so for each value we have to set a flag
        //containing that information
        this.semantics = new HashMap<>();
        for(Map.Entry<String, String> entry : semanticsValueMap.entrySet()){
            SemanticsModel property;
            if(entry.getValue().startsWith(TemplateFormatterWithFilters.ONTOLOGY_REF_PREFIX)){
                //Remove prefix & encoding, then encode to turtle
                String val = turtleEncodeUri(
                        decodeFromBuild(
                            StringUtils.removeStart(entry.getValue(), TemplateFormatterWithFilters.ONTOLOGY_REF_PREFIX)
                        )
                );
                property = new SemanticsModel(val, false);
            } else{
                //Encode
                property = new SemanticsModel(turtleEncodeLiteral(entry.getValue()), true);
            }
            this.semantics.put(entry.getKey(), property);
        }
    }

    protected String turtleEncodeUri(String uri){
        if(uri == null)
            return null;
        return uri.replaceAll(">", "\\\\>");
    }

    protected String turtleEncodeLiteral(String literal){
        if(literal == null)
            return null;
        return literal.replaceAll("\"", "\\\\\"");
    }

    protected String decodeFromBuild(String encoded){
        if(encoded == null)
            return null;
        return encoded.replaceAll("__", ":")
                .replaceAll("--", "/")
                .replaceAll("_-_", "#");
    }
}

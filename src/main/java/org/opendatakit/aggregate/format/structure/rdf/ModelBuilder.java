package org.opendatakit.aggregate.format.structure.rdf;

import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.structure.rdf.models.*;

import java.util.*;

public class ModelBuilder {
    public CellIdentifierModel buildCellIdentifierModel(ColumnModel columnModel, RowModel rowModel) {
        return new CellIdentifierModel(columnModel.columnHeader, rowModel.rowId);
    }

    public TopLevelModel buildTopLevelModel(IForm form, String toplevelIdentifier){
        //Extract the necessary information from the IForm
        //The getters are inconsistent in whether or not they can return null so
        //I'll make sure we have empty Strings instead of null
        String formId = (form.getFormId() == null) ? "" : form.getFormId();
        String name = (form.getViewableName() == null) ? "": form.getViewableName();
        String description = (form.getDescription() == null) ? "" : form.getDescription();
        String creationDate = (form.getCreationDate() == null) ? "" : form.getCreationDate().toString();
        String creationUser = (form.getCreationUser() == null) ? "" : form.getCreationUser();
        String lastUpdate = (form.getLastUpdateDate() == null) ? "" : form.getLastUpdateDate().toString();
        String version = (form.getMajorMinorVersionString() == null) ? "" : form.getMajorMinorVersionString();

        return new TopLevelModel(toplevelIdentifier, formId, name, description, creationDate, creationUser, lastUpdate, version);
    }

    public ColumnModel buildColumnModel(TopLevelModel topLevelModel, String columnHeader, String columnIdentifier){
        return new ColumnModel(topLevelModel, columnHeader, columnIdentifier);
    }

    public RowModel buildRowModel(TopLevelModel topLevelModel, String rowId, String rowIdentifier){
        return new RowModel(topLevelModel, rowId, rowIdentifier);
    }

    public AbstractCellModel buildCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier, FormElementModel.ElementType elementType, Map<String, String> semantics){
        switch(elementType){
            case DECIMAL:
            case INTEGER:
            case STRING:
            case SELECT1:
            case BOOLEAN:
            case METADATA:
                return buildSingleValueCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier, semantics);
            case SELECTN:
                return buildMultiValueCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier, semantics);
            case JRDATE:
                return buildDateCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier, semantics);
            case JRTIME:
                return buildTimeCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier, semantics);
            case JRDATETIME:
                return buildDateTimeCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier, semantics);
            case GEOPOINT:
                return buildGeolocationCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier, semantics);
            case GEOTRACE:
                return buildGeotraceCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier, semantics);
            case GEOSHAPE:
                return buildGeoshapeCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier, semantics);
            default: //TODO Support more types
                return buildSingleValueCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier, semantics);
        }
    }

    private AbstractCellModel buildSingleValueCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier, Map<String, String> semantics) {
        return new SingleValueCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier, semantics);
    }

    private AbstractCellModel buildMultiValueCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier, Map<String, String> semantics) {
        //The values are separated by a space and can not include a  space themselves
        if(cellValue == null)
            return new MultiValueCellModel(columnModel, rowModel, cellEntityIdentifier, semantics);
        String values[] = cellValue.split(" ");
        return new MultiValueCellModel(columnModel, rowModel, new ArrayList<>(Arrays.asList(values)), cellEntityIdentifier, semantics);
    }

    private AbstractCellModel buildDateCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier, Map<String, String> semantics) {
        if(cellValue == null)
            return new DateTimeCellModel(columnModel, rowModel, cellEntityIdentifier, semantics);
        String split[] = cellValue.split(" ", 2);
        return new DateTimeCellModel(columnModel, rowModel, split[0], null, cellEntityIdentifier, semantics); //TODO Might want to check if split.length == 2
    }

    private AbstractCellModel buildTimeCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier, Map<String, String> semantics) {
        if(cellValue == null)
            return new DateTimeCellModel(columnModel, rowModel, cellEntityIdentifier, semantics);
        String split[] = cellValue.split(" ", 2);
        return new DateTimeCellModel(columnModel, rowModel, null, split[1], cellEntityIdentifier, semantics); //TODO Might want to check if split.length == 2
    }

    private AbstractCellModel buildDateTimeCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier, Map<String, String> semantics) {
        if(cellValue == null)
            return new DateTimeCellModel(columnModel, rowModel, cellEntityIdentifier, semantics);
        String split[] = cellValue.split(" ", 2);
        return new DateTimeCellModel(columnModel, rowModel, split[0], split[1], cellEntityIdentifier, semantics); //TODO Might want to check if split.length == 2
    }
    
    private AbstractCellModel buildGeolocationCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier, Map<String, String> semantics) {
        if(cellValue == null)
            return new GeolocationCellModel(columnModel, rowModel, cellEntityIdentifier, semantics);
        String split[] = cellValue.split(", ", 4);
        return new GeolocationCellModel(columnModel, rowModel, split[0], split[1], split[2], split[3], cellEntityIdentifier, semantics);
    }

    private AbstractCellModel buildGeotraceCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier, Map<String, String> semantics) {
        if(cellValue == null)
            return new GeotraceCellModel(columnModel, rowModel, cellEntityIdentifier, semantics);
        String locationStrings[] = cellValue.split(";");
        List<GeotraceElement> pathElements = new ArrayList<>();
        for (int i = 0; i < locationStrings.length; i++) {
            String locationString = locationStrings[i];
            String split[] = locationString.split(" ", 4);
            //If we have less than four elements in split it's because the last location's data was cut off due to the
            //DB field length restriction - so it's an incomplete record and we just discard the last location(s)
            if (split.length >= 4)
                pathElements.add(new GeotraceElement(i + 1, new Location(split[0], split[1], split[2], split[3])));
        }
        return new GeotraceCellModel(columnModel, rowModel, pathElements, cellEntityIdentifier, semantics);
    }

    private AbstractCellModel buildGeoshapeCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier, Map<String, String> semantics) {
        //We reuse the GeotraceCellModel since it stores the same information we need for a Geoshape
        if(cellValue == null)
            return new GeotraceCellModel(columnModel, rowModel, cellEntityIdentifier, semantics);
        String locationStrings[] = cellValue.split(";");
        List<GeotraceElement> pathElements = new ArrayList<>();
        for (int i = 0; i < locationStrings.length; i++) {
            String locationString = locationStrings[i];
            String split[] = locationString.split(" ", 4);
            //If we have less than four elements in split it's because the last location's data was cut off due to the
            //DB field length restriction - so it's an incomplete record and we just discard the last location(s)
            if (split.length >= 4)
                pathElements.add(new GeotraceElement(i + 1, new Location(split[0], split[1], split[2], split[3])));
        }
        return new GeotraceCellModel(columnModel, rowModel, pathElements, cellEntityIdentifier, semantics);
    }




}

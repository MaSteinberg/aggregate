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
        String formId = form.getFormId();
        String name = form.getViewableName();
        String description = form.getDescription();
        String creationDate = form.getCreationDate().toString();
        String creationUser = form.getCreationUser();
        String lastUpdate = form.getLastUpdateDate().toString();
        String version = form.getMajorMinorVersionString();

        return new TopLevelModel(toplevelIdentifier, formId, name, description, creationDate, creationUser, lastUpdate, version);
    }

    public ColumnModel buildColumnModel(TopLevelModel topLevelModel, String columnHeader, FormElementModel.ElementType elementType, String columnIdentifier){
        return new ColumnModel(topLevelModel, columnHeader, columnIdentifier);
    }

    public RowModel buildRowModel(TopLevelModel topLevelModel, List<String> formattedValues, List<FormElementModel> headers, String rowId, String rowIdentifier, boolean requireGuid){
        return new RowModel(topLevelModel, rowId, rowIdentifier);
    }

    public AbstractCellModel buildCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier, FormElementModel.ElementType elementType){
        switch(elementType){
            case DECIMAL:
            case INTEGER:
            case STRING:
            case SELECT1:
            case BOOLEAN:
            case METADATA:
                return buildSingleValueCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier);
            case SELECTN:
                return buildMultiValueCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier);
            case JRDATE:
                return buildDateCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier);
            case JRTIME:
                return buildTimeCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier);
            case JRDATETIME:
                return buildDateTimeCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier);
            case GEOPOINT:
                return buildGeolocationCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier);
            case GEOTRACE:
                return buildGeotraceCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier);
            case GEOSHAPE:
                return buildGeoshapeCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier);
            default: //TODO Support more types
                return buildSingleValueCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier);
        }
    }

    private AbstractCellModel buildGeoshapeCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier) {
        if(cellValue == null)
            return new GeotraceCellModel(columnModel, rowModel, cellEntityIdentifier);
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
        return new GeotraceCellModel(columnModel, rowModel, pathElements, cellEntityIdentifier);
    }

    private AbstractCellModel buildMultiValueCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier) {
        //The values are separated by a space and can not include a  space themselves
        if(cellValue == null)
            return new MultiValueCellModel(columnModel, rowModel, cellEntityIdentifier);
        String values[] = cellValue.split(" ");
        return new MultiValueCellModel(columnModel, rowModel, Arrays.asList(values), cellEntityIdentifier);
    }

    private AbstractCellModel buildGeolocationCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier) {
        if(cellValue == null)
            return new GeolocationCellModel(columnModel, rowModel, cellEntityIdentifier);
        String split[] = cellValue.split(", ", 4);
        return new GeolocationCellModel(columnModel, rowModel, split[0], split[1], split[2], split[3], cellEntityIdentifier);
    }

    private AbstractCellModel buildGeotraceCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier) {
        if(cellValue == null)
            return new GeotraceCellModel(columnModel, rowModel, cellEntityIdentifier);
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
        return new GeotraceCellModel(columnModel, rowModel, pathElements, cellEntityIdentifier);
    }

    private AbstractCellModel buildDateTimeCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier) {
        if(cellValue == null)
            return new DateTimeCellModel(columnModel, rowModel, cellEntityIdentifier);
        String split[] = cellValue.split(" ", 2);
        return new DateTimeCellModel(columnModel, rowModel, split[0], split[1], cellEntityIdentifier); //TODO Might want to check if split.length == 2
    }

    private AbstractCellModel buildDateCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier) {
        if(cellValue == null)
            return new DateTimeCellModel(columnModel, rowModel, cellEntityIdentifier);
        String split[] = cellValue.split(" ", 2);
        return new DateTimeCellModel(columnModel, rowModel, split[0], null, cellEntityIdentifier); //TODO Might want to check if split.length == 2
    }

    private AbstractCellModel buildTimeCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier) {
        if(cellValue == null)
            return new DateTimeCellModel(columnModel, rowModel, cellEntityIdentifier);
        String split[] = cellValue.split(" ", 2);
        return new DateTimeCellModel(columnModel, rowModel, null, split[1], cellEntityIdentifier); //TODO Might want to check if split.length == 2
    }

    private AbstractCellModel buildSingleValueCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, String cellEntityIdentifier) {
        return new SingleValueCellModel(columnModel, rowModel, cellValue, cellEntityIdentifier);
    }
}

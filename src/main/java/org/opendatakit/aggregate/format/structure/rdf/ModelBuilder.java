package org.opendatakit.aggregate.format.structure.rdf;

import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.structure.rdf.models.*;

import java.util.*;

public class ModelBuilder {
    private int rowCounter = 1;

    public TopLevelModel buildTopLevelModel(IForm form){
        //Extract the necessary information from the IForm
        String id = form.getFormId();
        String name = form.getViewableName();
        String description = form.getDescription();
        String creationDate = form.getCreationDate().toString();
        String creationUser = form.getCreationUser();
        String lastUpdate = form.getLastUpdateDate().toString();
        String version = form.getMajorMinorVersionString();

        return new TopLevelModel(id, name, description, creationDate, creationUser, lastUpdate, version);
    }

    public ColumnModel buildColumnModel(TopLevelModel topLevelModel, String columnHeader, FormElementModel.ElementType elementType){
        return new ColumnModel(topLevelModel, columnHeader);
    }

    public RowModel buildRowModel(TopLevelModel topLevelModel, List<String> formattedValues, List<FormElementModel> headers, boolean requireGuid){
        String id = "";
        if(requireGuid){
            //Use the header names to identify the fields that contain row-related metadata
            for(int i = 0; i < headers.size(); i++){
                String header = headers.get(i).getElementName();
                if (header.equals("instanceID"))
                    id = formattedValues.get(i);
            }
        } else{
            //If we don't require globally unique row IDs we can use simple numbers
            id = String.valueOf(rowCounter);
            rowCounter++;
        }

        return new RowModel(topLevelModel, id);
    }

    public AbstractCellModel buildCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue, FormElementModel.ElementType elementType){
        switch(elementType){
            case DECIMAL:
            case INTEGER:
            case STRING:
            case SELECT1:
            case BOOLEAN:
            case METADATA:
                return buildSingleValueCellModel(columnModel, rowModel, cellValue);
            case SELECTN:
                return buildMultiValueCellModel(columnModel, rowModel, cellValue);
            case JRDATE:
                return buildDateCellModel(columnModel, rowModel, cellValue);
            case JRTIME:
                return buildTimeCellModel(columnModel, rowModel, cellValue);
            case JRDATETIME:
                return buildDateTimeCellModel(columnModel, rowModel, cellValue);
            case GEOPOINT:
                return buildGeolocationCellModel(columnModel, rowModel, cellValue);
            case GEOTRACE:
                return buildGeotraceCellModel(columnModel, rowModel, cellValue);
            case GEOSHAPE:
                return buildGeoshapeCellModel(columnModel, rowModel, cellValue);
            default: //TODO Support more types
                return buildSingleValueCellModel(columnModel, rowModel, cellValue);
        }
    }

    private AbstractCellModel buildGeoshapeCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue) {
        if(cellValue == null)
            return new GeotraceCellModel(columnModel, rowModel);
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
        return new GeotraceCellModel(columnModel, rowModel, pathElements);
    }

    private AbstractCellModel buildMultiValueCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue) {
        //The values are separated by a space and can not include a  space themselves
        if(cellValue == null)
            return new MultiValueCellModel(columnModel, rowModel);
        String values[] = cellValue.split(" ");
        return new MultiValueCellModel(columnModel, rowModel, Arrays.asList(values));
    }

    private AbstractCellModel buildGeolocationCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue) {
        if(cellValue == null)
            return new GeolocationCellModel(columnModel, rowModel);
        String split[] = cellValue.split(", ", 4);
        return new GeolocationCellModel(columnModel, rowModel, split[0], split[1], split[2], split[3]);
    }

    private AbstractCellModel buildGeotraceCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue) {
        if(cellValue == null)
            return new GeotraceCellModel(columnModel, rowModel);
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
        return new GeotraceCellModel(columnModel, rowModel, pathElements);
    }

    private AbstractCellModel buildDateTimeCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue) {
        if(cellValue == null)
            return new DateTimeCellModel(columnModel, rowModel);
        String split[] = cellValue.split(" ", 2);
        return new DateTimeCellModel(columnModel, rowModel, split[0], split[1]); //TODO Might want to check if split.length == 2
    }

    private AbstractCellModel buildDateCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue) {
        if(cellValue == null)
            return new DateTimeCellModel(columnModel, rowModel);
        String split[] = cellValue.split(" ", 2);
        return new DateTimeCellModel(columnModel, rowModel, split[0], null); //TODO Might want to check if split.length == 2
    }

    private AbstractCellModel buildTimeCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue) {
        if(cellValue == null)
            return new DateTimeCellModel(columnModel, rowModel);
        String split[] = cellValue.split(" ", 2);
        return new DateTimeCellModel(columnModel, rowModel, null, split[1]); //TODO Might want to check if split.length == 2
    }

    private AbstractCellModel buildSingleValueCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue) {
        return new SingleValueCellModel(columnModel, rowModel, cellValue);
    }
}

package org.opendatakit.aggregate.format.structure.rdf;

import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.structure.rdf.models.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelBuilder {
    private static Map<FormElementModel.ElementType, String> elementTypeToXsdTypeMap = new HashMap<>();
    private int rowCounter = 1;
    //Static initializer for the xsdTypeMap
    static{
        //ElementType.BOOLEAN -> Java type Boolean -> XSD-Type Boolean
        elementTypeToXsdTypeMap.put(FormElementModel.ElementType.BOOLEAN, RdfDataType.BOOLEAN.getXsdTypeValue());
        //ElementType.JRDATETIME -> Java type Java.util.Date -> XSD-Type dateTime
        elementTypeToXsdTypeMap.put(FormElementModel.ElementType.JRDATETIME, RdfDataType.DATE_TIME.getXsdTypeValue());
        //ElementType.JRDATE -> Java type Java.util.Date -> XSD-Type dateTime
        elementTypeToXsdTypeMap.put(FormElementModel.ElementType.JRDATE, RdfDataType.DATE_TIME.getXsdTypeValue());
        //ElementType.JRTIME -> Java type Java.util.Date -> XSD-Type dateTime
        elementTypeToXsdTypeMap.put(FormElementModel.ElementType.JRTIME, RdfDataType.DATE_TIME.getXsdTypeValue());
        //ElementType.INTEGER -> Java type Long -> XSD-Type integer
        elementTypeToXsdTypeMap.put(FormElementModel.ElementType.INTEGER, RdfDataType.INTEGER.getXsdTypeValue());
        //ElementType.DECIMAL -> Java type BigDecimal -> XSD-Type decimal
        elementTypeToXsdTypeMap.put(FormElementModel.ElementType.DECIMAL, RdfDataType.DECIMAL.getXsdTypeValue());
        //ElementType.String -> Java type String -> XSD-Type string
        elementTypeToXsdTypeMap.put(FormElementModel.ElementType.STRING, RdfDataType.STRING.getXsdTypeValue());
        //ElementType.SELECT1 -> Java type String -> XSD-Type String
        elementTypeToXsdTypeMap.put(FormElementModel.ElementType.SELECT1, RdfDataType.STRING.getXsdTypeValue());
        //TODO Proper Mappings for GEOPOINT, GEOTRACE, GEOSHAPE, BINARY, SELECTN, REPEAT, GROUP, METADATA
    }

    private enum RdfDataType{
        STRING("xsd:string"),
        INTEGER("xsd:integer"),
        INT("xsd:int"),
        DECIMAL("xsd:decimal"),
        BOOLEAN("xsd:boolean"),
        DATE_TIME("xsd:dateTime");

        private String xsdType;

        RdfDataType(String value){
            this.xsdType = value;
        }

        public String getXsdTypeValue(){
            return this.xsdType;
        }
    }

    public TopLevelModel buildTopLevelModel(IForm form){
        TopLevelModel topLevelModel = new TopLevelModel();

        //Extract the necessary information from the IForm
        topLevelModel.id = form.getFormId();
        topLevelModel.name = form.getViewableName();
        topLevelModel.description = form.getDescription();
        topLevelModel.creationDate = form.getCreationDate();
        topLevelModel.creationUser = form.getCreationUser();
        topLevelModel.lastUpdate = form.getLastUpdateDate();
        topLevelModel.version = form.getMajorMinorVersionString();

        return topLevelModel;
    }

    public ColumnModel buildColumnModel(TopLevelModel topLevelModel, String columnHeader, FormElementModel.ElementType elementType){
        ColumnModel colModel = new ColumnModel();

        colModel.topLevelModel= topLevelModel;
        colModel.columnHeader = columnHeader;

        //Find the corresponding xsd-Type, defaulting to the type corresponding to STRING
        //TODO: Some of the types may not be empty in xsd!
        if(elementTypeToXsdTypeMap.containsKey(elementType))
            colModel.recommendedXsdDatatype = elementTypeToXsdTypeMap.get(elementType);
        else
            colModel.recommendedXsdDatatype = elementTypeToXsdTypeMap.get(FormElementModel.ElementType.STRING);

        return colModel;
    }

    public RowModel buildRowModel(TopLevelModel topLevelModel, List<String> formattedValues, List<FormElementModel> headers, boolean requireGuid){
        RowModel rowModel = new RowModel();

        rowModel.topLevelModel = topLevelModel;
        if(formattedValues.size() == headers.size()){
            //Use the header names to identify the fields that contain row-related metadata
            for(int i = 0; i < headers.size(); i++){
                String val = formattedValues.get(i);
                if(!isNullOrEmpty(val)){
                    String header = headers.get(i).getElementName();
                    switch (header){
                        case "instanceID":
                            rowModel.rowId = val;
                            break;
                        case "deviceID":
                            rowModel.deviceId = val;
                            break;
                        case "startTime":
                            rowModel.startTime = val;
                            break;
                        case "endTime":
                            rowModel.endTime = val;
                            break;
                        case "today":
                            rowModel.today = val;
                            break;
                        case "username":
                            rowModel.creator = val;
                            break;
                        case "subscriberID":
                            rowModel.subscriberId = val;
                            break;
                        case "simSerial":
                            rowModel.simSerial = val;
                            break;
                        case "phone":
                            rowModel.phone = val;
                            break;
                    }
                }
            }
            //If we don't require globally unique row IDs we can replace them with simpler numbers
            if(!requireGuid){
                rowModel.rowId = String.valueOf(rowCounter);
                rowCounter++;
            }
        }

        return rowModel;
    }

    public AbstractCellModel buildCellModel(TopLevelModel topLevelModel, ColumnModel columnModel, RowModel rowModel, String cellValue, FormElementModel.ElementType elementType){
        switch(elementType){
            case DECIMAL:
            case INTEGER:
            case STRING:
            case SELECT1:
            case BOOLEAN:
            case METADATA:
                return buildSingleValueCellModel(topLevelModel, columnModel, rowModel, cellValue);
            case JRDATE:
                return buildDateCellModel(topLevelModel, columnModel, rowModel, cellValue);
            case JRTIME:
                return buildTimeCellModel(topLevelModel, columnModel, rowModel, cellValue);
            case JRDATETIME:
                return buildDateTimeCellModel(topLevelModel, columnModel, rowModel, cellValue);
            case GEOPOINT:
                return buildGeolocationCellModel(topLevelModel, columnModel, rowModel, cellValue);
            case GEOTRACE:
                return buildGeotraceCellModel(topLevelModel, columnModel, rowModel, cellValue);
            default: //TODO Support more types
                return buildSingleValueCellModel(topLevelModel, columnModel, rowModel, cellValue);
        }
    }

    private AbstractCellModel buildGeotraceCellModel(TopLevelModel topLevelModel, ColumnModel columnModel, RowModel rowModel, String cellValue) {
        String locationStrings[] = cellValue.split(";");
        List<GeopathElement> pathElements = new ArrayList<>();
        for (int i = 0; i < locationStrings.length; i++) {
            String locationString = locationStrings[i];
            String split[] = locationString.split(" ", 4);
            //If we have less than four elements in split it's because the last location's data was cut off due to the
            // DB field length restriction - so it's an incomplete record and we just discard the last location(s)
            if (split.length >= 4)
                pathElements.add(new GeopathElement(i+1, new Location(split[0], split[1], split[2], split[3])));
        }
        return new GeopathCellModel(topLevelModel, columnModel, rowModel, pathElements);
    }

    private AbstractCellModel buildGeolocationCellModel(TopLevelModel topLevelModel, ColumnModel columnModel, RowModel rowModel, String cellValue) {
        String split[] = cellValue.split(", ", 4);
        return new GeolocationCellModel(topLevelModel, columnModel, rowModel, split[0], split[1], split[2], split[3]);
    }

    private AbstractCellModel buildDateTimeCellModel(TopLevelModel topLevelModel, ColumnModel columnModel, RowModel rowModel, String cellValue) {
        String split[] = cellValue.split(" ", 2);
        return new DateTimeCellModel(topLevelModel, columnModel, rowModel, split[0], split[1]); //TODO Might want to check if split.length == 2
    }

    private AbstractCellModel buildDateCellModel(TopLevelModel topLevelModel, ColumnModel columnModel, RowModel rowModel, String cellValue) {
        String split[] = cellValue.split(" ", 2);
        return new DateTimeCellModel(topLevelModel, columnModel, rowModel, split[0], null); //TODO Might want to check if split.length == 2
    }

    private AbstractCellModel buildTimeCellModel(TopLevelModel topLevelModel, ColumnModel columnModel, RowModel rowModel, String cellValue) {
        String split[] = cellValue.split(" ", 2);
        return new DateTimeCellModel(topLevelModel, columnModel, rowModel, null, split[1]); //TODO Might want to check if split.length == 2
    }

    private AbstractCellModel buildSingleValueCellModel(TopLevelModel topLevelModel, ColumnModel columnModel, RowModel rowModel, String cellValue) {
        return new SingleValueCellModel(topLevelModel, columnModel, rowModel, cellValue);
    }

    /*
    https://www.programiz.com/java-programming/examples/string-empty-null
     */
    private static boolean isNullOrEmpty(String str) {
        if(str != null && !str.trim().isEmpty())
            return false;
        return true;
    }
}

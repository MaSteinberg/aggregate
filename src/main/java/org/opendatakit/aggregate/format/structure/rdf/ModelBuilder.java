package org.opendatakit.aggregate.format.structure.rdf;

import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.structure.rdf.models.CellModel;
import org.opendatakit.aggregate.format.structure.rdf.models.ColumnModel;
import org.opendatakit.aggregate.format.structure.rdf.models.RowModel;
import org.opendatakit.aggregate.format.structure.rdf.models.TopLevelModel;

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
            colModel.xsdDatatype = elementTypeToXsdTypeMap.get(elementType);
        else
            colModel.xsdDatatype = elementTypeToXsdTypeMap.get(FormElementModel.ElementType.STRING);

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

    public CellModel buildCellModel(TopLevelModel topLevelModel, ColumnModel columnModel, RowModel rowModel, String cellValue){
        CellModel cellModel = new CellModel();

        cellModel.topLevelModel = topLevelModel;
        cellModel.columnModel = columnModel;
        cellModel.rowModel = rowModel;
        cellModel.cellValue = cellValue;
        
        return cellModel;
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

package org.opendatakit.aggregate.format.structure.rdf;

import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.structure.rdf.models.CellModel;
import org.opendatakit.aggregate.format.structure.rdf.models.ColumnModel;
import org.opendatakit.aggregate.format.structure.rdf.models.RowModel;
import org.opendatakit.aggregate.format.structure.rdf.models.TopLevelModel;

import java.util.List;

public class ModelBuilder {
    public static TopLevelModel buildTopLevelModel(IForm form){
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

    public static ColumnModel buildColumnModel(TopLevelModel topLevelModel, String columnHeader){
        ColumnModel colModel = new ColumnModel();

        colModel.topLevelModel= topLevelModel;
        colModel.columnHeader = columnHeader;

        return colModel;
    }

    public static RowModel buildRowModel(TopLevelModel topLevelModel, List<String> formattedValues, List<FormElementModel> headers){
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
        }

        return rowModel;
    }

    public static CellModel buildCellModel(TopLevelModel topLevelModel, ColumnModel columnModel, RowModel rowModel, String cellValue){
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

package org.opendatakit.aggregate.format.structure.rdf;

import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.structure.rdf.models.CellModel;
import org.opendatakit.aggregate.format.structure.rdf.models.ColumnModel;
import org.opendatakit.aggregate.format.structure.rdf.models.RowModel;
import org.opendatakit.aggregate.format.structure.rdf.models.TopLevelModel;

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

    public static RowModel buildRowModel(TopLevelModel topLevelModel){
        RowModel rowModel = new RowModel();

        rowModel.topLevelModel = topLevelModel;

        return rowModel;
    }

    public static CellModel buildCellModel(TopLevelModel topLevelModel, ColumnModel colModel, RowModel rowModel, String cellValue){
        CellModel cellModel = new CellModel();

        cellModel.topLevelModel = topLevelModel;
        cellModel.colModel = colModel;
        cellModel.rowModel = rowModel;
        cellModel.cellValue = cellValue;
        
        return cellModel;
    }


}

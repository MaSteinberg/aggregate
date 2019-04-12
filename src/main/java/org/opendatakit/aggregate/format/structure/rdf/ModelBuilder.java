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

        return new TopLevelModel(
                toplevelIdentifier,
                formId,
                name,
                description,
                creationDate,
                creationUser,
                lastUpdate,
                version
        );
    }

    public ColumnModel buildColumnModel(TopLevelModel topLevelModel, String columnHeader, String columnIdentifier){
        return new ColumnModel(topLevelModel, columnHeader, columnIdentifier);
    }

    public RowModel buildRowModel(TopLevelModel topLevelModel, String rowId, String rowIdentifier){
        return new RowModel(topLevelModel, rowId, rowIdentifier);
    }

    private class CellModelBuilder {
        public ColumnModel columnModel;
        public RowModel rowModel;
        public String cellValue;
        public String cellEntityIdentifier;
        public FormElementModel.ElementType elementType;
        public Map<String, String> semantics;

        public CellModelBuilder(ColumnModel columnModel, RowModel rowModel, String cellValue,
                                String cellEntityIdentifier, FormElementModel.ElementType elementType,
                                Map<String, String> semantics) {
            this.columnModel = columnModel;
            this.rowModel = rowModel;
            this.cellValue = cellValue;
            this.cellEntityIdentifier = cellEntityIdentifier;
            this.elementType = elementType;
            this.semantics = semantics;
        }

        AbstractCellModel buildSingleValueCellModel() {
            return new SingleValueCellModel(
                this.columnModel, 
                this.rowModel, 
                this.cellValue, 
                this.cellEntityIdentifier, 
                this.semantics
            );
        }

        AbstractCellModel buildMultiValueCellModel() {
            //The values are separated by a space and can not include a space themselves
            if(this.cellValue == null){
                return new MultiValueCellModel(
                    this.columnModel, 
                    this.rowModel, 
                    this.cellEntityIdentifier, 
                    this.semantics
                );
            }
            String values[] = cellValue.split(" ");
            return new MultiValueCellModel(
                this.columnModel, 
                this.rowModel, 
                new ArrayList<>(Arrays.asList(values)), 
                this.cellEntityIdentifier, 
                this.semantics
            );
        }

        AbstractCellModel buildDateCellModel() {
            if(cellValue == null){
                return new DateTimeCellModel(
                    this.columnModel,
                    this.rowModel, 
                    this.cellEntityIdentifier, 
                    this.semantics
                );
            }
            String split[] = cellValue.split(" ", 2);
            return new DateTimeCellModel(
                this.columnModel, 
                this.rowModel, 
                split[0], 
                null, 
                this.cellEntityIdentifier, 
                this.semantics
            );
        }

        AbstractCellModel buildTimeCellModel() {
            if(cellValue == null){
                return new DateTimeCellModel(
                    this.columnModel, 
                    this.rowModel, 
                    this.cellEntityIdentifier,
                    this.semantics
                );
            }
            String split[] = cellValue.split(" ", 2);
            return new DateTimeCellModel(
                this.columnModel, 
                this.rowModel, 
                null, 
                split[1], 
                this.cellEntityIdentifier, 
                this.semantics
            );
        }

        AbstractCellModel buildDateTimeCellModel() {
            if(cellValue == null){
                return new DateTimeCellModel(
                    this.columnModel, 
                    this.rowModel, 
                    this.cellEntityIdentifier, 
                    this.semantics
                );
            }
            String split[] = cellValue.split(" ", 2);
            return new DateTimeCellModel(
                this.columnModel, 
                this.rowModel, 
                split[0], 
                split[1], 
                this.cellEntityIdentifier, 
                this.semantics
            );
        }

        AbstractCellModel buildGeolocationCellModel() {
            if(cellValue == null){
                return new GeolocationCellModel(
                    this.columnModel, 
                    this.rowModel, 
                    this.cellEntityIdentifier, 
                    this.semantics
                );
            }
            String split[] = cellValue.split(", ", 4);
            return new GeolocationCellModel(
                this.columnModel, 
                this.rowModel, 
                split[0], 
                split[1], 
                split[2], 
                split[3], 
                this.cellEntityIdentifier, 
                this.semantics
            );
        }

        AbstractCellModel buildGeotraceCellModel() {
            if(cellValue == null){
                return new GeotraceCellModel(
                    this.columnModel, 
                    this.rowModel, 
                    this.cellEntityIdentifier, 
                    this.semantics);
            }
            String locationStrings[] = cellValue.split(";");
            List<GeotraceElement> pathElements = new ArrayList<>();
            for (int i = 0; i < locationStrings.length; i++) {
                String locationString = locationStrings[i];
                String split[] = locationString.split(" ", 4);
                //If we have less than four elements in split it's because the last location's data was cut off due to the
                //DB field length restriction - so it's an incomplete record and we just discard the last location(s)
                if (split.length >= 4){
                    pathElements.add(
                        new GeotraceElement(i + 1, new Location(split[0], split[1], split[2], split[3]))
                    );
                }
            }
            return new GeotraceCellModel(
                this.columnModel, 
                this.rowModel, 
                pathElements, 
                this.cellEntityIdentifier, 
                this.semantics
            );
        }

        AbstractCellModel buildGeoshapeCellModel() {
            //We reuse the GeotraceCellModel since it stores the same information we need for a Geoshape
            if(cellValue == null){
                return new GeotraceCellModel(
                    this.columnModel, 
                    this.rowModel, 
                    this.cellEntityIdentifier, 
                    this.semantics
                );
            }
            String locationStrings[] = cellValue.split(";");
            List<GeotraceElement> pathElements = new ArrayList<>();
            for (int i = 0; i < locationStrings.length; i++) {
                String locationString = locationStrings[i];
                String split[] = locationString.split(" ", 4);
                //If we have less than four elements in split it's because the last location's data was cut off due to the
                //DB field length restriction - so it's an incomplete record and we just discard the last location(s)
                if (split.length >= 4){
                    pathElements.add(
                        new GeotraceElement(i + 1, new Location(split[0], split[1], split[2], split[3]))
                    );
                }
            }
            return new GeotraceCellModel(
                this.columnModel, 
                this.rowModel, 
                pathElements, 
                this.cellEntityIdentifier, 
                this.semantics
            );
        }
    }

    public AbstractCellModel buildCellModel(ColumnModel columnModel, RowModel rowModel, String cellValue,
                                            String cellEntityIdentifier, FormElementModel.ElementType elementType,
                                            Map<String, String> semantics){
        CellModelBuilder builder = new CellModelBuilder(
                columnModel,
                rowModel,
                cellValue,
                cellEntityIdentifier,
                elementType,
                semantics
        );

        switch(elementType){
            case DECIMAL:
            case INTEGER:
            case STRING:
            case SELECT1:
            case BOOLEAN:
            case METADATA:
                return builder.buildSingleValueCellModel();
            case SELECTN:
                return builder.buildMultiValueCellModel();
            case JRDATE:
                return builder.buildDateCellModel();
            case JRTIME:
                return builder.buildTimeCellModel();
            case JRDATETIME:
                return builder.buildDateTimeCellModel();
            case GEOPOINT:
                return builder.buildGeolocationCellModel();
            case GEOTRACE:
                return builder.buildGeotraceCellModel();
            case GEOSHAPE:
                return builder.buildGeoshapeCellModel();
            default:
                return builder.buildSingleValueCellModel();
        }
    }
}

package org.opendatakit.aggregate.format.structure.rdf.models;

import java.util.List;

public class GeopathCellModel extends AbstractCellModel {
    List<GeopathElement> locationList;

    public GeopathCellModel(TopLevelModel topLevelModel, ColumnModel columnModel, RowModel rowModel, List<GeopathElement> locationList) {
        super(topLevelModel, columnModel, rowModel);
        this.locationList = locationList;
    }
}

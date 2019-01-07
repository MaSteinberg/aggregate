package org.opendatakit.aggregate.format.structure.rdf.models;

import java.util.List;
import java.util.Map;

public class GeotraceCellModel extends AbstractCellModel {
    List<GeotraceElement> locationList;

    public GeotraceCellModel(ColumnModel columnModel, RowModel rowModel, List<GeotraceElement> locationList, String cellEntityIdentifier, Map<String, String> semantics) {
        super(columnModel, rowModel, cellEntityIdentifier, semantics);
        this.locationList = locationList;
    }

    public GeotraceCellModel(ColumnModel columnModel, RowModel rowModel, String cellEntityIdentifier, Map<String, String> semantics) {
        super(columnModel, rowModel, cellEntityIdentifier, semantics);
        this.locationList = null;
    }
}

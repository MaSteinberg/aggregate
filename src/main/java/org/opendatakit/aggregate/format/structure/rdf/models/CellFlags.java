package org.opendatakit.aggregate.format.structure.rdf.models;

public class CellFlags {
    public boolean isFirstCellOfColumn;
    public boolean isLastCellOfColumn;
    public boolean isFirstCellOfRow;
    public boolean isLastCellOfRow;

    public CellFlags(boolean isFirstCellOfColumn, boolean isFirstCellOfRow, boolean isLastCellOfRow) {
        this.isFirstCellOfColumn = isFirstCellOfColumn;
        this.isFirstCellOfRow = isFirstCellOfRow;
        this.isLastCellOfRow = isLastCellOfRow;
    }
}

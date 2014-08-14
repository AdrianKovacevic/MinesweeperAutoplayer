package com.company;

import java.util.ArrayList;

/**
 * @author Adrian Kovacevic akovacev@uwaterloo.ca Created by Adrian Kovacevic on August 7, 2014
 */

public class Cell {

    private byte value;
    private int[] coordinates;
    private ArrayList<int[]> surroundingUnknownCoordinates;
    private int numSurroundingFlags;

    /**
     * The constructor of a Cell states that its row and column must be given, as well as its value, as this information
     * is always present when reading a cell at a certain position
     *
     * @param cellValue The value of the cell, CELL_ONE to CELL_SEVEN for numbers, CELL_FLAG for a flag, CELL_BLANK for
     *                  a blank cell that would have 0 mines around it, and CELL_UNKNOWN for a cell which has not been
     *                  clicked
     * @param row       The row that the cell resides on
     * @param column    The column that the cell resides on
     */

    public Cell(byte cellValue, int row, int column) {
        value = cellValue;
        coordinates = new int[2];
        coordinates[0] = row;
        coordinates[1] = column;
        surroundingUnknownCoordinates = new ArrayList<int[]>();
        numSurroundingFlags = 0;
    }

    public byte getValue() {
        return value;
    }

    public int[] getCoordinates() {
        return coordinates;
    }

    /**
     * Clears the surroundingUnknownCoordinates list, likely used if there is a problem with the the number of flags
     * around a mine and it removes all flags and it has new unknowns, or if the unknowns have later changed to flags
     */
    public void clearSurroundingUnknownCoordinates() {
        surroundingUnknownCoordinates.clear();
    }

    /**
     * Adds a set of coordinates of a surrounding unknown of a cell to the list
     *
     * @param coordinates The array of coordinates of an unknown cell, in (row, column) format
     */

    public void addSurroundingUnknownCoordinates(int[] coordinates) {
        surroundingUnknownCoordinates.add(coordinates);
    }

    public int getNumSurroundingUnknown() {
        return surroundingUnknownCoordinates.size();
    }

    /**
     * Returns the coordinates of an unknown square in the list
     *
     * @param index The index of the list at which the unknown coordinates are located
     * @return Returns the coordinates of an unknown square in the list
     */
    public int[] getSurroundingUnknownCoordinates(int index) {
        return surroundingUnknownCoordinates.get(index);
    }

    public int getNumSurroundingFlags() {
        return numSurroundingFlags;
    }

    public void setNumSurroundingFlags(int numFlags) {
        numSurroundingFlags = numFlags;
    }

    /**
     * Creates a new cell that contains the same value, row, and column as the current one
     *
     * @return Returns the new copy of this cell
     */

    public Cell copyOf() {
        Cell copy = new Cell(this.value, this.coordinates[0], this.coordinates[1]);
        copy.setNumSurroundingFlags(this.numSurroundingFlags);
        for (int i = 0; i < surroundingUnknownCoordinates.size(); i++) {
            copy.addSurroundingUnknownCoordinates(surroundingUnknownCoordinates.get(i));
        }
        return copy;
    }
}

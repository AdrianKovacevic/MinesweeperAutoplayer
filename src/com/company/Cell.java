package com.company;

import java.util.ArrayList;

/**
 * Created by coop-tester on 07/08/2014.
 */
public class Cell {

    private byte value;
    private int[] coordinates;
    private ArrayList<int[]> surroundingUnknownCoordinates;
    private int numSurroundingFlags;

    public Cell (byte cellValue, int row, int column) {
        value = cellValue;
        coordinates = new int[2];
        coordinates[0] = row;
        coordinates[1] = column;
        surroundingUnknownCoordinates = new ArrayList<int[]>();
        numSurroundingFlags = 0;
    }

    public byte getValue () {
        return value;
    }

    public int[] getCoordinates () {
        return coordinates;
    }

    public void clearSurroundingUnknownCoordinates () {
        surroundingUnknownCoordinates.clear();
    }

    public void addSurroundingUnknownCoordinates (int[] coordinates) {
        surroundingUnknownCoordinates.add(coordinates);
    }

    public int getNumSurroundingUnknown () {
        return surroundingUnknownCoordinates.size();
    }

    public int[] getSurroundingUnknownCoordinates (int index) {
        return surroundingUnknownCoordinates.get(index);
    }

    public int getNumSurroundingFlags () {
        return numSurroundingFlags;
    }

    public void setNumSurroundingFlags (int numFlags) {
        numSurroundingFlags = numFlags;
    }

    public Cell copyOf () {
        Cell copy = new Cell(this.value, this.coordinates[0], this.coordinates[1]);
        copy.setNumSurroundingFlags(this.numSurroundingFlags);
        for (int i = 0; i < surroundingUnknownCoordinates.size(); i++) {
            copy.addSurroundingUnknownCoordinates(surroundingUnknownCoordinates.get(i));
        }
        return copy;
    }


}

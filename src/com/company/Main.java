package com.company;

import com.google.common.primitives.Doubles;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Properties;


/**
 * @author Adrian Kovacevic akovacev@uwaterloo.ca
 * Created by Adrian Kovacevic on June 17, 2014 Created in IntelliJ IDEA
 */

public class Main {

    static final byte CELL_UNKNOWN = 0;
    static final byte CELL_ONE = 1;
    static final byte CELL_TWO = 2;
    static final byte CELL_THREE = 3;
    static final byte CELL_FOUR = 4;
    static final byte CELL_FIVE = 5;
    static final byte CELL_SIX = 6;
    static final byte CELL_SEVEN = 7;
    static final byte CELL_BLANK = 11;
    static final byte CELL_FLAG = 15;

    /**
     * Goes to all adjacent cells in the array to see how many flags and unknown cells there are
     *
     * @param mineGrid The grid on which a cell is checked for its surrounding flags and unknown cells
     * @param row      The row that the cell resides on
     * @param column   The column that the cell resides on
     */

    private static void findNumSurroundingFlagsAndUnkown(Cell[][] mineGrid, int row, int column) {
        int numFlags = 0;
        mineGrid[row][column].clearSurroundingUnknownCoordinates();

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {

                int testCol = j + column;
                int testRow = i + row;

                if (testRow >= 0 && testRow < mineGrid.length && testCol >= 0 && testCol < mineGrid[0].length) {
                    if (mineGrid[testRow][testCol].getValue() == CELL_UNKNOWN) {
                        int[] coordinates = { testRow, testCol };
                        mineGrid[row][column].addSurroundingUnknownCoordinates(coordinates);
                    } else if (mineGrid[testRow][testCol].getValue() == CELL_FLAG) {
                        numFlags++;
                    }
                }
            }
        }

        mineGrid[row][column].setNumSurroundingFlags(numFlags);

        return;
    }

    /**
     * Checks to see if the mine grid is solved, used as a helper function for the algorithm that tries all permutations
     * of leftover mines in the available squares
     *
     * @param testMineGrid The mine grid that is being checked if it is valid
     * @return Returns true if the mine grid does not break any rules of Minesweeper, and false if it does
     */

    private static boolean isMineGridPossible(Cell[][] testMineGrid) {
        int rowSize = testMineGrid.length;
        int columnSize = testMineGrid[0].length;

        for (int row = 0; row < rowSize; row++) {
            for (int column = 0; column < columnSize; column++) {
                if (testMineGrid[row][column].getValue() != CELL_UNKNOWN
                        && testMineGrid[row][column].getValue() != CELL_BLANK
                        && testMineGrid[row][column].getValue() != CELL_FLAG) {
                    findNumSurroundingFlagsAndUnkown(testMineGrid, row, column);

                    if (testMineGrid[row][column].getValue() - testMineGrid[row][column].getNumSurroundingFlags()
                            != 0) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Goes through the mine grid and fixes any cells that have been accidentally incorrectly flagged. Not needed in
     * normal scenarios, but if the delay is very low then it may make moves based on analysing an incorrect screenshot
     *
     * @param screen Passes in the screen object that contains a screenshot and a 2D array of Cell objects that
     *               represent the minesweeper grid
     * @throws InterruptedException An exception for the sleep method
     */

    private static void fixIncorrectFlags (Screen screen) throws InterruptedException {
        Cell[][] mineGrid = screen.getMineGrid();
        int rowSize = mineGrid.length;
        int columnSize = mineGrid[0].length;

        for (int row = 0; row < rowSize; row++) {
            for (int column = 0; column < columnSize; column++) {

                if (mineGrid[row][column].getValue() != CELL_UNKNOWN && mineGrid[row][column].getValue() != CELL_FLAG && mineGrid[row][column].getValue() != CELL_BLANK) {
                    findNumSurroundingFlagsAndUnkown(mineGrid, row, column);
                    if (mineGrid[row][column].getValue() < mineGrid[row][column].getNumSurroundingFlags()) {
                        // made a mistake, unflag everything

                        System.out.println(
                                "Made a mistake. Unflagging around a " + mineGrid[row][column].getValue() + " at row " + row
                                        + " and column " + column + ". Numflags: " +
                                        mineGrid[row][column].getNumSurroundingFlags() + ". " + "Numunknowns: "
                                        + mineGrid[row][column].getNumSurroundingUnknown());

                        for (int i = -1; i < 2; i++) {
                            for (int j = -1; j < 2; j++) {
                                int testCol = j + column;
                                int testRow = i + row;

                                if (testRow >= 0 && testRow < rowSize && testCol >= 0 && testCol < columnSize) {
                                    if (mineGrid[testRow][testCol].getValue() == CELL_FLAG) {
                                        int[] tilePos = screen.getTilePos(testRow, testCol);
                                        screen.robot.mouseMove(tilePos[0], tilePos[1]);
                                        screen.robot.mousePress(InputEvent.BUTTON3_MASK);
                                        Thread.sleep(10);
                                        screen.robot.mouseRelease(InputEvent.BUTTON3_MASK);
                                    }
                                }
                            }
                        }

                        screen.robot.mouseMove(screen.getMineGridTopCornerX() - 10, screen.getMineGridTopCornerY() - 10);
                    }
                }


            }
        }
    }


    /**
     * Performs the next move, whether it is starting off the game, restarting the game if the game over popup appeared,
     * making a guaranteed move by process of elimination, solving a system of equations to either click or flag mines,
     * or to make a thought-out guess if needed
     *
     * @param screen Passes in the screen object that contains a screenshot and a 2D array of Cell objects that
     *               represent the minesweeper grid
     * @throws InterruptedException                  Throws an exception in the executor in the screen object, to let
     *                                               the thread know when it has finished
     * @throws GetWindowRect.GetWindowRectException  Throws an exception if the window coordinates cannot be found
     * @throws GetWindowRect.WindowNotFoundException Throws an exception if the window does not exist
     */

    private static void doNextMove(Screen screen)
            throws InterruptedException, GetWindowRect.GetWindowRectException, GetWindowRect.WindowNotFoundException {

        int totalMines = screen.getTotalMines();
        byte rowSize = screen.getRowSize();
        byte columnSize = screen.getColumnSize();

        double[][] guessingGrid = new double[rowSize][columnSize];

        for (int i = 0; i < rowSize; i++) {
            Arrays.fill(guessingGrid[i], -1.0);
        }

        boolean isEmpty = true;
        boolean gameOver;
        //        long startTime = System.nanoTime();

        gameOver = screen.fillMineGrid();

        //        long endTime = System.nanoTime();
        //        double duration = (endTime - startTime) / 1000000000.0;
        //        System.out.println("The mineGrid filling duration is: " + duration);

        //        startTime = System.nanoTime();

        if (gameOver) {
            System.out.println("Game over!");
            screen.robot.mouseMove(20, 20);
//            screen.robot.keyPress(KeyEvent.VK_ALT);
//            Thread.sleep(50);
//            screen.robot.keyPress(KeyEvent.VK_P);
//            Thread.sleep(50);
//            screen.robot.keyRelease(KeyEvent.VK_P);
//            Thread.sleep(50);
//            screen.robot.keyRelease(KeyEvent.VK_ALT);

            screen.robot.keyPress(KeyEvent.VK_ENTER);
            Thread.sleep(50);
            screen.robot.keyRelease(KeyEvent.VK_ENTER);
            Thread.sleep(1000);
            return;
        }

        Cell[][] mineGrid = screen.getMineGrid();

        // fills all the unknown cells in the guessing grid as a 0.
        // in this way, all cells that do not have any unknown cells around it will never be guessed on, and when it
        // comes to guessing around a cell, if there are no cells that can be guessed around, it will click on the
        // unknown cells to handle the case where a cell is boxed off by mines

        for (int row = 0; row < rowSize; row++) {
            for (int column = 0; column < columnSize; column++) {
                if (mineGrid[row][column].getValue() == CELL_UNKNOWN) {
                    guessingGrid[row][column] = 0.0;
                }
            }
        }

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < columnSize; x++) {
                if (mineGrid[y][x].getValue() != CELL_UNKNOWN) {
                    isEmpty = false;
                }
            }
        }

        if (isEmpty) {
            Random rand = new Random();
            // clicks a random cell, but does not touch the edges as they are usually bad to start off with
            int randomX =
                    rand.nextInt(((screen.getMineGridBottomCornerX() - 40) - (screen.getMineGridTopCornerX()) + 40)) + (
                            screen.getMineGridTopCornerX() + 40);
            int randomY =
                    rand.nextInt(((screen.getMineGridBottomCornerY() - 40) - (screen.getMineGridTopCornerY()) + 40)) + (
                            screen.getMineGridTopCornerY() + 40);

            screen.robot.mouseMove(randomX, randomY);
            screen.robot.mousePress(InputEvent.BUTTON1_MASK);
            Thread.sleep(10);
            screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
            // Extra delay as it would sometimes guess twice in the beginning or make poor moves because the first move
            // still has not been registered correctly
            Thread.sleep(100);
            return;
        } else {

            // when timed, this function is relatively inexpensive, taking less than 0.1 ms if called and nothing
            // has to be fixed. Also, it is better than the alternative of adding an else if to the following
            // for loops, as it checks for errors on the entire screen before committing to a move, whereas adding an
            // else if would only fix cells up until a certain point, allowing for errors
            // Although not necessary, the speed causes the program to be a bit unstable at times so fixing errors
            // can improve the reliability in the long run
            fixIncorrectFlags(screen);

            // this first pair of loops checks all cells for guaranteed moves, and keeps track of any possible guesses
            // the next loop checks for the cell with the least number of guesses, and performs the guesses on it
            for (int row = 0; row < rowSize; row++) {
                for (int column = 0; column < columnSize; column++) {
                    // iterates through the grid to click all unknown cells if all mines have been flagged
                    // otherwise, it goes to a numbered cell and counts the adjacent flags and unknown cells

                    if (screen.getNumFlaggedMines() == totalMines && mineGrid[row][column].getValue() == CELL_UNKNOWN) {
                        int[] tilePos = screen.getTilePos(row, column);
                        screen.robot.mouseMove(tilePos[0], tilePos[1]);
                        screen.robot.mousePress(InputEvent.BUTTON1_MASK);
                        Thread.sleep(10);
                        screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
                        //                        endTime = System.nanoTime();
                        //                        duration = (endTime - startTime) / 1000000000.0;
                        //                        System.out.println("The duration is: " + duration);
                        return;
                    } else if (mineGrid[row][column].getValue() != CELL_UNKNOWN
                            && mineGrid[row][column].getValue() != CELL_BLANK
                            && mineGrid[row][column].getValue() != CELL_FLAG) {

                        // goes to all adjacent cells in the array to see how many flags and unknown cells there are

                        findNumSurroundingFlagsAndUnkown(mineGrid, row, column);

                        int numUnknown = mineGrid[row][column].getNumSurroundingUnknown();
                        int numFlags = mineGrid[row][column].getNumSurroundingFlags();

                        // if the number of adjacent flags is equal to the number, it expands the cell if there are
                        // unknown squares adjacent to it

                        if (mineGrid[row][column].getValue() == numFlags && numUnknown != 0) {
                            // expand the tile since possible
                            System.out.println(
                                    "Expanding around a " + mineGrid[row][column].getValue() + " at row " + row
                                            + " and column " + column + ". Numflags: " + numFlags + ". "
                                            + "Numunknowns: " + numUnknown);

                            int[] tilePos = screen.getTilePos(row, column);
                            screen.robot.mouseMove(tilePos[0], tilePos[1]);
                            screen.robot.mousePress(InputEvent.BUTTON2_MASK);
                            Thread.sleep(10);
                            screen.robot.mouseRelease(InputEvent.BUTTON2_MASK);
                            //                            endTime = System.nanoTime();
                            //                            duration = (endTime - startTime) / 1000000000.0;
                            //                            System.out.println("The duration is: " + duration);
                            return;

                            // otherwise, if not all of the number's adjacent cells have been flagged, but there are
                            // the same number of unknown cells as leftover mines, then it flags all of the unknown cells
                        } else if (mineGrid[row][column].getValue() - numFlags == numUnknown && numUnknown != 0) {
                            System.out.println(
                                    "Flagging around a " + mineGrid[row][column].getValue() + " at row " + row
                                            + " and column " + column + ". Numflags: " + numFlags + ". "
                                            + "Numunknowns: " + numUnknown);

                            // mark all surrounding tiles with flags
                            for (int i = -1; i < 2; i++) {
                                for (int j = -1; j < 2; j++) {
                                    int testCol = j + column;
                                    int testRow = i + row;

                                    if (testRow >= 0 && testRow < rowSize && testCol >= 0 && testCol < columnSize) {
                                        if (mineGrid[testRow][testCol].getValue() == CELL_UNKNOWN) {
                                            int[] tilePos = screen.getTilePos(testRow, testCol);
                                            screen.robot.mouseMove(tilePos[0], tilePos[1]);
                                            screen.robot.mousePress(InputEvent.BUTTON3_MASK);
                                            Thread.sleep(10);
                                            screen.robot.mouseRelease(InputEvent.BUTTON3_MASK);
                                        }
                                    }
                                }
                            }

                            screen.robot.mouseMove(screen.getMineGridTopCornerX() - 10,
                                    screen.getMineGridTopCornerY() - 10);
                            //                            endTime = System.nanoTime();
                            //                            duration = (endTime - startTime) / 1000000000.0;
                            //                            System.out.println("The duration is: " + duration);
                            return;

                            // if it can not expand a cell or flag it using the information that is given, then it
                            // must take a guess. here it will compute how many guesses are necessary around the cell

                        } else if (numUnknown != 0) {
                            int numMines = mineGrid[row][column].getValue() - numFlags;
                            double chanceOfCorrectGuess = 1.0 - ((double) numMines / (double) numUnknown);
                            guessingGrid[row][column] = chanceOfCorrectGuess;
                        }
                    }
                }
            }

            // algorithm that doesn't guess yet, it uses matrices to deduce all of the squares that must be mines,
            // and the ones that must not be
            // uses the guessing grid as it has the valuable information of numbered cells that are adjacent to more
            // than one unknown square
            // uses the guessing grid to check for all non unknown squares because the numbered cells that can be
            // guessed on are also the cells that can give information to solve other cells (using a system of
            // equations)

            long testStart = System.nanoTime();

            ArrayList<Cell> matrixColumnCells = new ArrayList<Cell>();
            ArrayList<ArrayList<Double>> dynamicMatrix = new ArrayList<ArrayList<Double>>();
            ArrayList<Double> matrixConstants = new ArrayList<Double>();

            ArrayList<int[]> allUnknownCoordinates = new ArrayList<int[]>();

            for (int row = 0; row < rowSize; row++) {
                for (int column = 0; column < columnSize; column++) {
                    if (mineGrid[row][column].getValue() == CELL_UNKNOWN) {
                        allUnknownCoordinates.add(mineGrid[row][column].getCoordinates());
                    }
                }
            }
            // add in a row with all unknowns equal to total number of mines left
            // must add in matrix column cells for new unknown coordinates
            // add 0s to all other rows

            int numCellsSurroundedWithUnknowns = 0;

            for (int row = 0; row < rowSize; row++) {
                for (int column = 0; column < columnSize; column++) {

                    if (guessingGrid[row][column] > 0.01) {
                        dynamicMatrix.add(new ArrayList<Double>());
                        if (dynamicMatrix.size() != 0) {
                            for (int i = 0; i < dynamicMatrix.get(0).size(); i++) {
                                dynamicMatrix.get(numCellsSurroundedWithUnknowns).add(0.0);
                            }
                        }

                        for (int i = 0; i < mineGrid[row][column].getNumSurroundingUnknown(); i++) {
                            int[] unknownCoordinates = mineGrid[row][column].getSurroundingUnknownCoordinates(i);
                            int unknownRow = unknownCoordinates[0];
                            int unknownColumn = unknownCoordinates[1];

                            if (!matrixColumnCells.contains(mineGrid[unknownRow][unknownColumn])) {
                                matrixColumnCells.add(mineGrid[unknownRow][unknownColumn]);
                                for (int j = 0; j < dynamicMatrix.size(); j++) {
                                    dynamicMatrix.get(j).add(0.0);
                                }
                            }

                            int indexOfCurrentUnknownCell =
                                    matrixColumnCells.indexOf(mineGrid[unknownRow][unknownColumn]);
                            dynamicMatrix.get(numCellsSurroundedWithUnknowns).set(indexOfCurrentUnknownCell, 1.0);
                        }

                        matrixConstants.add((double) (mineGrid[row][column].getValue() - mineGrid[row][column]
                                .getNumSurroundingFlags()));

                        numCellsSurroundedWithUnknowns++;
                    }
                }
            }

            try {
                if (allUnknownCoordinates.size() <= 20) {
                    for (int i = 0; i < allUnknownCoordinates.size(); i++) {
                        int unknownRow = allUnknownCoordinates.get(i)[0];
                        int unknownColumn = allUnknownCoordinates.get(i)[1];

                        if (!matrixColumnCells.contains(mineGrid[unknownRow][unknownColumn])) {
                            matrixColumnCells.add(mineGrid[unknownRow][unknownColumn]);
                            for (int j = 0; j < dynamicMatrix.size(); j++) {
                                dynamicMatrix.get(j).add(0.0);
                            }
                        }
                    }

                    ArrayList<Double> allCellsEquation = new ArrayList<Double>();

                    for (int i = 0; i < dynamicMatrix.get(0).size(); i++) {
                        allCellsEquation.add(1.0);
                    }

                    dynamicMatrix.add(allCellsEquation);
                    matrixConstants.add((double) (totalMines - screen.getNumFlaggedMines()));
                }

                double[][] matrix = new double[dynamicMatrix.size()][dynamicMatrix.get(0).size() + 1];
                for (int row = 0; row < dynamicMatrix.size(); row++) {
                    dynamicMatrix.get(row).add(matrixConstants.get(row));
                    matrix[row] = Doubles.toArray(dynamicMatrix.get(row));
                }

                DenseMatrix64F matrixRREF = CommonOps.rref(new DenseMatrix64F(matrix), -1, null);

                //                matrixRREF.print();
                //
                //                for (int i = 0; i < matrixColumnCells.size(); i++) {
                //                    System.out.print(matrixColumnCells.get(i).getCoordinates()[0] + " " + matrixColumnCells.get(i).getCoordinates()[1] + " ");
                //                }
                //                System.out.print("\n");

                System.out.println(
                        "Matrix creation and reduction took: " + (double) (System.nanoTime() - testStart) / 1000000000.0
                                + " seconds.");

                Thread.sleep(1);

                // apply moves to possible solutions

                boolean solvedSomething = false;

                for (int matrixRow = 0; matrixRow < matrixRREF.getNumRows(); matrixRow++) {

                    ArrayList<Integer> simplifiedRow = new ArrayList<Integer>();
                    ArrayList<Cell> simplifiedRowCells = new ArrayList<Cell>();

                    for (int matrixColumn = 0; matrixColumn < matrixRREF.getNumCols() - 1; matrixColumn++) {
                        if (!(Math.abs(matrixRREF.get(matrixRow, matrixColumn)) < 0.001)) {
                            simplifiedRow.add((int) matrixRREF.get(matrixRow, matrixColumn));
                            if ((int) matrixRREF.get(matrixRow, matrixColumn) == 0) {
                                throw new RuntimeException("The change from double to integer went wrong!");
                            }

                            simplifiedRowCells.add(matrixColumnCells.get(matrixColumn));
                        }
                    }

                    if (simplifiedRow.size() > 0) {

                        int numPermutations = (int) Math.pow(2, simplifiedRow.size());
                        int[][] equationPermutations = new int[numPermutations][simplifiedRow.size()];
                        int[][] equationPermutations2 = new int[numPermutations][simplifiedRow.size()];

                        int consecutiveEquivalentDigits = numPermutations / 2;

                        // since the only possibilities of the values of the unknown cells is mine or not mine, they belong
                        // to a set of {0, 1}
                        // this is a binary set, so finding all permutations of this can be done by enumerating all numbers
                        // in binary with the number of bits equal to the number of non-zero entries in the matrix row
                        // equation
                        // could also make a 2d array of all numbers from 0 to 2 ^ (number of non-zero entries) - 1 in
                        // binary with an integer to binary function and multiply each column by its corresponding
                        // coefficient to correct the sign

                        for (int column = 0; column < simplifiedRow.size(); column++) {
                            for (int row = 0; row < numPermutations; ) {
                                for (int i = 0; i < consecutiveEquivalentDigits; i++) {
                                    equationPermutations[row][column] = simplifiedRow.get(column);
                                    row++;
                                }
                                for (int i = 0; i < consecutiveEquivalentDigits; i++) {
                                    equationPermutations[row][column] = 0;
                                    row++;
                                }
                            }
                            consecutiveEquivalentDigits /= 2;
                        }

                        ArrayList<int[]> solvedRow = new ArrayList<int[]>();

                        for (int row = 0; row < numPermutations; row++) {
                            int addAllColumns = 0;

                            for (int column = 0; column < simplifiedRow.size(); column++) {
                                addAllColumns += equationPermutations[row][column];
                            }

                            if (addAllColumns == (int) matrixRREF.get(matrixRow, matrixRREF.getNumCols() - 1)) {
                                solvedRow.add(equationPermutations[row]);
                            }
                        }

                        if (solvedRow.size() == 1) {
                            solvedSomething = true;
                            for (int column = 0; column < solvedRow.get(0).length; column++) {
                                // click the empty cells
                                if (solvedRow.get(0)[column] == 0) {
                                    Cell solvedCell = simplifiedRowCells.get(column);
                                    int cellRow = solvedCell.getCoordinates()[0];
                                    int cellColumn = solvedCell.getCoordinates()[1];
                                    System.out.println("Solved a cell at row: " + cellRow + " and column: " + cellColumn
                                            + ". It is not a mine.");
                                    int[] tilePos = screen.getTilePos(cellRow, cellColumn);
                                    screen.robot.mouseMove(tilePos[0], tilePos[1]);
                                    screen.robot.mousePress(InputEvent.BUTTON1_MASK);
                                    Thread.sleep(10);
                                    screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);

                                    // flag the cells with mines
                                } else {
                                    Cell solvedCell = simplifiedRowCells.get(column);
                                    int cellRow = solvedCell.getCoordinates()[0];
                                    int cellColumn = solvedCell.getCoordinates()[1];
                                    System.out.println("Solved a cell at row: " + cellRow + " and column: " + cellColumn
                                            + ". It is a mine.");
                                    int[] tilePos = screen.getTilePos(cellRow, cellColumn);
                                    screen.robot.mouseMove(tilePos[0], tilePos[1]);
                                    screen.robot.mousePress(InputEvent.BUTTON3_MASK);
                                    Thread.sleep(10);
                                    screen.robot.mouseRelease(InputEvent.BUTTON3_MASK);
                                    screen.robot.mouseMove(screen.getMineGridTopCornerX() - 10,
                                            screen.getMineGridTopCornerY() - 10);
                                    Thread.sleep(10);
                                }
                            }
                        }
                    }
                }

                if (solvedSomething) {
                    return;
                }

                // possibly reduce all of the data type transformations to improve speed
            } catch (IndexOutOfBoundsException e) {
                // this is to prevent an exception from being thrown if the program is reading the mine grid
                // in an intermediate frame (animation of a popup has not resolved)
                // either the exception can be thrown and caught harmlessly, or the program can be slowed down
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw e;
            }

            // put a function that only activates during low numbers of mines left, and goes through each permutation
            // of putting the mines on the board and checks if they are valid. If there's only one permutation, then
            // do those moves

            // the permutation code is now just there as a test to see if it will find anything in addition to the
            // new code which just adds another equation to the matrix that uses the total number of mines left

            ArrayList<ICombinatoricsVector<Integer>> numPossibleOptions =
                    new ArrayList<ICombinatoricsVector<Integer>>();

            if (allUnknownCoordinates.size() <= 20) {
                // the number of permutations of a variable number of mines given a constant number of squares is
                // directly related to the rows in Pascal's triangle, for the given number of squares
                // the equation relating the number of permutations to Pascal's triangle is:
                // numPermutations = PascalsTriangle[unknownCoordinates.size()][unknownCoordinates.size() - numMinesLeft];

                int numMinesLeft = screen.getTotalMines() - screen.getNumFlaggedMines();

                Integer[] initialPermutation = new Integer[allUnknownCoordinates.size()];

                Arrays.fill(initialPermutation, 0);

                // each mine is a 1, each empty square is a 0

                try {
                    for (int i = 0; i < numMinesLeft; i++) {
                        initialPermutation[i] = 1;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // if it tries to do this algorithm on an intermediate frame, it will given an exception.
                    // in normal conditions, it will not give an exception because the number of unknown coordinates
                    // will always be greater than the number of mines left
                }

                ICombinatoricsVector<Integer> initialVector = Factory.createVector(initialPermutation);

                // Create the generator
                Generator<Integer> generator = Factory.createPermutationGenerator(initialVector);

                List<ICombinatoricsVector<Integer>> permutations = generator.generateAllObjects();

                for (int i = 0; i < permutations.size(); i++) {
                    Cell[][] testMineGrid = new Cell[rowSize][columnSize];
                    for (int row = 0; row < rowSize; row++) {
                        for (int column = 0; column < columnSize; column++) {
                            testMineGrid[row][column] = mineGrid[row][column].copyOf();
                        }
                    }

                    for (int j = 0; j < allUnknownCoordinates.size(); j++) {
                        if (permutations.get(i).getValue(j) == 1) {
                            int curCellRow = allUnknownCoordinates.get(j)[0];
                            int curCellColumn = allUnknownCoordinates.get(j)[1];

                            testMineGrid[curCellRow][curCellColumn] = new Cell(CELL_FLAG, curCellRow, curCellColumn);
                        }
                    }

                    if (isMineGridPossible(testMineGrid)) {
                        numPossibleOptions.add(permutations.get(i));
                    }
                }

                if (numPossibleOptions.size() == 1) {
                    for (int i = 0; i < numPossibleOptions.get(0).getSize(); i++) {
                        if (numPossibleOptions.get(0).getValue(i) == 0) {
                            int cellRow = allUnknownCoordinates.get(i)[0];
                            int cellColumn = allUnknownCoordinates.get(i)[1];
                            System.out.println("Solved a cell through the permutation algorithm at row: " + cellRow
                                    + " and column: " + cellColumn + ". It is not a mine.");
                            int[] tilePos = screen.getTilePos(cellRow, cellColumn);
                            screen.robot.mouseMove(tilePos[0], tilePos[1]);
                            screen.robot.mousePress(InputEvent.BUTTON1_MASK);
                            Thread.sleep(10);
                            screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
                        }
                    }
                }
            }

            // iterate through the guessing chances for each number that is not able to be expanded, look for the
            // highest probability equal or less than 50%, and guess once on it. return, and check to see if anything
            // new opened up that does not have to be guessedp
            // 50% or less prevents guessing on something like a 1 with 8 empty cells around it'

            int[] highestGuessingChanceCell = new int[2];
            double highestGuessingChance = -0.5;

            int[] absoluteHighestGuessingChanceCell = new int[2];
            double absoluteHighestGuessingChance = -0.5;

            for (int row = 0; row < rowSize; row++) {
                for (int column = 0; column < columnSize; column++) {
                    // 0.51 is to prevent any sort of oddity including imprecise double values

                    if (highestGuessingChance < guessingGrid[row][column] && guessingGrid[row][column] <= 0.51) {
                        highestGuessingChance = guessingGrid[row][column];
                        highestGuessingChanceCell[0] = row;
                        highestGuessingChanceCell[1] = column;
                    }

                    if (absoluteHighestGuessingChance < guessingGrid[row][column]) {
                        absoluteHighestGuessingChance = guessingGrid[row][column];
                        absoluteHighestGuessingChanceCell[0] = row;
                        absoluteHighestGuessingChanceCell[1] = column;
                    }
                }
            }

            int guessRow;
            int guessColumn;

            // if the guess it would like to make is on an unknown cell and there exists another cell cell that
            // can be possibly guessed on, then it goes with the other cell
            // only guesses on unknown if there are no other guessing possibilities

            if (highestGuessingChance < 0.01 && absoluteHighestGuessingChance > 0.49) {
                guessRow = absoluteHighestGuessingChanceCell[0];

                guessColumn = absoluteHighestGuessingChanceCell[1];
            } else {
                guessRow = highestGuessingChanceCell[0];
                guessColumn = highestGuessingChanceCell[1];
            }

            System.out.println(
                    "Making a guess around a " + mineGrid[guessRow][guessColumn].getValue() + " at row " + guessRow
                            + " and column " + guessColumn + ". The chance of guessing correctly is: "
                            + guessingGrid[guessRow][guessColumn] * 100.0 + "%.");

            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    int testRow = i + guessRow;
                    int testCol = j + guessColumn;

                    if (testRow >= 0 && testRow < rowSize && testCol >= 0 && testCol < columnSize) {
                        if (mineGrid[testRow][testCol].getValue() == CELL_UNKNOWN) {
                            int[] tilePos = screen.getTilePos(testRow, testCol);
                            screen.robot.mouseMove(tilePos[0], tilePos[1]);
                            screen.robot.mousePress(InputEvent.BUTTON1_MASK);
                            Thread.sleep(10);
                            screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
                            Thread.sleep(10);
                            //                                endTime = System.nanoTime();
                            //                                duration = (endTime - startTime) / 1000000000.0;
                            //                                System.out.println("The duration is: " + duration);
                            //                                Thread.sleep(150);
                            return;
                        }
                    }
                }
            }

            //            endTime = System.nanoTime();
            //            duration = (endTime - startTime) / 1000000000.0;
            //            System.out.println("The duration is: " + duration);

            return;
        }
    }

    /**
     * A method that starts Minesweeper, re-sizes it as small as possible, moves it away from the edge of the screen to
     * prevent clipping of the popups, un-checks potentially disruptive options, and changes the grid colour to blue
     *
     * @param screen              Passes in the screen object that contains a screenshot and a 2D array of Cell objects
     *                            that represent the minesweeper grid
     * @param minesweeperLocation Passes in the path to MineSweeper.exe as it is read from MinesweeperAP.properties
     * @throws InterruptedException                  Throws an exception in the executor in the screen object, to let
     *                                               the thread know when it has finished
     * @throws GetWindowRect.GetWindowRectException  Throws an exception if the window coordinates cannot be found
     * @throws GetWindowRect.WindowNotFoundException Throws an exception if the window does not exist
     */

    public static void configureScreen(Screen screen, String minesweeperLocation)
            throws InterruptedException, GetWindowRect.WindowNotFoundException, GetWindowRect.GetWindowRectException {

        // runs minesweeper
        try {
            if (minesweeperLocation.charAt(0) == '\"'
                    && minesweeperLocation.charAt(minesweeperLocation.length() - 1) == '\"') {
                Runtime.getRuntime().exec("cmd.exe /C Start \"\" " + minesweeperLocation);
            } else {
                Runtime.getRuntime().exec("cmd.exe /C Start \"\" \"" + minesweeperLocation + "\"");
            }
        } catch (IOException e) {
            System.err.println("Couldn't run minesweeper! Check the file path in the MinesweeperAP.properties file.");
            e.printStackTrace();
            return;
        }

        // add setup that changes to smallest windows size , changes to advanced, and changes to blue squares
        // add support for other difficulties
        // add better window tracking capabilities across Windows 8 and non-aero versions of the game
        // add multithreading

        Thread.sleep(1500);

        // gets the game in a standard position on the screen, so that it is not partially off the screen

        screen.robot.keyPress(KeyEvent.VK_WINDOWS);
        screen.robot.keyPress(KeyEvent.VK_UP);
        screen.robot.keyRelease(KeyEvent.VK_UP);
        screen.robot.keyPress(KeyEvent.VK_LEFT);
        screen.robot.keyRelease(KeyEvent.VK_LEFT);
        screen.robot.keyRelease(KeyEvent.VK_WINDOWS);

        int[] rect;

        try {
            rect = GetWindowRect.getRect("Minesweeper");
        } catch (GetWindowRect.WindowNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (GetWindowRect.GetWindowRectException e) {
            e.printStackTrace();
            return;
        }

        // resizes the windows to its smallest size

        screen.robot.mouseMove(rect[2] - 5, rect[3] - 5);

        screen.robot.mousePress(InputEvent.BUTTON1_MASK);
        screen.robot.mouseMove(20, 20);
        screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);

        Thread.sleep(500);

        try {
            rect = GetWindowRect.getRect("Minesweeper");
        } catch (GetWindowRect.WindowNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (GetWindowRect.GetWindowRectException e) {
            e.printStackTrace();
            return;
        }

        // moves the window to the right so that the popup windows don't get cut off

        screen.robot.mouseMove(rect[0] + 30, rect[1] + 15);

        screen.robot.mousePress(InputEvent.BUTTON1_MASK);
        screen.robot.mouseMove(rect[0] + 150, rect[1] + 15);
        screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);

        // gets the line under Game and Help to appear, if they have not already

        screen.robot.keyPress(KeyEvent.VK_ALT);
        screen.robot.keyPress(KeyEvent.VK_ENTER);
        screen.robot.keyRelease(KeyEvent.VK_ENTER);
        screen.robot.keyRelease(KeyEvent.VK_ALT);

        // brings up the options popup

        screen.robot.keyPress(KeyEvent.VK_F5);
        screen.robot.keyRelease(KeyEvent.VK_F5);

        Thread.sleep(500);

        try {
            rect = GetWindowRect.getRect("Minesweeper");
        } catch (GetWindowRect.WindowNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (GetWindowRect.GetWindowRectException e) {
            e.printStackTrace();
            return;
        }

        Thread.sleep(500);

        // checks the checkboxes if they are not unchecked (better colour detection to see if not checked rather than if
        // they are)
        // removes the options of animations as they slow the game, tips as they can accidentally interfere with the
        // screenshot, and question marks as they can also accidentally be activated when they are never intended

        Color checkBoxColor = screen.robot.getPixelColor(rect[0] + 147, rect[1] + 228);

        if (!(checkBoxColor.getRed() >= 170 && checkBoxColor.getGreen() >= 180 && checkBoxColor.getBlue() >= 190)) {
            screen.robot.mouseMove(rect[0] + 147, rect[1] + 228);
            screen.robot.mousePress(InputEvent.BUTTON1_MASK);
            Thread.sleep(50);
            screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
        }

        checkBoxColor = screen.robot.getPixelColor(rect[0] + 147, rect[1] + 276);

        if (!(checkBoxColor.getRed() >= 170 && checkBoxColor.getGreen() >= 180 && checkBoxColor.getBlue() >= 190)) {
            screen.robot.mouseMove(rect[0] + 147, rect[1] + 276);
            screen.robot.mousePress(InputEvent.BUTTON1_MASK);
            Thread.sleep(50);
            screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
        }

        checkBoxColor = screen.robot.getPixelColor(rect[0] + 147, rect[1] + 348);

        if (!(checkBoxColor.getRed() >= 170 && checkBoxColor.getGreen() >= 180 && checkBoxColor.getBlue() >= 190)) {
            screen.robot.mouseMove(rect[0] + 147, rect[1] + 348);
            screen.robot.mousePress(InputEvent.BUTTON1_MASK);
            Thread.sleep(50);
            screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
        }

        Thread.sleep(200);

        screen.robot.keyPress(KeyEvent.VK_ENTER);
        screen.robot.keyRelease(KeyEvent.VK_ENTER);

        Thread.sleep(50);

        // changes the grid to blue as green is not supported

        screen.robot.keyPress(KeyEvent.VK_F7);
        screen.robot.keyRelease(KeyEvent.VK_F7);

        Thread.sleep(50);

        screen.robot.mouseMove(rect[0] + 110, rect[1] + 265);
        screen.robot.mousePress(InputEvent.BUTTON1_MASK);
        Thread.sleep(50);
        screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
        screen.robot.keyPress(KeyEvent.VK_LEFT);
        screen.robot.keyRelease(KeyEvent.VK_LEFT);
        screen.robot.keyPress(KeyEvent.VK_LEFT);
        screen.robot.keyRelease(KeyEvent.VK_LEFT);
        screen.robot.keyPress(KeyEvent.VK_ENTER);
        screen.robot.keyRelease(KeyEvent.VK_ENTER);

        screen.robot.mouseMove(20, 20);

        Thread.sleep(400);

        try {
            screen.findMineGridCorners();
        } catch (GetWindowRect.WindowNotFoundException e) {
            e.printStackTrace();
            throw e;
        } catch (GetWindowRect.GetWindowRectException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static Properties setDefaultProperties() {
        Properties properties = new Properties();
        OutputStream output = null;

        try {

            output = new FileOutputStream("MinesweeperAP.properties");

            properties.setProperty("minesweeper_location",
                    "\"%programfiles%\\Microsoft Games\\Minesweeper\\minesweeper.exe\"");
            properties.setProperty("delay_between_moves(ms)", "50");

            properties.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return properties;
    }

    /**
     * The main method, which creates a screen object, configures the Minesweeper window, and repeatedly performs the
     * next move infinitely, as long as it is still running.
     *
     * @param args Should be no arguments
     * @throws InterruptedException Throws an exception in the executor in the screen object, to let the thread know
     *                              when it has finished
     */

    public static void main(String[] args) throws InterruptedException {
        InputStream input = null;
        Properties properties;

        System.out.println(
                "Press CTRL + ALT + D to end the program at any time as it is performing the Minesweeper moves.");

        try {
            input = new FileInputStream("MinesweeperAP.properties");
            properties = new Properties();
            properties.load(input);
        } catch (FileNotFoundException e) {
            System.err.println("No properties file found. Creating a default one.");
            properties = setDefaultProperties();
        } catch (IOException e) {
            e.printStackTrace();
            properties = setDefaultProperties();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Screen screen;
        try {
            screen = new Screen();
        } catch (AWTException e) {
            e.printStackTrace();
            return;
        }

        try {
            configureScreen(screen, properties.getProperty("minesweeper_location"));
        } catch (GetWindowRect.WindowNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (GetWindowRect.GetWindowRectException e) {
            e.printStackTrace();
            return;
        }

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }

        GlobalScreen.getInstance().addNativeKeyListener(new GlobalKeyListener());

        try {
            while (GlobalScreen.isNativeHookRegistered()) {

                WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, "Minesweeper"); // window title
                if (hwnd == null) {
                    System.err.println(
                            "Minesweeper is not running! Make sure that the path is correct and that all single backslahses in the properties file are double backslashes.");
                    return;
                } else {
                    // makes sure it is not minimized
                    User32.INSTANCE.ShowWindow(hwnd, 9);
                }

                doNextMove(screen);
                // Delay between the moves helps the animations settle for accurate screenshots. If the delay is high,
                // the moves will be slower but will be less prone to doing moves before something like the game over
                // popup appears or a flag/numbered cell is rendered. The lower the delay, the faster the moves will
                // be performed

                Thread.sleep(Long.parseLong(properties.getProperty("delay_between_moves(ms)")));
            }
        } catch (RuntimeException e) {
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (GetWindowRect.GetWindowRectException e) {
            e.printStackTrace();
            return;
        } catch (GetWindowRect.WindowNotFoundException e) {
            e.printStackTrace();
            return;
        }
    }
}

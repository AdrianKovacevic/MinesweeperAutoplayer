package com.company;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Adrian Kovacevic akovacev@uwaterloo.ca Created by Adrian Kovacevic on June 18, 2014
 */

public class Screen {

    private final byte CELL_UNKNOWN = 0;
    private final byte CELL_ONE = 1;
    private final byte CELL_TWO = 2;
    private final byte CELL_THREE = 3;
    private final byte CELL_FOUR = 4;
    private final byte CELL_FIVE = 5;
    private final byte CELL_SIX = 6;
    private final byte CELL_SEVEN = 7;
    private final byte CELL_BLANK = 11;
    private final byte CELL_FLAG = 15;
    private final byte CELL_SIDE_LENGTH = 18;

    private final int[] CELL_ONE_COLOUR = { 61, 80, 190 };
    private final int[] CELL_TWO_COLOUR = { 30, 103, 2 };
    private final int[] CELL_THREE_COLOUR = { 170, 12, 12 };
    private final int[] CELL_FOUR_COLOUR = { 1, 1, 130 };
    private final int[] CELL_FIVE_COLOUR = { 129, 10, 10 };
    private final int[] CELL_SIX_COLOUR = { 5, 125, 122 };
    private final int[] CELL_SEVEN_COLOUR = { 173, 15, 15 };
    private final int[] CELL_BLANK_COLOUR = { 172, 181, 212 };
    private final int[] CELL_FLAG_COLOUR = { 161, 159, 162 };
    private final int[] GAMEOVER_COLOUR = { 240, 240, 240 };

    private Cell[][] mineGrid;

    private BufferedImage screenShot;

    private int screenWidth;

    private int screenHeight;

    public Robot robot;

    private int mineGridTopCornerX;

    private int mineGridTopCornerY;

    private int mineGridBottomCornerX;

    private int mineGridBottomCornerY;

    private byte numFlags;

    private boolean isGameOver;

    private byte rowSize;

    private byte columnSize;

    private int totalMines;

    private ArrayList<Integer> tasksPerThread;

    private final Lock lock = new ReentrantLock();

    /**
     * An object which stores a screenshot of the screen and a 2D array that represents the minesweeper grid, along with
     * functions that analyze the grid values
     *
     * @throws AWTException Throws an exception if a new Robot object could not be created
     */

    public Screen() throws AWTException {
        screenWidth = 0;
        screenHeight = 0;
        mineGridTopCornerX = 0;
        mineGridTopCornerY = 0;
        mineGridBottomCornerX = 0;
        mineGridBottomCornerY = 0;
        numFlags = 0;
        screenShot = null;
        isGameOver = false;
        tasksPerThread = new ArrayList<Integer>();

        try {
            robot = new Robot();
        } catch (AWTException e) {
            System.err.println("Couldn't make a robot!");
            e.printStackTrace();
            throw e;
        }
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public BufferedImage getScreenShot() {
        return screenShot;
    }

    public Cell[][] getMineGrid() {
        return mineGrid;
    }

    public int getMineGridTopCornerY() {
        return mineGridTopCornerY;
    }

    public int getMineGridTopCornerX() {
        return mineGridTopCornerX;
    }

    public int getMineGridBottomCornerY() {
        return mineGridBottomCornerY;
    }

    public int getMineGridBottomCornerX() {
        return mineGridBottomCornerX;
    }

    public byte getNumFlaggedMines() {
        return numFlags;
    }

    public int getTotalMines() {
        return totalMines;
    }

    public byte getRowSize() {
        return rowSize;
    }

    public byte getColumnSize() {
        return columnSize;
    }

    /**
     * Gets the number of tasks each thread will have to complete
     */

    private void getTasksPerThread() {
        int numCores = Runtime.getRuntime().availableProcessors();
        int totalTasks = rowSize * columnSize;
        int minNumThreadsPerCore = totalTasks / numCores;
        int remainingThreads = totalTasks % numCores;

        for (int i = 0; i < numCores; i++) {
            tasksPerThread.add(minNumThreadsPerCore);
        }

        for (int i = 0; i < remainingThreads; i++) {
            tasksPerThread.set(i, tasksPerThread.get(i) + 1);
        }

        return;
    }

    /**
     * Inherits from Runnable for each thread to use to complete its task of filling the mineGrid array in parallel.
     * Detects multiple pixels in each cell to be able to determine its value, and puts a cell with the corresponding
     * value, row, and column in the mine grid array
     */

    public class MineGridFillThread implements Runnable {
        private final int termStartIndex;
        private final int numTerms;

        MineGridFillThread(int termStartIndex, int numTerms) {
            this.termStartIndex = termStartIndex;
            this.numTerms = numTerms;
        }

        @Override
        public void run() {

            int startRow = termStartIndex / columnSize;
            int endRow = (termStartIndex + numTerms) / columnSize;
            int startColumn = termStartIndex % columnSize;
            int termsLeft = numTerms;

            int column = startColumn;

            // loops from its starting cell to its end cell, and analyzes certain pixels in each cell
            // 17 and 13 are offset values from the top corner of the mine grid to a certain pixel on each cell, to
            // best determine what the cell contains

            for (int row = startRow; row <= endRow; row++) {

                int y = mineGridTopCornerY + 17 + (CELL_SIDE_LENGTH * row);

                while (column != columnSize && termsLeft != 0) {

                    column = column % columnSize;

                    int x = mineGridTopCornerX + 13 + (CELL_SIDE_LENGTH * column);

                    Color colour = new Color(screenShot.getRGB(x, y));

                    int[] colourRGBValues = new int[3];

                    colourRGBValues[0] = colour.getRed();
                    colourRGBValues[1] = colour.getGreen();
                    colourRGBValues[2] = colour.getBlue();

                    Color colourAbove = new Color(screenShot.getRGB(x, y - 1));

                    int[] colourAboveRGBValues = new int[3];

                    colourAboveRGBValues[0] = colourAbove.getRed();
                    colourAboveRGBValues[1] = colourAbove.getGreen();
                    colourAboveRGBValues[2] = colourAbove.getBlue();

                    Color colourRight = new Color(screenShot.getRGB(x + 1, y));

                    int[] colourRightRGBValues = new int[3];

                    colourRightRGBValues[0] = colourRight.getRed();
                    colourRightRGBValues[1] = colourRight.getGreen();
                    colourRightRGBValues[2] = colourRight.getBlue();

                    Color colourFarBelow = new Color(screenShot.getRGB(x, y + 2));

                    int[] colourFarBelowRGBValues = new int[3];

                    colourFarBelowRGBValues[0] = colourFarBelow.getRed();
                    colourFarBelowRGBValues[1] = colourFarBelow.getGreen();
                    colourFarBelowRGBValues[2] = colourFarBelow.getBlue();

                    int gameOverPopup = 0;

                    Color colourLeft = new Color(screenShot.getRGB(x - 2, y));

                    int[] colourLeftRGBValues = new int[3];

                    colourLeftRGBValues[0] = colourLeft.getRed();
                    colourLeftRGBValues[1] = colourLeft.getGreen();
                    colourLeftRGBValues[2] = colourLeft.getBlue();

                    // checks to see if it is game over by analyzing the background of the popup that appears
                    // if 25 of the exact same grey pixels are present, then it sets isGameOver to true

                    if (isColourMatch(colourRGBValues, GAMEOVER_COLOUR, 0)) {
                        gameOverPopup += 1;
                        for (int i = 1; i < 25; i++) {
                            Color gameOverColour = new Color(screenShot.getRGB(x + i, y));
                            int[] gameOverColourRGBValues = new int[3];
                            gameOverColourRGBValues[0] = gameOverColour.getRed();
                            gameOverColourRGBValues[1] = gameOverColour.getGreen();
                            gameOverColourRGBValues[2] = gameOverColour.getBlue();
                            if (isColourMatch(gameOverColourRGBValues, GAMEOVER_COLOUR, 0)) {
                                gameOverPopup += 1;
                            }
                        }
                        if (gameOverPopup == 25) {
                            lock.lock();
                            isGameOver = true;
                            lock.unlock();

                            break;
                        }
                    }

                    if (!isGameOver) {
                        // using different pixels to tell what the cell contains. If it does not contain any of these
                        // values, then it is left as unknown (only other option besides the unimportant 8, which is
                        // handled in the doNextMove method by clicking on isolated squares once there are no
                        // moves left)

                        if (isColourMatch(colourLeftRGBValues, CELL_SEVEN_COLOUR, 30) && colourRGBValues[0] >= 140
                                && colourRGBValues[1] >= 100 && colourRGBValues[2] >= 100
                                && colourRightRGBValues[0] >= 140 && colourRightRGBValues[1] >= 140
                                && colourRightRGBValues[2] >= 180) {
                            Cell cell = new Cell(CELL_SEVEN, row, column);
                            mineGrid[row][column] = cell;
                        } else if (isColourMatch(colourRightRGBValues, CELL_FOUR_COLOUR, 15)) {
                            Cell cell = new Cell(CELL_FOUR, row, column);
                            mineGrid[row][column] = cell;
                        } else if (isColourMatch(colourRGBValues, CELL_THREE_COLOUR, 35) || isColourMatch(
                                colourAboveRGBValues, CELL_THREE_COLOUR, 35)) {
                            Cell cell = new Cell(CELL_THREE, row, column);
                            mineGrid[row][column] = cell;
                        } else if (Math.abs(colourRGBValues[0] - 62) < 4 && Math.abs(colourRGBValues[1] - 80) < 2
                                && Math.abs(colourRGBValues[2] - 189) < 2 && colourFarBelowRGBValues[0] >= 150
                                && colourFarBelowRGBValues[1] >= 150) {
                            Cell cell = new Cell(CELL_ONE, row, column);
                            mineGrid[row][column] = cell;
                        } else if (colourRGBValues[0] <= 230 && colourRGBValues[0] >= 160 && colourRGBValues[1] <= 245
                                && colourRGBValues[1] >= 170 && colourRGBValues[2] <= 255
                                && colourRGBValues[2] >= 200) {
                            Cell cell = new Cell(CELL_BLANK, row, column);
                            mineGrid[row][column] = cell;
                        } else if (isColourMatch(colourRGBValues, CELL_TWO_COLOUR, 30)) {
                            Cell cell = new Cell(CELL_TWO, row, column);
                            mineGrid[row][column] = cell;
                        } else if (isColourMatch(colourRGBValues, CELL_FIVE_COLOUR, 30) || isColourMatch(
                                colourAboveRGBValues, CELL_FIVE_COLOUR, 30)) {
                            Cell cell = new Cell(CELL_FIVE, row, column);
                            mineGrid[row][column] = cell;
                        } else if (isColourMatch(colourRGBValues, CELL_SIX_COLOUR, 30) || isColourMatch(
                                colourAboveRGBValues, CELL_SIX_COLOUR, 30)) {
                            Cell cell = new Cell(CELL_SIX, row, column);
                            mineGrid[row][column] = cell;
                        } else if (isColourMatch(colourRGBValues, CELL_FLAG_COLOUR, 35)) {
                            numFlags++;
                            Cell cell = new Cell(CELL_FLAG, row, column);
                            mineGrid[row][column] = cell;
                        }
                    }

                    // move on to next cell, by moving the x coordinate exactly one cell length further

                    column++;
                    termsLeft--;
                }

                if (termsLeft == 0 || isGameOver) {
                    break;
                }

                column = column % columnSize;
            }
        }
    }

    /**
     * Uses the Euclidean distance between 2 colours to tell how similar they are. If it is under a tolerance, it will
     * return true to signify a match.
     *
     * @param givenColourRGB    The RGB colour array to be tested (index 0 is red, 1 is green, 2 is blue)
     * @param expectedColourRGB The expected value of the tested colour
     * @param tolerance         The allowed tolerance that the colours can differ by
     * @return Returns true if the Euclidean colour difference is equal or less than the given tolerance, false
     * otherwise.
     */
    private static boolean isColourMatch(int[] givenColourRGB, int[] expectedColourRGB, double tolerance) {

        double colourDifferenceResultant;
        double redDifferenceSquared;
        double greenDifferenceSquared;
        double blueDifferenceSquared;

        redDifferenceSquared = Math.pow(givenColourRGB[0] - expectedColourRGB[0], 2);
        greenDifferenceSquared = Math.pow(givenColourRGB[1] - expectedColourRGB[1], 2);
        blueDifferenceSquared = Math.pow(givenColourRGB[2] - expectedColourRGB[2], 2);

        colourDifferenceResultant = Math.sqrt(redDifferenceSquared + greenDifferenceSquared + blueDifferenceSquared);

        return (colourDifferenceResultant <= tolerance);
    }

    /**
     * Finds the corners of the Minesweeper window and the mine grid for the mineGrid array Calls the getTasksPerThread
     * method as this can only be called after the number of cells is found
     *
     * @throws GetWindowRect.GetWindowRectException  Throws an exception if the window coordinates cannot be found
     * @throws GetWindowRect.WindowNotFoundException Throws an exception if the window does not exist
     */

    public void findMineGridCorners()
            throws GetWindowRect.WindowNotFoundException, GetWindowRect.GetWindowRectException {
        screenShot = null;
        takeScreenshot();

        int[] rect;

        try {
            rect = GetWindowRect.getRect("Minesweeper");
        } catch (GetWindowRect.WindowNotFoundException e) {
            e.printStackTrace();
            throw e;
        } catch (GetWindowRect.GetWindowRectException e) {
            e.printStackTrace();
            throw e;
        }

        int minesweeperTopCornerX = rect[0];
        int minesweeperTopCornerY = rect[1];
        int minesweeperBottomCornerX = rect[2];
        int minesweeperBottomCornerY = rect[3];

        int mineSweeperHeight = minesweeperBottomCornerY - minesweeperTopCornerY;
        int mineSweeperWidth = minesweeperBottomCornerX - minesweeperTopCornerX;

        // decides which game type it is played based on the size of the minesweeper window

        if (mineSweeperHeight > 320) {
            if (mineSweeperWidth > 500) {
                // advanced
                System.out.println("Playing on advanced.");
                rowSize = 16;
                columnSize = 30;
                totalMines = 99;
            } else {
                // intermediate
                System.out.println("Playing on intermediate.");
                rowSize = 16;
                columnSize = 16;
                totalMines = 40;
            }
        } else {
            // beginner
            System.out.println("Playing on beginner.");
            rowSize = 9;
            columnSize = 9;
            totalMines = 10;
        }

        int numConsecutiveBlackPixels = 0;

        // looks for 8 consecutive black pixels, which comes first at the line under the G in "Game"

        for (int y = minesweeperTopCornerY; y < minesweeperBottomCornerY; y++) {
            for (int x = minesweeperTopCornerX; x < minesweeperBottomCornerX; x++) {
                Color pixel = new Color(screenShot.getRGB(x, y));

                numConsecutiveBlackPixels = 0;

                if (pixel.getRed() <= 10 && pixel.getGreen() <= 10 && pixel.getBlue() <= 10) {
                    //                    robot.mouseMove(x, y);
                    numConsecutiveBlackPixels++;
                    for (int i = 1; i < 8; i++) {
                        Color pixelBeside = new Color(screenShot.getRGB(x + i, y));
                        if (pixelBeside.getRed() <= 10 && pixelBeside.getGreen() <= 10 && pixelBeside.getBlue() <= 10) {
                            numConsecutiveBlackPixels++;
                        }
                    }
                }

                if (numConsecutiveBlackPixels == 8) {
                    mineGridTopCornerX = x + 21;
                    mineGridTopCornerY = y + 33;
                    break;
                }
            }

            if (numConsecutiveBlackPixels == 8) {
                break;
            }
        }

        // if finding the 8 black pixel row under Game failed, use a pre-determined distance from the corner
        // doesn't always work in different configurations, such as non-aero Windows or Windows 8
        if (numConsecutiveBlackPixels != 8) {
            mineGridTopCornerX = rect[0] + 36;
            mineGridTopCornerY = rect[1] + 78;
        }

        mineGrid = new Cell[rowSize][columnSize];

        mineGridBottomCornerX = rect[2] - 37;
        mineGridBottomCornerY = rect[3] - 40;

        getTasksPerThread();
    }

    /**
     * Runs the threads in parallel to analyze the screenshot and fill the mine grid array
     *
     * @return Returns true if the game is over (found a game over popup) and false if not
     * @throws GetWindowRect.GetWindowRectException  Throws an exception if the window coordinates cannot be found
     * @throws GetWindowRect.WindowNotFoundException Throws an exception if the window does not exist
     * @throws InterruptedException                  Throws an exception in the executor to let the thread know when it
     *                                               has finished
     */
    public boolean fillMineGrid()
            throws GetWindowRect.WindowNotFoundException, GetWindowRect.GetWindowRectException, InterruptedException {
        screenShot = null;

        takeScreenshot();

        // fills the mine grid with all unknown cells, as they are unknown unless something else is detected
        // also prevents the grid from holding values when a new game is started

        for (int row = 0; row < rowSize; row++) {
            for (int column = 0; column < columnSize; column++) {
                mineGrid[row][column] = new Cell(CELL_UNKNOWN, row, column);
            }
        }

        numFlags = 0;
        isGameOver = false;

        ExecutorService executor = Executors.newFixedThreadPool(tasksPerThread.size());

        int firstTermIndex = 0;
        for (int i = 0; i < tasksPerThread.size(); i++) {
            Runnable worker = new MineGridFillThread(firstTermIndex, tasksPerThread.get(i));
            firstTermIndex += tasksPerThread.get(i);

            executor.execute(worker);
        }
        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }

        return isGameOver;
    }

    /**
     * Returns the coordinates of an area that can be clicked on for a cell at a certain row and column
     *
     * @param row    The row of the cell to be clicked on
     * @param column The column of the cell to be clicked on
     * @return Returns screen coordinates in (x, y) format in an integer array of 2 elements of the location of the
     * given cell
     */

    public int[] getTilePos(int row, int column) {
        int[] tilePos = new int[2];
        tilePos[0] = mineGridTopCornerX + 6 + (CELL_SIDE_LENGTH * column);
        tilePos[1] = mineGridTopCornerY + 6 + (CELL_SIDE_LENGTH * row);

        return tilePos;
    }

    /**
     * Takes a screenshot without the cursor in it
     */

    private void takeScreenshot() {
        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        screenShot = robot.createScreenCapture(screenSize);
        screenWidth = screenSize.width;
        screenHeight = screenSize.height;

        return;
    }
}

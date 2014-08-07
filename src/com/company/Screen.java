package com.company;


import javax.imageio.ImageIO;

import static org.junit.Assert.assertArrayEquals;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Screen {

    final byte ROW_SIZE = 16;
    final byte COLUMN_SIZE = 30;
    final byte CELL_UNKNOWN = 0;
    final byte CELL_ONE = 1;
    final byte CELL_TWO = 2;
    final byte CELL_THREE = 3;
    final byte CELL_FOUR = 4;
    final byte CELL_FIVE = 5;
    final byte CELL_SIX = 6;
    final byte CELL_SEVEN = 7;
    final byte CELL_BLANK = 11;
    final byte CELL_FLAG = 15;
    final byte CELL_SIDE_LENGTH = 18;


    final int[] CELL_ONE_COLOUR = {61, 80, 190};
    final int[] CELL_TWO_COLOUR = {30, 103, 2};
    final int[] CELL_THREE_COLOUR = {170, 12, 12};
    final int[] CELL_FOUR_COLOUR = {1, 1, 130};
    final int[] CELL_FIVE_COLOUR = {129, 10, 10};
    final int[] CELL_SIX_COLOUR = {5, 125, 122};
    final int[] CELL_SEVEN_COLOUR = {173, 4, 5};
    final int[] CELL_BLANK_COLOUR = {172, 181, 212};
    final int[] CELL_FLAG_COLOUR = {161, 159, 162};
    final int[] GAMEOVER_COLOUR = {240, 240, 240};

    private byte[][] mineGrid;

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

    final private ArrayList<Integer> tasksPerThread;

    final private Lock lock = new ReentrantLock();

    // bug fixing purposes

    public ArrayList<String> one;
    public ArrayList<String> two;
    public ArrayList<String> three;
    public ArrayList<String> four;
    public ArrayList<String> five;
    public ArrayList<String> six;
    public ArrayList<String> seven;
    public ArrayList<String> blank;
    public ArrayList<String> flag;


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
        one = new ArrayList<String>();
        two = new ArrayList<String>();
        three = new ArrayList<String>();
        four = new ArrayList<String>();
        five = new ArrayList<String>();
        six = new ArrayList<String>();
        seven = new ArrayList<String>();
        blank = new ArrayList<String>();
        flag = new ArrayList<String>();
        tasksPerThread = new ArrayList<Integer>();

        getTasksPerThread(tasksPerThread);

        mineGrid = new byte[ROW_SIZE][COLUMN_SIZE];
        try {
            robot = new Robot();
        } catch (AWTException e) {
            System.out.println("Couldn't make a robot!");
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


    public byte[][] getMineGrid() {
        return mineGrid;
    }



    public int getMineGridTopCornerY() {
        return mineGridTopCornerY;
    }


    public int getMineGridTopCornerX() {
        return mineGridTopCornerX;
    }


    public int getMineGridBottomCornerY () {
        return mineGridBottomCornerY;
    }

    public int getMineGridBottomCornerX () {
        return mineGridBottomCornerX;
    }

    public byte getNumFlaggedMines() {
        return numFlags;
    }


    private void getTasksPerThread(ArrayList<Integer> tasksPerThread) {
        int numCores = Runtime.getRuntime().availableProcessors();
//        int numCores = 7;
        int totalTasks = ROW_SIZE * COLUMN_SIZE;
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

    public class fillMineGridRunnable implements Runnable {
        private final int termStartIndex;
        private final int numTerms;


        fillMineGridRunnable (int termStartIndex, int numTerms) {
            this.termStartIndex = termStartIndex;
            this.numTerms = numTerms;
        }

        @Override
        public void run() {

            int startRow = termStartIndex / COLUMN_SIZE;
            int endRow = (termStartIndex + numTerms) / COLUMN_SIZE;
            int startColumn = termStartIndex % COLUMN_SIZE;
            int termsLeft = numTerms;

            int column = startColumn;
            //int endColumn = (termStart + numTerms) / COLUMN_SIZE;

            // 17 and 13 are offset values from the top corner of the mine grid to a certain pixel on each cell, to
            // best determine what the cell contains

//            robot.mouseMove(mineGridTopCornerX + 13, mineGridTopCornerY + 17);
//            Color test = new Color(screenShot.getRGB(mineGridTopCornerX + 13, mineGridTopCornerY + 17));


            for (int row = startRow; row <= endRow; row++) {

                int y = mineGridTopCornerY + 17 + (CELL_SIDE_LENGTH * row);


                while (column != 30 && termsLeft != 0) {

                    column = column % COLUMN_SIZE;



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

                    Color colourFarAbove = new Color(screenShot.getRGB(x + 1, y - 10));

                    int[] colourFarAboveRGBValues = new int[3];

                    colourFarAboveRGBValues[0] = colourFarAbove.getRed();
                    colourFarAboveRGBValues[1] = colourFarAbove.getGreen();
                    colourFarAboveRGBValues[2] = colourFarAbove.getBlue();

                    int gameOverPopup = 0;

//                    robot.mouseMove(x, y);

                    if (isColourMatch(colourRGBValues, GAMEOVER_COLOUR, 0)) {
                        robot.mouseMove(x, y);
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
                        if (isColourMatch(colourFarAboveRGBValues, CELL_SEVEN_COLOUR, 30) && colourRGBValues[0] >= 140 && colourRGBValues[1] >= 140 && colourRGBValues[2] >= 180
                                && colourRightRGBValues[0] >= 140 && colourRightRGBValues[1] >= 140 && colourRightRGBValues[2] >= 180) {
                            seven.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                            mineGrid[row][column] = CELL_SEVEN;
                        } else if (isColourMatch(colourRightRGBValues, CELL_FOUR_COLOUR, 15)) {
                            four.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                            mineGrid[row][column] = CELL_FOUR;
                        } else if (isColourMatch(colourRGBValues, CELL_THREE_COLOUR, 35) || isColourMatch(
                                colourAboveRGBValues, CELL_THREE_COLOUR, 35)) {
                            three.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                            mineGrid[row][column] = CELL_THREE;
                        } else if (Math.abs(colourRGBValues[0] - 62) < 4 && Math.abs(colourRGBValues[1] - 80) < 2 && Math.abs(colourRGBValues[2] - 189) < 2 && colourFarBelowRGBValues[0] >= 150
                                && colourFarBelowRGBValues[1] >= 150) {
                            one.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                            mineGrid[row][column] = CELL_ONE;
                        } else if (colourRGBValues[0] <= 230 && colourRGBValues[0] >= 160 && colourRGBValues[1] <= 245
                                && colourRGBValues[1] >= 170 && colourRGBValues[2] <= 255 && colourRGBValues[2] >= 200) {
                            blank.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                            mineGrid[row][column] = CELL_BLANK;
                        } else if (isColourMatch(colourRGBValues, CELL_TWO_COLOUR, 30)) {
                            two.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                            mineGrid[row][column] = CELL_TWO;
                        } else if (isColourMatch(colourRGBValues, CELL_FIVE_COLOUR, 30) || isColourMatch(
                                colourAboveRGBValues, CELL_FIVE_COLOUR, 30)) {
                            five.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                            mineGrid[row][column] = CELL_FIVE;
                        } else if (isColourMatch(colourRGBValues, CELL_SIX_COLOUR, 30) || isColourMatch(
                                colourAboveRGBValues, CELL_SIX_COLOUR, 30)) {
                            six.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                            mineGrid[row][column] = CELL_SIX;
                        } else if (isColourMatch(colourRGBValues, CELL_FLAG_COLOUR, 35)) {
                            flag.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                            numFlags++;
                            mineGrid[row][column] = CELL_FLAG;
                        }
                    }

                    // move on to next cell, by moving the x coordinate exactly one cell length further

                    column++;
                    termsLeft--;

                }

                if (termsLeft == 0 || isGameOver) {
                    break;
                }

                column = column % COLUMN_SIZE;
            }


        }

    }



    private static boolean isColourMatch(int[] givenColourRGB, int[] expectedColourRGB, double tolerance) {


        double colourDifferenceResultant;
        double redDifferenceSquared;
        double greenDifferenceSquared;
        double blueDifferenceSquared;


        redDifferenceSquared = Math.pow(givenColourRGB[0] - expectedColourRGB[0], 2);
        greenDifferenceSquared = Math.pow(givenColourRGB[1] - expectedColourRGB[1], 2);
        blueDifferenceSquared = Math.pow(givenColourRGB[2] - expectedColourRGB[2], 2);

        colourDifferenceResultant = Math.sqrt(redDifferenceSquared + greenDifferenceSquared + blueDifferenceSquared);

        if (colourDifferenceResultant <= tolerance) {
            return true;
        } else {
            return false;
        }

    }



    public void findMineGridCorners () throws GetWindowRect.WindowNotFoundException, GetWindowRect.GetWindowRectException {
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

        int numConsecutiveBlackPixels = 0;

        for (int y = minesweeperTopCornerY; y < minesweeperBottomCornerY; y++) {
            for (int x = minesweeperTopCornerX; x < minesweeperBottomCornerX; x++) {
                Color pixel = new Color(screenShot.getRGB(x, y));

                numConsecutiveBlackPixels = 0;

                if (pixel.getRed() <= 10 && pixel.getGreen() <= 10 && pixel.getBlue() <= 10) {
//                    robot.mouseMove(x, y);
                    numConsecutiveBlackPixels++;
                    for (int i = 1; i < 8; i++) {
                        Color pixelBeside = new Color(screenShot.getRGB(x + i, y));
                        if (pixelBeside.getRed() <= 10 && pixelBeside.getGreen() <= 10 && pixelBeside.getBlue() <= 10
                                ) {
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

//        robot.mouseMove(mineGridTopCornerX, mineGridTopCornerY);

//        robot.mouseMove(mineGridTopCornerX + 31, mineGridTopCornerY + 17);

//        BufferedImage ss = screenShot.getSubimage(mineGridTopCornerX + 31, mineGridTopCornerY + 17, 10, 10);
//        File outputfile = new File("saved3.png");
//        try {
//            ImageIO.write(ss, "png", outputfile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//

        if (numConsecutiveBlackPixels != 8) {
            mineGridTopCornerX = rect[0] + 36;
            mineGridTopCornerY = rect[1] +  78;
        }

//        BufferedImage ss = screenShot.getSubimage(mineGridTopCornerX, mineGridTopCornerY, 10, 10);
//        File outputfile = new File("saved.png");
//        try {
//            ImageIO.write(ss, "png", outputfile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



        mineGridBottomCornerX = rect[2] - 37;
        mineGridBottomCornerY = rect[3] - 40;

    }

    public boolean fillMineGrid()
            throws GetWindowRect.WindowNotFoundException, GetWindowRect.GetWindowRectException, InterruptedException {
        screenShot = null;

        takeScreenshot();

        for (int y = 0; y < ROW_SIZE; y++) {
            Arrays.fill(mineGrid[y], (byte) 0);
        }

//        int[] rect;
//
//
//        try {
//            rect = GetWindowRect.getRect("Minesweeper");
//        } catch (GetWindowRect.WindowNotFoundException e) {
//            e.printStackTrace();
//            throw e;
//        } catch (GetWindowRect.GetWindowRectException e) {
//            e.printStackTrace();
//            throw e;
//        }
//
//        int minesweeperTopCornerX = rect[0];
//        int minesweeperTopCornerY = rect[1];
//        int minesweeperBottomCornerX = rect[2];
//        int minesweeperBottomCornerY = rect[3];
//
//        int numConsecutiveBlackPixels = 0;
//
//        for (int y = minesweeperTopCornerY; y < minesweeperBottomCornerY; y++) {
//            for (int x = minesweeperTopCornerX; x < minesweeperBottomCornerX; x++) {
//                Color pixel = new Color(screenShot.getRGB(x, y));
//
//                numConsecutiveBlackPixels = 0;
//
//                if (pixel.getRed() <= 10 && pixel.getGreen() <= 10 && pixel.getBlue() <= 10) {
////                    robot.mouseMove(x, y);
//                    numConsecutiveBlackPixels++;
//                    for (int i = 1; i < 8; i++) {
//                        Color pixelBeside = new Color(screenShot.getRGB(x + i, y));
//                        if (pixelBeside.getRed() <= 10 && pixelBeside.getGreen() <= 10 && pixelBeside.getBlue() <= 10
//                                ) {
//                            numConsecutiveBlackPixels++;
//                        }
//                    }
//                }
//
//                if (numConsecutiveBlackPixels == 8) {
//                    mineGridTopCornerX = x + 21;
//                    mineGridTopCornerY = y + 33;
//                    break;
//                }
//
//            }
//
//            if (numConsecutiveBlackPixels == 8) {
//                break;
//            }
//
//        }

//        robot.mouseMove(mineGridTopCornerX, mineGridTopCornerY);

//        robot.mouseMove(mineGridTopCornerX + 31, mineGridTopCornerY + 17);

//        BufferedImage ss = screenShot.getSubimage(mineGridTopCornerX + 31, mineGridTopCornerY + 17, 10, 10);
//        File outputfile = new File("saved3.png");
//        try {
//            ImageIO.write(ss, "png", outputfile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//

//        if (numConsecutiveBlackPixels != 8) {
//            mineGridTopCornerX = rect[0] + 36;
//            mineGridTopCornerY = rect[1] +  78;
//        }

//        BufferedImage ss = screenShot.getSubimage(mineGridTopCornerX, mineGridTopCornerY, 10, 10);
//        File outputfile = new File("saved.png");
//        try {
//            ImageIO.write(ss, "png", outputfile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



//        mineGridBottomCornerX = rect[2] - 37;
//        mineGridBottomCornerY = rect[3] - 40;

//        robot.mouseMove(mineGridTopCornerX, mineGridTopCornerY);


//        one.clear();
//        two.clear();
//        three.clear();
//        four.clear();
//        five.clear();
//        six.clear();
//        blank.clear();
//        flag.clear();

        numFlags = 0;

//        int gameOverPopup = 0;
//        isGameOver = false;
//        int testNum = 0;
        // 17 and 13 are offset values from the top corner of the mine grid to a certain pixel on each cell, to
        // best determine what the cell contains

//        int y = mineGridTopCornerY + 17;
//
//
//        for (int row = 0; row < ROW_SIZE; row++) {
//
//            int x = mineGridTopCornerX + 13;
//
//            for (int column = 0; column < COLUMN_SIZE; column++) {
//
//                // the culprit for the extreme slowness was the robot.getPixelColor method, which was called
//                // every time it thought it found a one
//
//
//                Color colour = new Color(screenShot.getRGB(x, y));
//
//                int[] colourRGBValues = new int[3];
//
//                colourRGBValues[0] = colour.getRed();
//                colourRGBValues[1] = colour.getGreen();
//                colourRGBValues[2] = colour.getBlue();
//
//                Color colourAbove = new Color(screenShot.getRGB(x, y - 1));
//
//
//                int[] colourAboveRGBValues = new int[3];
//
//                colourAboveRGBValues[0] = colourAbove.getRed();
//                colourAboveRGBValues[1] = colourAbove.getGreen();
//                colourAboveRGBValues[2] = colourAbove.getBlue();
//
//
//                Color colourRight = new Color(screenShot.getRGB(x + 1, y));
//
//                int[] colourRightRGBValues = new int[3];
//
//                colourRightRGBValues[0] = colourRight.getRed();
//                colourRightRGBValues[1] = colourRight.getGreen();
//                colourRightRGBValues[2] = colourRight.getBlue();
//
//                Color colourFarBelow = new Color(screenShot.getRGB(x, y + 2));
//
//                int[] colourFarBelowRGBValues = new int[3];
//
//                colourFarBelowRGBValues[0] = colourFarBelow.getRed();
//                colourFarBelowRGBValues[1] = colourFarBelow.getGreen();
//                colourFarBelowRGBValues[2] = colourFarBelow.getBlue();
////                robot.mouseMove(x, y);
//
//
////                    if (testNum == COLUMN_SIZE * 2
////                            + 2) {
////                        robot.mouseMove(x, y);
////                        System.out.println(colourRGBValues);
////
////                        colour = new Color(screenShot.getRGB(x, y - 1));
////
////                        colourRGBValues = new int[3];
////
////                        colourRGBValues[0] = colour.getRed();
////                        colourRGBValues[1] = colour.getGreen();
////                        colourRGBValues[2] = colour.getBlue();
////                        System.out.println(colourRGBValues);
////
////                    }
//
//
//                if (isColourMatch(colourRGBValues, GAMEOVER_COLOUR, 0)) {
//                    gameOverPopup += 1;
//                    for (int i = 1; i < 25; i++) {
//                        Color gameOverColour = new Color(screenShot.getRGB(x + i, y));
//                        int[] gameOverColourRGBValues = new int[3];
//                        gameOverColourRGBValues[0] = gameOverColour.getRed();
//                        gameOverColourRGBValues[1] = gameOverColour.getGreen();
//                        gameOverColourRGBValues[2] = gameOverColour.getBlue();
//                        if (isColourMatch(gameOverColourRGBValues, GAMEOVER_COLOUR, 0)) {
//                            gameOverPopup += 1;
//                        }
//                    }
//                    if (gameOverPopup == 25) {
//                        isGameOver = true;
//                        break;
//                    } else {
//                        gameOverPopup = 0;
//                    }
//
//                }
//
//                if (!isGameOver) {
//
//                    if (isColourMatch(colourRightRGBValues, CELL_FOUR_COLOUR, 15)) {
//                        four.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
//                        mineGrid[row][column] = CELL_FOUR;
//                    } else if (isColourMatch(colourRGBValues, CELL_THREE_COLOUR, 35) || isColourMatch(colourAboveRGBValues, CELL_THREE_COLOUR, 35)) {
//                        three.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
//                        mineGrid[row][column] = CELL_THREE;
//
//                    } else if (Math.abs(colourRGBValues[0] - 62) < 4 && Math.abs(colourRGBValues[1] - 80) < 2 && Math.abs(colourRGBValues[2] - 189) < 2 && colourFarBelowRGBValues[0] >= 150 && colourFarBelowRGBValues[1] >= 150) {
//                        one.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
//                        mineGrid[row][column] = CELL_ONE;
//                    } else if (colourRGBValues[0] <= 230 && colourRGBValues[0] >= 160 && colourRGBValues[1] <= 245 && colourRGBValues[1] >= 170 && colourRGBValues[2] <= 255 && colourRGBValues[2] >= 200) {
//                        blank.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
//                        mineGrid[row][column] = CELL_BLANK;
//                    } else if (isColourMatch(colourRGBValues, CELL_TWO_COLOUR, 30)) {
//                        two.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
//                        mineGrid[row][column] = CELL_TWO;
//                    } else if (isColourMatch(colourRGBValues, CELL_FIVE_COLOUR, 30) || isColourMatch(colourAboveRGBValues, CELL_FIVE_COLOUR, 30)) {
//                        five.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
//                        mineGrid[row][column] = CELL_FIVE;
//                    } else if (isColourMatch(colourRGBValues, CELL_SIX_COLOUR, 30) || isColourMatch(colourAboveRGBValues, CELL_SIX_COLOUR, 30)) {
//                        six.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
//                        mineGrid[row][column] = CELL_SIX;
//                    } else if (isColourMatch(colourRGBValues, CELL_FLAG_COLOUR, 35)) {
//                        flag.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
//                        numFlags++;
//                        mineGrid[row][column] = CELL_FLAG;
//                    }
//
//                    // move on to next cell, by moving the x coordinate exactly one cell length further
//
//                    x += CELL_SIDE_LENGTH;
//
//                    testNum++;
//                }
//
//
//
//            }
//
//            if (isGameOver) {
//                break;
//            }
//
//            y += CELL_SIDE_LENGTH;
//
//        }

        isGameOver = false;

        ExecutorService executor = Executors.newFixedThreadPool(tasksPerThread.size());

        int firstTermIndex = 0;
        for (int i = 0; i < tasksPerThread.size(); i++) {
            Runnable worker = new fillMineGridRunnable(firstTermIndex, tasksPerThread.get(i));
            firstTermIndex += tasksPerThread.get(i);

            executor.execute(worker);
        }
        // This will make the executor accept no new threads
        // and finish all existing threads in the queue
        executor.shutdown();
        // Wait until all threads are finish
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }
//        System.out.println("Finished all threads!");


        return isGameOver;

    }


    public int[] getTilePos(int row, int column) {
        int[] tilePos = new int[2];
        tilePos[0] = mineGridTopCornerX + 6 + (CELL_SIDE_LENGTH * column);
        tilePos[1] = mineGridTopCornerY + 6 + (CELL_SIDE_LENGTH * row);

        return tilePos;
    }


    private void takeScreenshot() {
        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        screenShot = robot.createScreenCapture(screenSize);
        screenWidth = screenSize.width;
        screenHeight = screenSize.height;

        return;
    }

    public static void main(String[] args) {
        int[] colour = {170, 12, 12};
        int[] givenColour = {167, 8, 8};

        System.out.println(isColourMatch(colour, givenColour, 35));
    }


}

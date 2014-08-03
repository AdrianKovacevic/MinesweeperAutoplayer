package com.company;



import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

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
    //final int[] CELL_ONE_COLOUR_BESIDE = {62, 79, 190};
    final int[] CELL_TWO_COLOUR = {30, 103, 2};
    final int[] CELL_THREE_COLOUR = {170, 8, 8};
    final int[] CELL_FOUR_COLOUR = {1, 1, 130};
    final int[] CELL_FIVE_COLOUR = {129, 1, 1};
    final int[] CELL_SIX_COLOUR = {5, 125, 122};
    final int[] CELL_SEVEN_COLOUR = {};
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


    public Screen () {
        screenWidth = 0;
        screenHeight = 0;
        mineGridTopCornerX = 0;
        mineGridTopCornerY = 0;
        mineGridBottomCornerX = 0;
        mineGridBottomCornerY = 0;
        screenShot = null;
        one = new ArrayList<String>();
        two = new ArrayList<String>();
        three = new ArrayList<String>();
        four = new ArrayList<String>();
        five = new ArrayList<String>();
        six = new ArrayList<String>();
        seven = new ArrayList<String>();
        blank = new ArrayList<String>();
        flag = new ArrayList<String>();

        mineGrid = new byte[ROW_SIZE][COLUMN_SIZE];
        try {
            robot = new Robot();
        } catch (AWTException e) {
            System.out.println("Couldn't make a robot!");
            e.printStackTrace();
        }
    };

    public int getScreenHeight() {
        return screenHeight;
    };

    public int getScreenWidth() {
        return screenWidth;
    };

    public BufferedImage getScreenShot() {
        return screenShot;
    };

    public byte[][] getMineGrid() {
        return mineGrid;
    };

    public int getMineGridBottomCornerX() {
        return mineGridBottomCornerX;
    };

    public int getMineGridBottomCornerY() {
        return mineGridBottomCornerY;
    };

    public int getMineGridTopCornerY() {
        return mineGridTopCornerY;
    };

    public int getMineGridTopCornerX() {
        return mineGridTopCornerX;
    };

    private static boolean isColourMatch (int[] givenColourRGB, int[] expectedColourRGB, double tolerance) {


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


    public boolean fillMineGrid() {
        screenShot = null;
        takeScreenshot();


        for (int y = 0; y < ROW_SIZE; y++) {
            Arrays.fill(mineGrid[y], (byte) 0);
        }





        int[] rect = new int[4];
        try {
            rect = GetWindowRect.getRect("Minesweeper");
        } catch (GetWindowRect.WindowNotFoundException e) {
            e.printStackTrace();
        } catch (GetWindowRect.GetWindowRectException e) {
            e.printStackTrace();
        }


            mineGridTopCornerX = rect[0] + 36;
            mineGridTopCornerY = rect[1] +  78;
            mineGridBottomCornerX = rect[2] - 37;
            mineGridBottomCornerY = rect[3] - 40;



            int row = 0;
            int column = 0;
            one.clear();
            two.clear();
            three.clear();
            four.clear();
            five.clear();
            six.clear();
            blank.clear();
            flag.clear();
            int gameOverPopup = 0;
            int testNum = 0;


        for (int y = mineGridTopCornerY + 17; y < mineGridBottomCornerY; y += CELL_SIDE_LENGTH) {

                for (int x = mineGridTopCornerX + 13; x < mineGridBottomCornerX; x += CELL_SIDE_LENGTH) {

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
//                    robot.mouseMove(x, y);




//                    if (testNum == COLUMN_SIZE * 2
//                            + 2) {
//                        robot.mouseMove(x, y);
//                        System.out.println(colourRGBValues);
//
//                        colour = new Color(screenShot.getRGB(x, y - 1));
//
//                        colourRGBValues = new int[3];
//
//                        colourRGBValues[0] = colour.getRed();
//                        colourRGBValues[1] = colour.getGreen();
//                        colourRGBValues[2] = colour.getBlue();
//                        System.out.println(colourRGBValues);
//
//                    }


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
                            return true;
                        } else {
                            gameOverPopup = 0;
                        }
                    }


                    boolean givenValue = false;


                    // new idea: calculate Euclidean distance between all known pixel colours and the given one
                    // lowest distance means the given pixel is the known pixel, fill array cell accordingly
                    // probably faster, possibly more accurate

                    if (isColourMatch(colourRightRGBValues, CELL_FOUR_COLOUR, 15) ) {
                        four.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                        mineGrid[row][column % COLUMN_SIZE] = CELL_FOUR;
                    } else if (isColourMatch(colourRGBValues, CELL_THREE_COLOUR, 30) || isColourMatch(colourAboveRGBValues, CELL_THREE_COLOUR, 30)) {
                        //                    System.out.println("Three?");
                        three.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                        mineGrid[row][column % COLUMN_SIZE] = CELL_THREE;

                    } else if (Math.abs(colourRGBValues[0] - 62) < 4 && Math.abs(colourRGBValues[1] - 80) < 2 && Math.abs(colourRGBValues[2] - 189) < 2) {
                            one.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                            mineGrid[row][column % COLUMN_SIZE] = CELL_ONE;
                    } else if (colourRGBValues[0] <= 230 && colourRGBValues[0] >= 160 && colourRGBValues[1] <= 245 && colourRGBValues[1] >= 170 &&  colourRGBValues[2] <= 255 && colourRGBValues[2] >= 200) {
                        blank.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                        mineGrid[row][column % COLUMN_SIZE] = CELL_BLANK;
                    }  else if (isColourMatch(colourRGBValues, CELL_TWO_COLOUR, 30)) {
                        two.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                        mineGrid[row][column % COLUMN_SIZE] = CELL_TWO;
                    }  else if (isColourMatch(colourRGBValues, CELL_FIVE_COLOUR, 20) || isColourMatch(colourAboveRGBValues, CELL_FIVE_COLOUR, 20)) {
                        five.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                        mineGrid[row][column % COLUMN_SIZE] = CELL_FIVE;
                    }  else if (isColourMatch(colourRGBValues, CELL_SIX_COLOUR, 20) || isColourMatch(colourAboveRGBValues, CELL_SIX_COLOUR, 20)) {
                        six.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                        mineGrid[row][column % COLUMN_SIZE] = CELL_SIX;
                    } else if (isColourMatch(colourRGBValues, CELL_FLAG_COLOUR, 35)) {
                        flag.add(colourRGBValues[0] + " " + colourRGBValues[1] + " " + colourRGBValues[2]);
                        mineGrid[row][column % COLUMN_SIZE] = CELL_FLAG;
                    }


                    column++;
                    testNum++;
                }
                row++;

            }



        return false;

    };

    public int[] getTilePos (int row, int column) {
        int[] tilePos = new int[2];
        tilePos[0] = mineGridTopCornerX + 6 + (CELL_SIDE_LENGTH * column);
        tilePos[1] = mineGridTopCornerY + 6 + (CELL_SIDE_LENGTH * row);

        return tilePos;
    };


    private void takeScreenshot () {
        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        screenShot = robot.createScreenCapture(screenSize);
        screenWidth = screenSize.width;
        screenHeight = screenSize.height;

        return;
    };

    public static void main (String[] args) {
        int[] colour = {0, 129, 2};
        int[] givenColour = {70, 199, 72};

        System.out.println(isColourMatch(colour, givenColour, 50));
    }







}

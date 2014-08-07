package com.company;


import java.awt.AWTException;
import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Main {


    public static void doNextMove (Screen screen) throws InterruptedException, GetWindowRect.GetWindowRectException, GetWindowRect.WindowNotFoundException {
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
        final byte FINAL_ROW_INDEX = 15;
        final byte FINAL_COLUMN_INDEX = 29;

        int totalMines = screen.getTotalMines();
        byte rowSize = screen.getRowSize();
        byte columnSize = screen.getColumnSize();

        double[][] guessingGrid = new double[rowSize][columnSize];

        for (int i = 0; i < rowSize; i++) {
            Arrays.fill(guessingGrid[i], -1.0);
        }


        boolean isEmpty = true;
        boolean gameOver;
        long startTime = System.nanoTime();

        gameOver = screen.fillMineGrid();

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1000000000.0;
        System.out.println("The mineGrid filling duration is: " + duration);

        startTime = System.nanoTime();


        if (gameOver) {
            System.out.println("Game over!");
            screen.robot.mouseMove(20, 20);
            screen.robot.keyPress(KeyEvent.VK_ALT);
            Thread.sleep(50);
            screen.robot.keyPress(KeyEvent.VK_P);
            Thread.sleep(50);
            screen.robot.keyRelease(KeyEvent.VK_P);
            Thread.sleep(50);
            screen.robot.keyRelease(KeyEvent.VK_ALT);
            Thread.sleep(1000);
            return;
        }

        byte[][] mineGrid = screen.getMineGrid();

        // fills all the unknown cells in the guessing grid as a 0.
        // in this way, all cells that do not have any unknown cells around it will never be guessed on, and when it
        // comes to guessing around a cell, if there are no cells that can be guessed around, it will click on the
        // unknown cells to handle the case where a cell is boxed off by mines

        for (int row = 0; row < rowSize; row++) {
            for (int column = 0; column < columnSize; column++) {
                if (mineGrid[row][column] == CELL_UNKNOWN) {
                    guessingGrid[row][column] = CELL_UNKNOWN;
                }
            }
        }


        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < columnSize; x++) {
                if (mineGrid[y][x] != 0) {
                    isEmpty = false;
                }
            }
        }

        if (isEmpty) {
            Random rand = new Random();
            // clicks a random cell, but does not touch the edges as they are usually bad to start off with
            int randomX = rand.nextInt(((screen.getMineGridBottomCornerX() - 20) - (screen.getMineGridTopCornerX()) + 20)) + (screen.getMineGridTopCornerX() + 20);
            int randomY = rand.nextInt(((screen.getMineGridBottomCornerY() - 20) - (screen.getMineGridTopCornerY()) + 20)) + (screen.getMineGridTopCornerY() + 20);

            screen.robot.mouseMove(randomX, randomY);
            screen.robot.mousePress(InputEvent.BUTTON1_MASK);
            Thread.sleep(10);
            screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
            return;
        } else {


            // this first pair of loops checks all cells for guaranteed moves, and keeps track of any possible guesses
            // the next loop checks for the cell with the least number of guesses, and performs the guesses on it
            for (int y = 0; y < rowSize; y++) {
                for (int x = 0; x < columnSize; x++) {
                    // iterates through the grid to click all unknown cells if all mines have been flagged
                    // otherwise, it goes to a numbered cell and counts the adjacent flags and unknown cells


                    if (screen.getNumFlaggedMines() == totalMines && mineGrid[y][x] == CELL_UNKNOWN) {
                        int[] tilePos = screen.getTilePos(y, x);
                        screen.robot.mouseMove(tilePos[0], tilePos[1]);
                        screen.robot.mousePress(InputEvent.BUTTON1_MASK);
                        Thread.sleep(10);
                        screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
                        endTime = System.nanoTime();
                        duration = (endTime - startTime) / 1000000000.0;
                        System.out.println("The duration is: " + duration);
                        return;
                    } else if (mineGrid[y][x] != CELL_UNKNOWN && mineGrid[y][x] != CELL_BLANK && mineGrid[y][x] != CELL_FLAG) {
                        int numUnknown = 0;
                        int numFlags = 0;

                        // goes to all adjacent cells in the array to see how many flags and unknown cells there are

                        for (int i = -1; i < 2; i++) {
                            for (int j = -1; j < 2; j++) {
                                if (i != 0 || j != 0) {
                                    int testCol = j + x;
                                    int testRow = i + y;

                                    try {
                                        if (mineGrid[testRow][testCol] == CELL_UNKNOWN) {
                                            numUnknown++;
                                        } else if (mineGrid[testRow][testCol] == CELL_FLAG) {
                                            numFlags++;
                                        }
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        // catching and doing nothing is fine as the out of play cells will not be flags
                                        // or unknown
                                    }
                                }
                            }
                        }

                        // if the number of adjacent flags is equal to the number, it expands the cell if there are
                        // unknown squares adjacent to it

                        if (mineGrid[y][x] == numFlags && numUnknown != 0) {
                            // expand the tile since possible
                            System.out.println("Expanding around a " + mineGrid[y][x] + " at row " + y + " and column " + x + ". Numflags: " + numFlags + ". " + "numUnknowns: " + numUnknown);

                            int[] tilePos = screen.getTilePos(y, x);
                            screen.robot.mouseMove(tilePos[0], tilePos[1]);
                            screen.robot.mousePress(InputEvent.BUTTON2_MASK);
                            Thread.sleep(10);
                            screen.robot.mouseRelease(InputEvent.BUTTON2_MASK);
                            endTime = System.nanoTime();
                            duration = (endTime - startTime) / 1000000000.0;
                            System.out.println("The duration is: " + duration);
                            return;

                        // otherwise, if not all of the number's adjacent cells have been flagged, but there are
                        // the same number of unknown cells as leftover mines, then it flags all of the unknown cells
                        } else if (mineGrid[y][x] - numFlags == numUnknown && numUnknown != 0) {
                            System.out.println("Flagging around a " + mineGrid[y][x] + " at row " + y + " and column " + x + ". Numflags: " + numFlags + ". " + "numUnknowns: " + numUnknown);

                            // mark all surrounding tiles with flags
                            for (int i = -1; i < 2; i++) {
                                for (int j = -1; j < 2; j++) {
                                    if (i != 0 || j != 0) {
                                        int testCol = j + x;
                                        int testRow = i + y;

                                        try {
                                            if (mineGrid[testRow][testCol] == CELL_UNKNOWN) {
                                                int[] tilePos = screen.getTilePos(testRow, testCol);
                                                screen.robot.mouseMove(tilePos[0], tilePos[1]);
                                                screen.robot.mousePress(InputEvent.BUTTON3_MASK);
                                                Thread.sleep(10);
                                                screen.robot.mouseRelease(InputEvent.BUTTON3_MASK);
                                            }
                                        } catch (ArrayIndexOutOfBoundsException e) {
                                          //  System.out.println("Array out of bounds");

                                        }
                                    }
                                }
                            }

                            screen.robot.mouseMove(screen.getMineGridTopCornerX() - 10, screen.getMineGridTopCornerY() - 10);
                            endTime = System.nanoTime();
                            duration = (endTime - startTime) / 1000000000.0;
                            System.out.println("The duration is: " + duration);
                            return;

                        // if it can not expand a cell or flag it using the information that is given, then it
                        // must take a guess. here it will compute how many guesses are necessary around the cell

                        } else if (numUnknown != 0) {
                            int numMines = mineGrid[y][x] - numFlags;
                            double chanceOfCorrectGuess = 1.0 - ((double) numMines / (double) numUnknown);
                            guessingGrid[y][x] = chanceOfCorrectGuess;
                        }
                    }
                }
            }

            // put guessing algorithm in a larger for loop, from 1 to 7, depending on how many guesses it needs to make
            // if no guesses possible around a number, then finally just guess on other unknown cells since some mines
            // are left (previous check for 99 flags)

            // iterate through the guessing chances for each number that is not able to be expanded, look for the
            // highest probability equal or less than 50%, and guess once on it. return, and check to see if anything
            // new opened up that does not have to be guessed
            // 50% or less prevents guessing on something like a 1 with 8 empty cells around it


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

            System.out.println("Making a guess around a " + mineGrid[guessRow][guessColumn] + " at row " + guessRow + " and column " +
                    guessColumn + ". The chance of guessing correctly is: " + guessingGrid[guessRow][guessColumn] * 100.0 + "%.");

            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    if (i != 0 || j != 0) {
                        int testRow = i + guessRow;
                        int testCol = j + guessColumn;

                        try {
                            if (mineGrid[testRow][testCol] == CELL_UNKNOWN) {
                                int[] tilePos = screen.getTilePos(testRow, testCol);
                                screen.robot.mouseMove(tilePos[0], tilePos[1]);
                                screen.robot.mousePress(InputEvent.BUTTON1_MASK);
                                Thread.sleep(10);
                                screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
                                Thread.sleep(10);
                                screen.robot.mousePress(InputEvent.BUTTON1_MASK);
                                Thread.sleep(10);
                                screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
                                endTime = System.nanoTime();
                                duration = (endTime - startTime) / 1000000000.0;
                                System.out.println("The duration is: " + duration);
//                                Thread.sleep(150);
                                return;
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
//                          System.out.println("Array out of bounds");

                        }

                    }
                }
            }

            endTime = System.nanoTime();
            duration = (endTime - startTime) / 1000000000.0;
            System.out.println("The duration is: " + duration);

            return;
            //implement best guess


        }



    }

    public static void configureScreen(Screen screen) throws InterruptedException, GetWindowRect.WindowNotFoundException, GetWindowRect.GetWindowRectException {

        try {
            Runtime.getRuntime().exec("cmd.exe /C Start \"\" \"%programfiles%\\Microsoft Games\\Minesweeper\\minesweeper.exe\"");
        } catch (IOException e) {
            System.out.println("Couldn't run minesweeper!");
            e.printStackTrace();
        }

        System.out.println("Make sure that you are using blue tiles, " +
                "playing on advanced, and that the window size is as small as possible.");
        // add setup that changes to smallest windows size , changes to advanced, and changes to blue squares
        // add support for other difficulties
        // add better window tracking capabilities across Windows 8 and non-aero versions of the game
        // add multithreading



        Thread.sleep(1500);

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

        screen.robot.mouseMove(rect[0] + 30, rect[1] + 15);

        screen.robot.mousePress(InputEvent.BUTTON1_MASK);
        screen.robot.mouseMove(rect[0] + 150, rect[1] + 15);
        screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);




        screen.robot.keyPress(KeyEvent.VK_ALT);
        screen.robot.keyPress(KeyEvent.VK_ENTER);
        screen.robot.keyRelease(KeyEvent.VK_ENTER);
        screen.robot.keyRelease(KeyEvent.VK_ALT);

        screen.robot.keyPress(KeyEvent.VK_F5);
        screen.robot.keyRelease(KeyEvent.VK_F5);
//        screen.robot.keyPress(KeyEvent.VK_ALT);
//        screen.robot.keyPress(KeyEvent.VK_V);
//        screen.robot.keyRelease(KeyEvent.VK_V);
//        screen.robot.keyRelease(KeyEvent.VK_ALT);

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

        Thread.sleep(200);

        screen.robot.keyPress(KeyEvent.VK_ENTER);
        screen.robot.keyRelease(KeyEvent.VK_ENTER);

        Thread.sleep(50);

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

        //
//        screen.robot.mousePress(InputEvent.BUTTON1_MASK);
//        Thread.sleep(50);
//        screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
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



    public static void main(String[] args) throws InterruptedException {

        Screen screen;
        try {
            screen = new Screen();
        } catch (AWTException e) {
            e.printStackTrace();
            return;
        }

        try {
            configureScreen(screen);
        } catch (GetWindowRect.WindowNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (GetWindowRect.GetWindowRectException e) {
            e.printStackTrace();
            return;
        }

//        for (int i = 0; i < 5; i++) {
//
//            try {
//                long startTime = System.nanoTime();
//
//                screen.fillMineGrid();
//
//                long endTime = System.nanoTime();
//                double duration = (endTime - startTime) / 1000000000.0;
//                System.out.println("The duration is: " + duration);
//
//            } catch (GetWindowRect.WindowNotFoundException e) {
//                e.printStackTrace();
//            } catch (GetWindowRect.GetWindowRectException e) {
//                e.printStackTrace();
//            }
//
//            screen.getMineGrid();
//
//        }




        try {
            while (true) {
//                long startTime = System.nanoTime();
                doNextMove(screen);
//                long endTime = System.nanoTime();
//                double duration = (endTime - startTime) / 1000000000.0;
//                System.out.println("The duration is: " + duration);
                Thread.sleep(50);
            }
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

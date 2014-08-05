package com.company;


import com.sun.jna.platform.WindowUtils;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Random;
import javax.swing.text.JTextComponent;

public class Main {


    public static void doNextMove (Screen screen) throws InterruptedException, GetWindowRect.GetWindowRectException, GetWindowRect.WindowNotFoundException {
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
        final byte FLAG_MAXSIZE = 99;
        final byte FINAL_ROW_INDEX = 15;
        final byte FINAL_COLUMN_INDEX = 29;

        final int PLAYAGAIN_BUTTON_X_DISTANCE = 396;
        final int PLAYAGAIN_BUTTON_Y_DISTANCE = 210;


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
            Thread.sleep(2000);
            return;
        }

        byte[][] mineGrid = screen.getMineGrid();

        for (int y = 0; y < ROW_SIZE; y++) {
            for (int x = 0; x < COLUMN_SIZE; x++) {
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
            Thread.sleep(50);
            screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
            return;
        } else {




            for (int y = 0; y < ROW_SIZE; y++) {
                for (int x = 0; x < COLUMN_SIZE; x++) {
                    if (screen.getNumFlaggedMines() == FLAG_MAXSIZE && mineGrid[y][x] == CELL_UNKNOWN) {
                        int[] tilePos = screen.getTilePos(y, x);
                        screen.robot.mouseMove(tilePos[0], tilePos[1]);
                        screen.robot.mousePress(InputEvent.BUTTON1_MASK);
                        Thread.sleep(50);
                        screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
                        endTime = System.nanoTime();
                        duration = (endTime - startTime) / 1000000000.0;
                        System.out.println("The duration is: " + duration);
                        return;
                    } else if (mineGrid[y][x] != CELL_UNKNOWN && mineGrid[y][x] != CELL_BLANK && mineGrid[y][x] != CELL_FLAG) {
                        int numZero = 0;
                        int numFlags = 0;
                        for (int i = -1; i < 2; i++) {
                            for (int j = -1; j < 2; j++) {
                                if (i != 0 || j != 0) {
                                    int testCol = j + x;
                                    int testRow = i + y;

                                    try {
                                        if (mineGrid[testRow][testCol] == CELL_UNKNOWN) {
                                            numZero++;
                                        } else if (mineGrid[testRow][testCol] == CELL_FLAG) {
                                            numFlags++;
                                        }
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        //System.out.println("Array out of bounds");
                                    }
                                }
                            }
                        }



                        if (mineGrid[y][x] == numFlags && numZero != 0) {
                            // expand the tile since possible
                            System.out.println("Expanding around a " + mineGrid[y][x] + " at row " + y + " and column " + x + ". Numflags: " + numFlags + ". " + "Numzeros: " + numZero);

                            int[] tilePos = screen.getTilePos(y, x);
                            screen.robot.mouseMove(tilePos[0], tilePos[1]);
                            screen.robot.mousePress(InputEvent.BUTTON2_MASK);
                            Thread.sleep(50);
                            screen.robot.mouseRelease(InputEvent.BUTTON2_MASK);
                            endTime = System.nanoTime();
                            duration = (endTime - startTime) / 1000000000.0;
                            System.out.println("The duration is: " + duration);
                            return;
                        } else if (mineGrid[y][x] - numFlags == numZero && numZero != 0) {
                            System.out.println("Flagging around a " + mineGrid[y][x] + " at row " + y + " and column " + x + ". Numflags: " + numFlags + ". " + "Numzeros: " + numZero);

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
                                                Thread.sleep(50);
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
                        }
                    }
                }
            }

            // put guessing algorithm in a larger for loop, from 1 to 7, depending on how many guesses it needs to make
            // if no guesses possible around a number, then finally just guess on other unknown cells since some mines
            // are left (previous check for 99 flags)

            for (int y = 0; y < ROW_SIZE; y++) {
                for (int x = 0; x < COLUMN_SIZE; x++) {
                    if (mineGrid[y][x] != CELL_UNKNOWN && mineGrid[y][x] != CELL_BLANK && mineGrid[y][x] != CELL_FLAG) {
                        int numZero = 0;
                        int numFlags = 0;
                        for (int i = -1; i < 2; i++) {
                            for (int j = -1; j < 2; j++) {
                                if (i != 0 || j != 0) {
                                    int testCol = j + x;
                                    int testRow = i + y;

                                    try {
                                        if (mineGrid[testRow][testCol] == CELL_UNKNOWN) {
                                            numZero++;
                                        } else if (mineGrid[testRow][testCol] == CELL_FLAG) {
                                            numFlags++;
                                        }
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                       // System.out.println("Array out of bounds");

                                    }
                                }
                            }
                        }



                        if (numZero == 2) {
                            System.out.println("Making a guess around a " + mineGrid[y][x] + " at row " + y + " and column " + x + ". Numflags: " + numFlags + ". " + "Numzeros: " + numZero);
                            for (int i = -1; i < 2; i++) {
                                for (int j = -1; j < 2; j++) {
                                    if (i != 0 || j != 0) {
                                        int testCol = j + x;
                                        int testRow = i + y;

                                        try {
                                            if (mineGrid[testRow][testCol] == CELL_UNKNOWN) {
                                                int[] tilePos = screen.getTilePos(testRow, testCol);
                                                screen.robot.mouseMove(tilePos[0], tilePos[1]);
                                                screen.robot.mousePress(InputEvent.BUTTON1_MASK);
                                                Thread.sleep(50);
                                                screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
                                                Thread.sleep(50);
                                                screen.robot.mousePress(InputEvent.BUTTON1_MASK);
                                                Thread.sleep(50);
                                                screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
                                                endTime = System.nanoTime();
                                                duration = (endTime - startTime) / 1000000000.0;
                                                System.out.println("The duration is: " + duration);
                                                return;
                                            }
                                        } catch (ArrayIndexOutOfBoundsException e) {
//                                            System.out.println("Array out of bounds");

                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
            for (int y = 0; y < ROW_SIZE; y++) {
                for (int x = 0; x < COLUMN_SIZE; x++) {
                    if (mineGrid[y][x] != CELL_UNKNOWN && mineGrid[y][x] != CELL_BLANK && mineGrid[y][x] != CELL_FLAG) {
                        int numZero = 0;
                        int numFlags = 0;
                        for (int i = -1; i < 2; i++) {
                            for (int j = -1; j < 2; j++) {
                                if (i != 0 || j != 0) {
                                    int testCol = j + x;
                                    int testRow = i + y;

                                    try {
                                        if (mineGrid[testRow][testCol] == CELL_UNKNOWN) {
                                            numZero++;
                                        } else if (mineGrid[testRow][testCol] == CELL_FLAG) {
                                            numFlags++;
                                        }
                                    } catch (ArrayIndexOutOfBoundsException e) {
//                                        System.out.println("Array out of bounds");

                                    }
                                }
                            }
                        }



                        if (numZero != 0) {
                            int numGuesses = Math.abs(numZero - (mineGrid[y][x] - numFlags));
                            System.out.println("Making " + numGuesses + " guesses around a " + mineGrid[y][x] + " at row " + y + " and column " + x + ". Numflags: " + numFlags + ". " + "Numzeros: " + numZero);

                            for (int i = -1; i < 2; i++) {
                                for (int j = -1; j < 2; j++) {
                                    if (i != 0 || j != 0) {
                                        int testCol = j + x;
                                        int testRow = i + y;

                                        try {
                                            if (mineGrid[testRow][testCol] == CELL_UNKNOWN) {
                                                if (numGuesses == 0) {
                                                    endTime = System.nanoTime();
                                                    duration = (endTime - startTime) / 1000000000.0;
                                                    System.out.println("The duration is: " + duration);
                                                    return;
                                                }
                                                int[] tilePos = screen.getTilePos(testRow, testCol);
                                                screen.robot.mouseMove(tilePos[0], tilePos[1]);
                                                screen.robot.mousePress(InputEvent.BUTTON1_MASK);
                                                Thread.sleep(50);
                                                screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
                                                Thread.sleep(50);
                                                screen.robot.mousePress(InputEvent.BUTTON1_MASK);
                                                Thread.sleep(50);
                                                screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
                                                numGuesses--;
                                            }
                                        } catch (ArrayIndexOutOfBoundsException e) {
                                           // System.out.println("Array out of bounds");

                                        }
                                    }
                                }
                            }
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

    public static void configureScreen(Screen screen) throws InterruptedException {

        try {
            Runtime.getRuntime().exec("cmd.exe /C Start \"\" \"%programfiles%\\Microsoft Games\\Minesweeper\\minesweeper.exe\"");
        } catch (IOException e) {
            System.out.println("Couldn't run minesweeper!");
            e.printStackTrace();
        }

        System.out.println("This program only works on Windows 7 so far. Also, make sure that you are using blue tiles, " +
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
        screen.robot.mouseMove(rect[0] + 60, rect[1] + 15);
        screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);




        screen.robot.keyPress(KeyEvent.VK_ALT);
        screen.robot.keyPress(KeyEvent.VK_ENTER);
        screen.robot.keyRelease(KeyEvent.VK_ENTER);
        screen.robot.keyRelease(KeyEvent.VK_ALT);

        screen.robot.keyPress(KeyEvent.VK_F5);
        screen.robot.keyRelease(KeyEvent.VK_F5);
        screen.robot.keyPress(KeyEvent.VK_ALT);
        screen.robot.keyPress(KeyEvent.VK_V);
        screen.robot.keyRelease(KeyEvent.VK_V);
        screen.robot.keyRelease(KeyEvent.VK_ALT);

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
        screen.robot.mousePress(InputEvent.BUTTON1_MASK);
        Thread.sleep(50);
        screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);

        Thread.sleep(100);

        screen.robot.mouseMove(20, 20);

    }



    public static void main(String[] args) throws InterruptedException {

        Screen screen = new Screen();

        configureScreen(screen);

        for (int i = 0; i < 5; i++) {

            try {
                long startTime = System.nanoTime();

                screen.fillMineGrid();

                long endTime = System.nanoTime();
                double duration = (endTime - startTime) /
                        1000000000.0;
                System.out.println("The duration is: " + duration);

            } catch (GetWindowRect.WindowNotFoundException e) {
                e.printStackTrace();
            } catch (GetWindowRect.GetWindowRectException e) {
                e.printStackTrace();
            }

            screen.getMineGrid();

        }




        try {
            while (true) {
//                long startTime = System.nanoTime();
                doNextMove(screen);
//                long endTime = System.nanoTime();
//                double duration = (endTime - startTime) / 1000000000.0;
//                System.out.println("The duration is: " + duration);
                Thread.sleep(25);
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

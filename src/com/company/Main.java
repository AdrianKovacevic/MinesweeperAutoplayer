package com.company;

import javafx.scene.control.*;
import javafx.scene.input.MouseButton;

import java.awt.Robot;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.ButtonModel;

public class Main {

    public static void doNextMove (Screen screen) throws InterruptedException {
        boolean isEmpty = true;
        boolean gameOver;
        gameOver = screen.fillMineGrid();

        if (gameOver) {
            screen.robot.mouseMove(screen.getMineGridTopCornerX() + 396, screen.getMineGridTopCornerY() + 210);
            screen.robot.mousePress(InputEvent.BUTTON1_MASK);
            Thread.sleep(50);
            screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
            screen.robot.mouseMove(screen.getMineGridTopCornerX(), screen.getMineGridTopCornerY());
            Thread.sleep(2000);
            return;
        }

        int[][] mineGrid = screen.getMineGrid();

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 30; x++) {
                if (mineGrid[y][x] != 0) {
                    isEmpty = false;
                }
            }
        }

        if (isEmpty) {
            Random rand = new Random();
            int randomX = rand.nextInt(((screen.getMineGridBottomCornerX() - 1) - (screen.getMineGridTopCornerX()) + 1)) + (screen.getMineGridTopCornerX() + 1);
            int randomY = rand.nextInt(((screen.getMineGridBottomCornerY() - 1) - (screen.getMineGridTopCornerY()) + 1)) + (screen.getMineGridTopCornerY() + 1);

            screen.robot.mouseMove(randomX, randomY);
            screen.robot.mousePress(InputEvent.BUTTON1_MASK);
            Thread.sleep(50);
            screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
            return;
        } else {


            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 30; x++) {
                    if (mineGrid[y][x] != 0 && mineGrid[y][x] != 11 && mineGrid[y][x] != 15) {
                        int numZero = 0;
                        int numFlags = 0;
                        for (int i = -1; i < 2; i++) {
                            for (int j = -1; j < 2; j++) {
                                if (i != 0 || j != 0) {
                                    int testCol = j + x;
                                    int testRow = i + y;

                                    try {
                                        if (mineGrid[testRow][testCol] == 0) {
                                            numZero++;
                                        } else if (mineGrid[testRow][testCol] == 15) {
                                            numFlags++;
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Array out of bounds");

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
                                            if (mineGrid[testRow][testCol] == 0) {
                                                int[] tilePos = screen.getTilePos(testRow, testCol);
                                                screen.robot.mouseMove(tilePos[0], tilePos[1]);
                                                screen.robot.mousePress(InputEvent.BUTTON3_MASK);
                                                Thread.sleep(50);
                                                screen.robot.mouseRelease(InputEvent.BUTTON3_MASK);
                                            }
                                        } catch (Exception e) {
                                            System.out.println("Array out of bounds");

                                        }
                                    }
                                }
                            }
                            screen.robot.mouseMove(screen.getMineGridTopCornerX() - 10, screen.getMineGridTopCornerY() - 10);
                            return;
                        }
                    }
                }
            }

            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 30; x++) {
                    if (mineGrid[y][x] != 0 && mineGrid[y][x] != 11 && mineGrid[y][x] != 15) {
                        int numZero = 0;
                        int numFlags = 0;
                        for (int i = -1; i < 2; i++) {
                            for (int j = -1; j < 2; j++) {
                                if (i != 0 || j != 0) {
                                    int testCol = j + x;
                                    int testRow = i + y;

                                    try {
                                        if (mineGrid[testRow][testCol] == 0) {
                                            numZero++;
                                        } else if (mineGrid[testRow][testCol] == 15) {
                                            numFlags++;
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Array out of bounds");

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
                                            if (mineGrid[testRow][testCol] == 0) {
                                                int[] tilePos = screen.getTilePos(testRow, testCol);
                                                screen.robot.mouseMove(tilePos[0], tilePos[1]);
                                                screen.robot.mousePress(InputEvent.BUTTON1_MASK);
                                                Thread.sleep(50);
                                                screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
                                                Thread.sleep(50);
                                                screen.robot.mousePress(InputEvent.BUTTON1_MASK);
                                                Thread.sleep(50);
                                                screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
                                                return;
                                            }
                                        } catch (Exception e) {
                                            System.out.println("Array out of bounds");

                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 30; x++) {
                    if (mineGrid[y][x] != 0 && mineGrid[y][x] != 11 && mineGrid[y][x] != 15) {
                        int numZero = 0;
                        int numFlags = 0;
                        for (int i = -1; i < 2; i++) {
                            for (int j = -1; j < 2; j++) {
                                if (i != 0 || j != 0) {
                                    int testCol = j + x;
                                    int testRow = i + y;

                                    try {
                                        if (mineGrid[testRow][testCol] == 0) {
                                            numZero++;
                                        } else if (mineGrid[testRow][testCol] == 15) {
                                            numFlags++;
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Array out of bounds");

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
                                            if (mineGrid[testRow][testCol] == 0) {
                                                if (numGuesses == 0) {
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
                                        } catch (Exception e) {
                                            System.out.println("Array out of bounds");

                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
            return;
            //implement best guess
        }


    }



    public static void main(String[] args) {
        try {
            Runtime.getRuntime().exec("cmd.exe /C START " + "C:\\Windows\\winsxs\\amd64_microsoft-windows-s..oxgames-minesweeper_31bf3856ad364e35_6.1.7600.16385_none_fe560f0352e04f48\\minesweeper.exe");
        } catch (Exception e) {
            System.out.println("Couldn't run minesweeper!");
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Screen screen = new Screen();

        try {
            while (true) {
                doNextMove(screen);
                Thread.sleep(400);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

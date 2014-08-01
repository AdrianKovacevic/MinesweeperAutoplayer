package com.company;



import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Screen {

    final int ROW_SIZE = 16;
    final int COLUMN_SIZE = 30;
    final int CELL_UNKNOWN = 0;
    final int CELL_ONE = 1;
    final int CELL_TWO = 2;
    final int CELL_THREE = 3;
    final int CELL_FOUR = 4;
    final int CELL_FIVE = 5;
    final int CELL_SIX = 6;
    final int CELL_SEVEN = 7;
    final int CELL_BLANK = 11;
    final int CELL_FLAG = 15;

    public Screen () {
        screenWidth = 0;
        screenHeight = 0;
        mineGridTopCornerX = 0;
        mineGridTopCornerY = 0;
        mineGridBottomCornerX = 0;
        mineGridBottomCornerY = 0;
        screenShot = null;
        mineGrid = new int[ROW_SIZE][COLUMN_SIZE];
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

    public int[][] getMineGrid() {
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


    public boolean fillMineGrid() {

        screenShot = null;
        takeScreenshot();
        boolean topCornerFound = false;

        mineGrid = new int[ROW_SIZE][COLUMN_SIZE];

// finds the corners by looking for a certain pixel, not good
//            for (int y = 0; y < screenHeight; y++) {
//                for (int x = 0; x < screenWidth; x++) {
//                    Color colour = new Color(screenShot.getRGB(x, y));
//                    if (colour.getRed() == 168 && colour.getGreen() == 183 && colour.getBlue() == 203 && topCornerFound == false) {
//                        mineGridTopCornerX = x;
//                        mineGridTopCornerY = y;
//                        topCornerFound = true;
//                    }
//                    if (colour.getRed() == 107 && colour.getGreen() == 117 && colour.getBlue() == 143) {
//                        mineGridBottomCornerX = x;
//                        mineGridBottomCornerY = y;
//                    }
//                }
//            }

        int[] rect = new int[4];
        try {
            rect = GetWindowRect.getRect("Minesweeper");
        } catch (GetWindowRect.WindowNotFoundException e) {
            e.printStackTrace();
        } catch (GetWindowRect.GetWindowRectException e) {
            e.printStackTrace();
        }


        System.out.println("Moving... ");


            mineGridTopCornerX = rect[0] + 37;
            mineGridTopCornerY = rect[1] +  80;
            mineGridBottomCornerX = rect[2] - 37;
            mineGridBottomCornerY = rect[3] - 40;

        robot.mouseMove(mineGridTopCornerX, mineGridTopCornerY);
        Color colour2 = new Color(screenShot.getRGB(mineGridTopCornerX, mineGridTopCornerY));
        int red2 = colour2.getRed();
        int green2 = colour2.getGreen();
        int blue2 = colour2.getBlue();
                    System.out.println(red2 + " " + green2 + " " + blue2);
                    BufferedImage ss = screenShot.getSubimage(mineGridTopCornerX, mineGridTopCornerY, 10, 10);
                    File outputfile = new File("saved.png");
                    try {
                        ImageIO.write(ss, "png", outputfile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }



            int row = 0;
            int column = 0;
            one = 0;
            two = 0;
            three = 0;
            four = 0;
            five = 0;
            six = 0;
            blank = 0;
            flag = 0;
            int testNum = 0;
            int gameOverPopup = 0;

            for (int y = mineGridTopCornerY + 17; y < mineGridBottomCornerY; y += 18) {

                for (int x = mineGridTopCornerX + 13; x < mineGridBottomCornerX; x += 18) {
                    Color colour = new Color(screenShot.getRGB(x, y));
                    int red = colour.getRed();
                    int green = colour.getGreen();
                    int blue = colour.getBlue();
//                if (testNum == 30*0 + 2) {
//                    robot.mouseMove(x, y);
//                    System.out.println(red + " " + green + " " + blue);
//                    BufferedImage ss = screenShot.getSubimage(x, y, 10, 10);
//                    File outputfile = new File("saved.png");
//                    try {
//                        ImageIO.write(ss, "png", outputfile);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                }
                    if (red == 240 && green == 240 && blue == 240) {
                        gameOverPopup += 1;
                        for (int i = 1; i < 25; i++) {
                            Color gameOverColour = new Color(screenShot.getRGB(x + i, y));
                            if (gameOverColour.getRed() == 240 && gameOverColour.getGreen() == 240 && gameOverColour.getBlue() == 240) {
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

                    if (red <= 230 && red > 160 && green <= 85 && green >= 0 &&  blue <= 90 && blue >= 0) {
                        //                    System.out.println("Three?");
                        three++;
                        mineGrid[row][column % COLUMN_SIZE] = 3;
                        givenValue = true;
                    } else if (red <= 67 && red >= 55 && green <= 85 && green >= 75 &&  blue <= 195 && blue >= 185) {
                        colour = robot.getPixelColor(x, y + 2);
                        if (colour.getRed() >= 150 && colour.getGreen() >= 150) {
                            one++;
                            mineGrid[row][column % COLUMN_SIZE] = 1;
                            givenValue = true;
                        }
                    } else if (red <= 230 && red >= 160 && green <= 245 && green >= 170 &&  blue <= 255 && blue >= 200) {
                        blank++;
                        mineGrid[row][column % COLUMN_SIZE] = 11;
                        givenValue = true;
                    }  else if (red <= 65 && red >= 10 && green <= 117 && green >= 97 &&  blue <= 50 && blue >= 0) {
                        two++;
                        mineGrid[row][column % COLUMN_SIZE] = 2;
                        givenValue = true;
                    }  else if (red <= 153 && red >= 115 && green <= 90 && green >= 0 &&  blue <= 100 && blue >= 0) {
                        five++;
                        mineGrid[row][column % COLUMN_SIZE] = 5;
                        givenValue = true;
                    }  else if (red <= 20 && green <= 130 && green >= 119 &&  blue <= 130 && blue >= 115) {
                        six++;
                        mineGrid[row][column % COLUMN_SIZE] = 6;
                        givenValue = true;
                    } else if (red <= 164 && red >= 150 && green <= 163 && green >= 150 &&  blue <= 170 && blue >= 156) {
                        flag++;
                        mineGrid[row][column % COLUMN_SIZE] = 15;
                        givenValue = true;
                    }

                    if (givenValue == false) {
                        colour = new Color(screenShot.getRGB(x + 1, y));
                        red = colour.getRed();
                        green = colour.getGreen();
                        blue = colour.getBlue();
                        if (red <= 40 && green <= 40 && blue <= 145 && blue >= 120) {
                            four++;
                            mineGrid[row][column % COLUMN_SIZE] = 4;
                        }
                    }



                    column++;

//                colour = robot.getPixelColor(x, y);
//                robot.mouseMove(x, y);
//                Thread.sleep(100);
//                System.out.println(red + " " + green + " " + blue);

                testNum++;
                }
                row++;
//                if (testNum == 221) {
//                    break;
//                }
            }

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return false;

    };

    public int[] getTilePos (int row, int column) {
        int[] tilePos = new int[2];
        tilePos[0] = mineGridTopCornerX + 6 + (18 * column);
        tilePos[1] = mineGridTopCornerY + 6 + (18 * row);

        return tilePos;
    };


    private void takeScreenshot () {
        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        screenShot = robot.createScreenCapture(screenSize);
        screenWidth = screenSize.width;
        screenHeight = screenSize.height;

        return;
    };



    private int[][] mineGrid;

    private BufferedImage screenShot;

    private int screenWidth;

    private int screenHeight;

    public Robot robot;

    private int mineGridTopCornerX;

    private int mineGridTopCornerY;

    private int mineGridBottomCornerX;

    private int mineGridBottomCornerY;

    // bugfixing purposes
    public int one;
    public int two;
    public int three;
    public int four;
    public int five;
    public int six;
    public int blank;
    public int flag;




}

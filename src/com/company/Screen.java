package com.company;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

public class Screen {

    public Screen () {
        screenWidth = 0;
        screenHeight = 0;
        mineGridTopCornerX = 0;
        mineGridTopCornerY = 0;
        mineGridBottomCornerX = 0;
        mineGridBottomCornerY = 0;
        gameOver = false;
        screenShot = null;
        mineGrid = new int[16][30];
        try {
            robot = new Robot();
        } catch (Exception e) {
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

    public boolean isGameOver () {
       return gameOver;
    };

    public void fillMineGrid() {

        takeScreenshot();

            for (int y = 0; y < screenHeight; y++) {
                for (int x = 0; x < screenWidth; x++) {
                    Color colour = new Color(screenShot.getRGB(x, y));
                    if (colour.getRed() == 168 && colour.getGreen() == 183 && colour.getBlue() == 203) {
                        mineGridTopCornerX = x;
                        mineGridTopCornerY = y;
                    }
                    if (colour.getRed() == 107 && colour.getGreen() == 117 && colour.getBlue() == 143) {
                        mineGridBottomCornerX = x;
                        mineGridBottomCornerY = y;
                    }
                }
            }

            int row = 0;
            int column = 0;
            int one = 0;
            int two = 0;
            int three = 0;
            int four = 0;
            int five = 0;
            int blank = 0;
            int flag = 0;
            int testNum = 0;

            for (int y = mineGridTopCornerY + 17; y < mineGridBottomCornerY; y += 18) {

                for (int x = mineGridTopCornerX + 13; x < mineGridBottomCornerX; x += 18) {
                    Color colour = new Color(screenShot.getRGB(x, y));
                    int red = colour.getRed();
                    int green = colour.getGreen();
                    int blue = colour.getBlue();
//                robot.mouseMove(x, y);
//                if (testNum == 7) {
//                    System.out.println(red + " " + green + " " + blue);
//                    screen = screen.getSubimage(x, y, 10, 10);
//                    File outputfile = new File("saved.png");
//                    ImageIO.write(screen, "png", outputfile);
//                    break;
//                }
                    if (red <= 230 && red > 160 && green <= 70 && green >= 0 &&  blue <= 80 && blue >= 0) {
                        //                    System.out.println("Three?");
                        //                    three++;
                        mineGrid[row][column % 30] = 3;

                    } else if (red <= 67 && red >= 55 && green <= 85 && green >= 75 &&  blue <= 195 && blue >= 185) {
                        colour = robot.getPixelColor(x, y + 2);
                        if (colour.getRed() >= 150 && colour.getGreen() >= 150) {
                            one++;
                            mineGrid[row][column % 30] = 1;
                        }
                    } else if (red <= 230 && red >= 165 && green <= 245 && green >= 175 &&  blue <= 255 && blue >= 206) {
                        blank++;
                        mineGrid[row][column % 30] = 11;
                    }  else if (red <= 65 && red >= 10 && green <= 117 && green >= 97 &&  blue <= 50 && blue >= 0) {
                        two++;
                        mineGrid[row][column % 30] = 2;
                    }  else if (red <= 135 && red >= 115 && green <= 50 && green >= 0 &&  blue <= 50 && blue >= 0) {
                        five++;
                        mineGrid[row][column % 30] = 5;
                    } else if (red <= 120 && red >= 75 && green <= 125 && green >= 80 &&  blue <= 175 && blue >= 160) {
                        four++;
                        mineGrid[row][column % 30] = 4;
                    } else if (red <= 164 && red >= 156 && green <= 163 && green >= 155 &&  blue <= 164 && blue >= 156) {
                        flag++;
                        mineGrid[row][column % 30] = 15;
                    }
                    column++;

//                colour = robot.getPixelColor(x, y);
//                robot.mouseMove(x, y);
//                Thread.sleep(100);
//                System.out.println(red + " " + green + " " + blue);

//                testNum++;
                }
                row++;
//           if (testNum == 7) {
//                break;
//            }
            }

        return;

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

    private boolean gameOver;


}

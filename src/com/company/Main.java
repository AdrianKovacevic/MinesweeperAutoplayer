package com.company;

import java.awt.Robot;
import java.awt.*;
import java.awt.image.BufferedImage;


public class Main {

    private static int screenWidth;
    private static int screenHeight;

    public static BufferedImage takeScreenshot (Robot robot) {

            try {

                Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                screenWidth = screenSize.width;
                screenHeight = screenSize.height;
                BufferedImage screen = robot.createScreenCapture(screenSize);
                return screen;

            }
            catch(Exception e) {
                System.out.println("Couldn't take a screenshot!");
            }

            return null;
    }


    public static void main(String[] args) throws Throwable {
        try {
            Runtime.getRuntime().exec("cmd.exe /C START " + "C:\\Windows\\winsxs\\amd64_microsoft-windows-s..oxgames-minesweeper_31bf3856ad364e35_6.1.7600.16385_none_fe560f0352e04f48\\minesweeper.exe");
        } catch (Exception e) {
            System.out.println("Couldn't run minesweeper!");
        }
        Thread.sleep(3000);

        Robot robot = new Robot();

        BufferedImage screen = takeScreenshot(robot);
        int topCornerX = 0;
        int topCornerY = 0;
        int bottomCornerX = 0;
        int bottomCornerY = 0;

        for (int y = 0; y < screenHeight; y++) {
            for (int x = 0; x < screenWidth; x++) {
                Color colour = new Color(screen.getRGB(x, y));
                if (colour.getRed() == 168 && colour.getGreen() == 183 && colour.getBlue() == 203) {
                    topCornerX = x;
                    topCornerY = y;
                }
                if (colour.getRed() == 107 && colour.getGreen() == 117 && colour.getBlue() == 143) {
                    bottomCornerX = x;
                    bottomCornerY = y;
                }
            }
        }

        int[][] mineGrid = new int[16][30];
        int row = 0;
        int column = 0;
        int one = 0;
        int two = 0;
        int three = 0;
        int four = 0;
        int five = 0;
        int blank = 0;

        for (int y = topCornerY + 17; y < bottomCornerY; y += 18) {

            for (int x = topCornerX + 14; x < bottomCornerX; x += 18) {
                Color colour = new Color(screen.getRGB(x, y));
                int red = colour.getRed();
                int green = colour.getGreen();
                int blue = colour.getBlue();

                if (red <= 190 && red >= 160 && green <= 125 && green >= 80 &&  blue <= 130 && blue >= 80) {
                    System.out.println("Three?");
                    three++;
                    robot.mouseMove(x, y);
                    System.out.println(red + " " + green + " " + blue);
                    // fix the three and five recognition
                } else if (red <= 80 && red >= 60 && green <= 95 && green >= 75 &&  blue <= 192 && blue >= 188) {
                    System.out.println("One?");
                    one++;

                } else if (red <= 230 && red >= 165 && green <= 245 && green >= 175 &&  blue <= 255 && blue >= 200) {
                    System.out.println("Blank?");
                    blank++;
                }  else if (red <= 45 && red >= 10 && green <= 110 && green >= 100 &&  blue <= 10 && blue >= 0) {
                    System.out.println("Two?");
                    two++;
                }  else if (red <= 165 && red >= 145 && green <= 115 && green >= 105 &&  blue <= 130 && blue >= 110) {
                    System.out.println("Five?");
                    five++;

                } else if (red <= 10 && red >= 0 && green <= 10 && green >= 0 &&  blue <= 135 && blue >= 125) {
                    System.out.println("Four?");
                    four++;
                }
//                robot.mouseMove(x + 126, y + 108);
//                return;

                //colour = robot.getPixelColor(x, y);
//                robot.mouseMove(x, y);
//                Thread.sleep(100);
//                System.out.println(red + " " + green + " " + blue);
                column++;
            }
            row++;
        }









	// write your code here

    }
}

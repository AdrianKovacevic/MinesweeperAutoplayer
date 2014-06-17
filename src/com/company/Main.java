package com.company;

import java.awt.Robot;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Main {


    public static BufferedImage takeScreenshot (Robot robot) {




            try {

                Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                int screenWidth = screenSize.width;
                int screenHeight = screenSize.height;
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

        Robot robot = new Robot();
        BufferedImage screen = takeScreenshot(robot);








	// write your code here

    }
}

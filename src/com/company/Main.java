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

    public static void doNextMove (Screen screen) {
        boolean isEmpty = true;
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
            screen.robot.mouseRelease(InputEvent.BUTTON1_MASK);
        }
    }


    public static void main(String[] args) throws Throwable {
        try {
            Runtime.getRuntime().exec("cmd.exe /C START " + "C:\\Windows\\winsxs\\amd64_microsoft-windows-s..oxgames-minesweeper_31bf3856ad364e35_6.1.7600.16385_none_fe560f0352e04f48\\minesweeper.exe");
        } catch (Exception e) {
            System.out.println("Couldn't run minesweeper!");
        }

        Thread.sleep(3000);

        Screen screen = new Screen();
        screen.fillMineGrid();
        doNextMove(screen);



    }
}

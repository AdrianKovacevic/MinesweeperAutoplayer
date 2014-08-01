package com.company;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.Arrays;


public class GetWindowRect {

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

        WinDef.HWND FindWindow(String lpClassName, String lpWindowName);

        int GetWindowRect(WinDef.HWND handle, int[] rect);
    }

    public static int[] getRect(String windowName) throws WindowNotFoundException,
            GetWindowRectException {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
        if (hwnd == null) {
            throw new WindowNotFoundException("", windowName);
        }

        int[] rect = {0, 0, 0, 0};
        int result = User32.INSTANCE.GetWindowRect(hwnd, rect);
        if (result == 0) {
            throw new GetWindowRectException(windowName);
        }
        return rect;
    }

    @SuppressWarnings("serial")
    public static class WindowNotFoundException extends Exception {
        public WindowNotFoundException(String className, String windowName) {
            super(String.format("Window null for className: %s; windowName: %s",
                    className, windowName));
        }
    }

    @SuppressWarnings("serial")
    public static class GetWindowRectException extends Exception {
        public GetWindowRectException(String windowName) {
            super("Window Rect not found for " + windowName);
        }
    }

    public static void main(String[] args) throws AWTException {
        String windowName = "Minesweeper";
        int[] rect;
        Robot robot = new Robot() ;
        try {
            rect = GetWindowRect.getRect(windowName);
            System.out.printf("The corner locations for the window \"%s\" are %s",
                    windowName, Arrays.toString(rect));
            robot.mouseMove(rect[0], rect[1]);
        } catch (GetWindowRect.WindowNotFoundException e) {
            e.printStackTrace();
        } catch (GetWindowRect.GetWindowRectException e) {
            e.printStackTrace();
        }
    }
}

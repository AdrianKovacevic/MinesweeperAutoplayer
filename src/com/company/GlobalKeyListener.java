package com.company;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

/**
 * @author Adrian Kovacevic akovacev@uwaterloo.ca
 * Created by Adrian Kovacevic on August 14, 2014
 */

public class GlobalKeyListener implements NativeKeyListener {
    boolean[] currentPressedKeys = new boolean[3];

    /**
     * Un-registers the hook when CTRL ALT and D are pressed at the same time, allowing the loop in Main to close due to
     * this
     *
     * @param e The key that has triggered an event
     */

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {

        if (e.getKeyCode() == NativeKeyEvent.VK_D) {
            currentPressedKeys[0] = true;
            if (currentPressedKeys[1] && currentPressedKeys[2]) {
                GlobalScreen.unregisterNativeHook();
            }
        } else if (e.getKeyCode() == NativeKeyEvent.VK_CONTROL) {
            currentPressedKeys[1] = true;
            if (currentPressedKeys[0] && currentPressedKeys[2]) {
                GlobalScreen.unregisterNativeHook();
            }
        } else if (e.getKeyCode() == NativeKeyEvent.VK_ALT) {
            currentPressedKeys[2] = true;
            if (currentPressedKeys[0] && currentPressedKeys[1]) {
                GlobalScreen.unregisterNativeHook();
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VK_D) {
            currentPressedKeys[0] = false;
        } else if (e.getKeyCode() == NativeKeyEvent.VK_CONTROL) {
            currentPressedKeys[1] = false;
        } else if (e.getKeyCode() == NativeKeyEvent.VK_ALT) {
            currentPressedKeys[2] = false;
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}
}

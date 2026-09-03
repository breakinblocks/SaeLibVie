package com.breakinblocks.saelibvie.ui.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputQuirks;
import org.lwjgl.glfw.GLFW;

public final class Modifiers {
    private Modifiers() {
    }

    private static boolean down(int left, int right) {
        Window window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, left) || InputConstants.isKeyDown(window, right);
    }

    public static boolean shift() {
        return down(GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public static boolean ctrl() {
        if (InputQuirks.ON_OSX) {
            return down(GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER);
        }
        return down(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    public static boolean alt() {
        return down(GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT);
    }

    public static boolean shift(int modifiers) {
        return (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
    }

    public static boolean ctrl(int modifiers) {
        return (modifiers & InputQuirks.EDIT_SHORTCUT_KEY_MODIFIER) != 0;
    }

    public static boolean alt(int modifiers) {
        return (modifiers & GLFW.GLFW_MOD_ALT) != 0;
    }

    private static boolean editShortcut(int key, int modifiers, int expected) {
        return key == expected && ctrl(modifiers) && !shift(modifiers) && !alt(modifiers);
    }

    public static boolean isSelectAll(int key, int modifiers) {
        return editShortcut(key, modifiers, GLFW.GLFW_KEY_A);
    }

    public static boolean isCopy(int key, int modifiers) {
        return editShortcut(key, modifiers, GLFW.GLFW_KEY_C);
    }

    public static boolean isPaste(int key, int modifiers) {
        return editShortcut(key, modifiers, GLFW.GLFW_KEY_V);
    }

    public static boolean isCut(int key, int modifiers) {
        return editShortcut(key, modifiers, GLFW.GLFW_KEY_X);
    }

    public static int step(int base, int shiftStep, int ctrlStep) {
        if (ctrl()) return ctrlStep;
        if (shift()) return shiftStep;
        return base;
    }

    public static int step(int base) {
        return step(base, base * 10, base * 64);
    }

    public static int signedStep(double scrollY, int base, int shiftStep, int ctrlStep) {
        int magnitude = step(base, shiftStep, ctrlStep);
        return scrollY > 0 ? magnitude : -magnitude;
    }
}

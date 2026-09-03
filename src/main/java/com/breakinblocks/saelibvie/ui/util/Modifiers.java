package com.breakinblocks.saelibvie.ui.util;

import net.minecraft.client.gui.screens.Screen;

public final class Modifiers {
    private Modifiers() {
    }

    public static boolean shift() {
        return Screen.hasShiftDown();
    }

    public static boolean ctrl() {
        return Screen.hasControlDown();
    }

    public static boolean alt() {
        return Screen.hasAltDown();
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

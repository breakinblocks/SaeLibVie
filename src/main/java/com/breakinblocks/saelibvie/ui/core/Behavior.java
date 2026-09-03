package com.breakinblocks.saelibvie.ui.core;

import com.breakinblocks.saelibvie.ui.render.UiGraphics;

public interface Behavior {
    default void tick(Widget widget) {
    }

    default boolean mouseClicked(Widget widget, double lx, double ly, int button) {
        return false;
    }

    default boolean mouseReleased(Widget widget, double lx, double ly, int button) {
        return false;
    }

    default boolean mouseDoubleClicked(Widget widget, double lx, double ly, int button) {
        return false;
    }

    default boolean mouseDragged(Widget widget, double lx, double ly, int button, double dx, double dy) {
        return false;
    }

    default boolean mouseScrolled(Widget widget, double lx, double ly, double scrollX, double scrollY) {
        return false;
    }

    default boolean keyPressed(Widget widget, int key, int scanCode, int modifiers) {
        return false;
    }

    default void paintOverlay(Widget widget, UiGraphics g) {
    }

    default boolean isActive(Widget widget) {
        return false;
    }
}

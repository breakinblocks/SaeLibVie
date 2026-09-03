package com.breakinblocks.saelibvie.ui.screen;

import com.breakinblocks.saelibvie.ui.core.CursorType;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;
import java.util.Map;

public final class SaeLibVieCursor {
    private static final Map<CursorType, Long> CURSORS = new EnumMap<>(CursorType.class);
    @Nullable
    private static CursorType applied;

    private SaeLibVieCursor() {
    }

    @Nullable
    public static UiRoot rootOf(@Nullable Screen screen) {
        if (screen instanceof SaeScreen sae) return sae.root();
        if (screen instanceof SaeContainerScreen<?> container) return container.root();
        return null;
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        UiRoot root = rootOf(mc.screen);
        CursorType requested = root == null ? null : root.requestedCursor();
        if (requested == null || requested == CursorType.DEFAULT) {
            reset();
            return;
        }
        apply(requested);
    }

    public static void apply(CursorType type) {
        if (applied == type) return;
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();
        long cursor = CURSORS.computeIfAbsent(type, t -> GLFW.glfwCreateStandardCursor(t.glfwShape()));
        GLFW.glfwSetCursor(window, cursor);
        applied = type;
    }

    public static void reset() {
        if (applied == null) return;
        Minecraft mc = Minecraft.getInstance();
        GLFW.glfwSetCursor(mc.getWindow().getWindow(), 0L);
        applied = null;
    }
}

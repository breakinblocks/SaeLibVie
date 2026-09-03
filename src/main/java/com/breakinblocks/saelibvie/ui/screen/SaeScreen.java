package com.breakinblocks.saelibvie.ui.screen;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.color.Theme;
import com.breakinblocks.saelibvie.ui.color.Themes;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.ClientTasks;
import com.breakinblocks.saelibvie.ui.widget.TextField;
import com.breakinblocks.saelibvie.ui.widget.Window;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public abstract class SaeScreen extends Screen {
    protected final UiRoot root;
    @Nullable
    protected Screen parent;
    private boolean pauses;
    private boolean dimBackground = true;
    private boolean blurBackground;
    private boolean returnToParent = true;
    private boolean cursorRecorded;
    private double savedCursorX;
    private double savedCursorY;
    private boolean opened;

    protected SaeScreen(Component title) {
        this(title, Minecraft.getInstance().screen, Themes.getDefault());
    }

    protected SaeScreen(Component title, @Nullable Screen parent) {
        this(title, parent, Themes.getDefault());
    }

    protected SaeScreen(Component title, @Nullable Screen parent, Theme theme) {
        super(title);
        this.parent = parent;
        this.root = new UiRoot(theme);
        this.root.onCloseRequest(this::close);
    }

    protected abstract void build(UiRoot root);

    public UiRoot root() {
        return root;
    }

    public Theme theme() {
        return root.theme();
    }

    public void setTheme(Theme theme) {
        root.setTheme(theme);
    }

    @Nullable
    public Screen getParentScreen() {
        return parent;
    }

    public void setParent(@Nullable Screen parent) {
        this.parent = parent;
    }

    @Nullable
    public Screen effectiveParent() {
        if (parent instanceof ChatScreen) return null;
        if (parent instanceof BusyScreen busy) return busy.effectiveParent();
        return parent;
    }

    protected SaeScreen pauses(boolean pauses) {
        this.pauses = pauses;
        return this;
    }

    protected SaeScreen dimBackground(boolean dim) {
        this.dimBackground = dim;
        return this;
    }

    protected SaeScreen blurBackground(boolean blur) {
        this.blurBackground = blur;
        return this;
    }

    public SaeScreen returnToParent(boolean value) {
        this.returnToParent = value;
        return this;
    }

    public boolean returnsToParent() {
        return returnToParent;
    }

    public boolean isCurrent() {
        return Minecraft.getInstance().screen == this;
    }

    public void openIfNotCurrent() {
        if (!isCurrent()) {
            Minecraft.getInstance().setScreen(this);
        }
    }

    public void openLater() {
        ClientTasks.later(this::openIfNotCurrent);
    }

    public Runnable openAfter(Runnable action) {
        return () -> {
            action.run();
            openIfNotCurrent();
        };
    }

    @Override
    protected void init() {
        super.init();
        if (!opened) {
            opened = true;
            Minecraft mc = Minecraft.getInstance();
            savedCursorX = mc.mouseHandler.xpos();
            savedCursorY = mc.mouseHandler.ypos();
            cursorRecorded = true;
        }
        root.closeAllLayers();
        root.clear();
        root.setBounds(new Rect(0, 0, width, height));
        root.setScreenSize(width, height);
        build(root);
        root.layoutNow();
    }

    public Widget openWindow(Window window) {
        root.add(window);
        root.clampToScreen(window);
        return window;
    }

    public void openCentered(Window window) {
        root.add(window);
        root.centerOnScreen(window);
    }

    public void pushModal(Widget widget) {
        root.centerOnScreen(widget);
        root.pushLayer(widget);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (dimBackground) {
            if (blurBackground) {
                renderBackground(graphics, mouseX, mouseY, partialTick);
            } else if (minecraft != null && minecraft.level != null) {
                graphics.fill(0, 0, width, height, root.theme().color(ColorToken.OVERLAY_DIM));
            } else {
                renderPanorama(graphics, partialTick);
                renderBlurredBackground(partialTick);
                renderMenuBackground(graphics);
            }
        }
        root.renderFrame(graphics, mouseX, mouseY, partialTick);
        renderOverlay(graphics, mouseX, mouseY, partialTick);
    }

    protected void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void tick() {
        super.tick();
        root.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 3) {
            if (!root.handleEscape()) {
                onEscape();
            }
            return true;
        }
        if (root.handleMouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (root.handleMouseReleased(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (root.handleMouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (root.handleMouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        root.handleMouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (root.handleKeyPressed(keyCode, scanCode, modifiers)) return true;
        if (keyCode == InputConstants.KEY_ESCAPE && shouldCloseOnEsc()) {
            onEscape();
            return true;
        }
        if (isInventoryKey(keyCode, scanCode) && closesOnInventoryKey()) {
            onEscape();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    protected boolean isInventoryKey(int keyCode, int scanCode) {
        return minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode);
    }

    protected boolean closesOnInventoryKey() {
        return false;
    }

    protected void onEscape() {
        close();
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (root.handleKeyReleased(keyCode, scanCode, modifiers)) return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (root.handleCharTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return pauses;
    }

    public void close() {
        root.forEachDescendant(w -> {
            if (w instanceof TextField field) {
                field.commitIfDirty();
            }
        });
        root.closeContextMenu();
        root.closeAllLayers();
        Minecraft mc = Minecraft.getInstance();
        Screen target = returnToParent ? effectiveParent() : null;
        mc.setScreen(target);
        if (target != null && cursorRecorded) {
            GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), savedCursorX, savedCursorY);
        }
        SaeLibVieCursor.reset();
    }

    @Override
    public void onClose() {
        close();
    }

    @Override
    public void removed() {
        super.removed();
        SaeLibVieCursor.reset();
    }

    protected static void drawScrim(UiGraphics g, Rect rect) {
        Painter.scrim(g, rect);
    }
}

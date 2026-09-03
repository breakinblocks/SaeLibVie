package com.breakinblocks.saelibvie.ui.screen;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.color.Theme;
import com.breakinblocks.saelibvie.ui.color.Themes;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.MenuButtons;
import com.breakinblocks.saelibvie.ui.widget.TextField;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public abstract class SaeContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected final UiRoot root;
    private boolean drawWindowChrome = true;
    private boolean drawVanillaLabels;
    private boolean drawTitleStrip = true;
    @Nullable
    private UiGraphics frame;

    protected SaeContainerScreen(T menu, Inventory inventory, Component title, int imageWidth, int imageHeight) {
        this(menu, inventory, title, imageWidth, imageHeight, Themes.getDefault());
    }

    protected SaeContainerScreen(T menu, Inventory inventory, Component title, int imageWidth, int imageHeight, Theme theme) {
        super(menu, inventory, title, imageWidth, imageHeight);
        this.root = new UiRoot(theme);
        this.root.onCloseRequest(this::onClose);
        this.inventoryLabelY = imageHeight - 94;
    }

    protected abstract void build(UiRoot root);

    public UiRoot root() {
        return root;
    }

    public Theme theme() {
        return root.theme();
    }

    protected void windowChrome(boolean draw) {
        this.drawWindowChrome = draw;
    }

    protected void vanillaLabels(boolean draw) {
        this.drawVanillaLabels = draw;
    }

    protected void titleStrip(boolean draw) {
        this.drawTitleStrip = draw;
    }

    protected Rect titleStripRect() {
        return new Rect(4, 4, imageWidth - 8, root.theme().headerHeight());
    }

    protected void sendButton(int id) {
        MenuButtons.send(menu, id);
    }

    @Override
    protected void init() {
        super.init();
        root.closeAllLayers();
        root.clear();
        root.setBounds(new Rect(leftPos, topPos, imageWidth, imageHeight));
        root.setScreenSize(width, height);
        build(root);
        root.layoutNow();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        frame = root.beginFrame(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (frame != null) {
            UiGraphics g = frame;
            frame = null;
            root.renderLayersAndTooltip(g);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (drawVanillaLabels) {
            super.extractLabels(graphics, mouseX, mouseY);
        }
    }

    protected void paintWindow(UiGraphics g) {
        if (drawWindowChrome) {
            Painter.window(g, root.localRect());
        }
        if (drawTitleStrip) {
            Rect strip = titleStripRect();
            Painter.inset(g, strip);
            g.centeredText(g.headerText(title, strip.w() - 8), strip.centerX(), strip.y() + (strip.h() - 7) / 2, g.color(ColorToken.TEXT_TITLE), g.theme().textShadow());
        }
    }

    protected void installWindowBackground() {
        root.background((panel, g) -> paintWindow(g));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        root.tick();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (root.handleMouseClicked(event.x(), event.y(), event.button())) return true;
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (root.handleMouseReleased(event.x(), event.y(), event.button())) return true;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (root.handleMouseDragged(event.x(), event.y(), event.button(), dragX, dragY)) return true;
        return super.mouseDragged(event, dragX, dragY);
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
    public boolean keyPressed(KeyEvent event) {
        if (root.handleKeyPressed(event.key(), event.scancode(), event.modifiers())) return true;
        if (event.key() == InputConstants.KEY_ESCAPE && root.hasLayers()) {
            root.popLayer();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (root.handleKeyReleased(event.key(), event.scancode(), event.modifiers())) return true;
        return super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (root.handleCharTyped(event.codepoint())) return true;
        return super.charTyped(event);
    }

    @Override
    public void onClose() {
        root.forEachDescendant(w -> {
            if (w instanceof TextField field) {
                field.commitIfDirty();
            }
        });
        super.onClose();
    }
}

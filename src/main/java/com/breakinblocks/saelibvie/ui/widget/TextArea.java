package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.mixin.MultilineTextFieldAccessor;
import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.CursorType;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class TextArea extends Widget {
    private MultilineTextField model;
    private final Font font;
    private int padding = 4;
    @Nullable
    private Component placeholder;
    @Nullable
    private Consumer<String> responder;
    private boolean editable = true;
    private boolean drawBackground = true;
    private boolean escapeCancels;
    private boolean dragging;

    public TextArea() {
        this.font = Minecraft.getInstance().font;
        this.model = createModel(Math.max(20, width() - 8), "");
        focusable(true);
    }

    public TextArea(Rect bounds) {
        this();
        bounds(bounds);
        rebuildModel();
    }

    private MultilineTextField createModel(int width, String text) {
        MultilineTextField created = new MultilineTextField(font, width);
        created.setValue(text);
        created.setValueListener(value -> {
            updatePreferredHeight();
            if (responder != null) responder.accept(value);
        });
        return created;
    }

    private void rebuildModel() {
        int width = Math.max(20, width() - padding * 2);
        MultilineTextField old = model;
        int cursor = old.cursor();
        int limit = old.characterLimit();
        MultilineTextField created = createModel(width, old.value());
        created.setCharacterLimit(limit);
        created.seekCursor(Whence.ABSOLUTE, cursor);
        model = created;
    }

    public TextArea value(String text) {
        setValue(text);
        return this;
    }

    public TextArea placeholder(Component text) {
        this.placeholder = text;
        return this;
    }

    public TextArea responder(Consumer<String> responder) {
        this.responder = responder;
        return this;
    }

    public TextArea maxLength(int length) {
        model.setCharacterLimit(length);
        return this;
    }

    public TextArea editable(boolean editable) {
        this.editable = editable;
        return this;
    }

    public TextArea background(boolean draw) {
        this.drawBackground = draw;
        return this;
    }

    public TextArea escapeCancels(boolean value) {
        this.escapeCancels = value;
        return this;
    }

    public String value() {
        return model.value();
    }

    public void setValue(String text) {
        model.setValue(text);
        updatePreferredHeight();
    }

    public int lineCount() {
        return model.getLineCount();
    }

    public void selectAll() {
        model.setSelecting(true);
        model.seekCursor(Whence.ABSOLUTE, 0);
        ((MultilineTextFieldAccessor) model).saelibvie$setSelectCursor(0);
        model.seekCursor(Whence.END, 0);
    }

    public boolean hasSelection() {
        return model.hasSelection();
    }

    public String selectedText() {
        return model.getSelectedText();
    }

    public void insertText(String text) {
        if (!editable) return;
        model.insertText(text);
    }

    public int lineHeight() {
        return font.lineHeight;
    }

    @Override
    @Nullable
    public CursorType cursor() {
        return editable && isEnabled() ? CursorType.IBEAM : null;
    }

    @Override
    protected void onResized() {
        rebuildModel();
        updatePreferredHeight();
    }

    private void updatePreferredHeight() {
        int h = padding * 2 + Math.max(1, model.getLineCount()) * font.lineHeight;
        preferredSize(width() > 0 ? width() : 100, h);
        if (parent() != null && h != height()) {
            setSize(width(), h);
            invalidateLayout();
        }
        revealCursor();
    }

    @Override
    protected Size measure() {
        return new Size(width() > 0 ? width() : 100, padding * 2 + Math.max(1, model.getLineCount()) * font.lineHeight);
    }

    private void revealCursor() {
        Panel p = parent();
        while (p != null && !(p instanceof ScrollPanel)) {
            p = p.parent();
        }
        if (p instanceof ScrollPanel scroll) {
            int line = 0;
            int cursor = model.cursor();
            for (int i = 0; i < model.getLineCount(); i++) {
                MultilineTextField.StringView view = model.getLineView(i);
                if (cursor >= view.beginIndex() && cursor <= view.endIndex()) {
                    line = i;
                    break;
                }
            }
            int y = y() + padding + line * font.lineHeight;
            scroll.reveal(new Rect(x(), y, width(), font.lineHeight));
        }
    }

    @Override
    protected void paint(UiGraphics g) {
        Rect r = localRect();
        boolean focused = isFocused();
        if (drawBackground) {
            Painter.textField(g, r, focused, isEnabled() && editable);
        }
        int color = !isEnabled() || !editable ? g.color(ColorToken.TEXT_DISABLED) : g.color(ColorToken.TEXT);
        int textX = padding;
        int textY = padding;
        String value = model.value();
        if (value.isEmpty() && !focused && placeholder != null) {
            g.text(placeholder, textX, textY, g.color(ColorToken.TEXT_DIM), false);
            return;
        }
        boolean showCursor = focused && (Util.getMillis() / 500) % 2 == 0;
        int cursor = model.cursor();
        int lineIndex = 0;
        for (MultilineTextField.StringView view : model.iterateLines()) {
            int y = textY + lineIndex * font.lineHeight;
            String line = value.substring(view.beginIndex(), view.endIndex());
            if (model.hasSelection()) {
                MultilineTextField.StringView selection = model.getSelected();
                int start = Math.max(view.beginIndex(), selection.beginIndex());
                int end = Math.min(view.endIndex(), selection.endIndex());
                if (start < end) {
                    int sx = textX + font.width(value.substring(view.beginIndex(), start));
                    int ex = textX + font.width(value.substring(view.beginIndex(), end));
                    g.fill(sx, y - 1, ex, y + font.lineHeight, g.color(ColorToken.SELECTED));
                }
            }
            g.text(line, textX, y, color, g.theme().textShadow());
            if (showCursor && cursor >= view.beginIndex() && cursor <= view.endIndex()) {
                boolean atEnd = cursor == view.endIndex() && lineIndex == model.getLineCount() - 1;
                int cx = textX + font.width(value.substring(view.beginIndex(), cursor));
                if (atEnd) {
                    g.text("_", cx, y, color, false);
                } else {
                    g.fill(cx, y - 1, cx + 1, y + font.lineHeight, color);
                }
            }
            lineIndex++;
        }
    }

    private void seekMouse(double lx, double ly) {
        int localX = (int) lx - padding;
        int localY = (int) ly - padding;
        model.seekCursorToPoint(localX, localY);
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (button != 0) return false;
        requestFocus();
        model.setSelecting(Screen.hasShiftDown());
        seekMouse(lx, ly);
        dragging = true;
        return true;
    }

    @Override
    protected boolean onMouseDoubleClicked(double lx, double ly, int button) {
        if (button != 0) return false;
        requestFocus();
        model.setSelecting(false);
        seekMouse(lx, ly);
        int start = model.getPreviousWord().beginIndex();
        int end = model.getNextWord().endIndex();
        model.setSelecting(false);
        model.seekCursor(Whence.ABSOLUTE, start);
        model.setSelecting(true);
        model.seekCursor(Whence.ABSOLUTE, end);
        model.setSelecting(false);
        return true;
    }

    @Override
    protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
        if (!dragging || button != 0) return false;
        model.setSelecting(true);
        seekMouse(lx, ly);
        model.setSelecting(false);
        return true;
    }

    @Override
    protected boolean onMouseReleased(double lx, double ly, int button) {
        if (!dragging) return false;
        dragging = false;
        return true;
    }

    @Override
    protected boolean onKeyPressed(int key, int scanCode, int modifiers) {
        if (!isFocused()) return false;
        if (key == InputConstants.KEY_ESCAPE) {
            if (escapeCancels) return false;
            UiRoot root = root();
            if (root != null) root.clearFocus();
            return true;
        }
        if ((key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER) && Screen.hasShiftDown()) {
            return false;
        }
        if (!editable) {
            if (Screen.isCopy(key)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(model.getSelectedText());
                return true;
            }
            if (Screen.isSelectAll(key)) {
                selectAll();
                return true;
            }
            return switch (key) {
                case InputConstants.KEY_LEFT, InputConstants.KEY_RIGHT, InputConstants.KEY_UP, InputConstants.KEY_DOWN,
                     InputConstants.KEY_HOME, InputConstants.KEY_END -> model.keyPressed(key);
                default -> false;
            };
        }
        boolean handled = model.keyPressed(key);
        if (handled) {
            revealCursor();
        }
        return handled;
    }

    @Override
    protected boolean onCharTyped(char character, int modifiers) {
        if (!isFocused() || !editable) return false;
        if (!StringUtil.isAllowedChatCharacter(character)) return false;
        model.insertText(Character.toString(character));
        revealCursor();
        return true;
    }

    public int cursorLineY() {
        int cursor = model.cursor();
        int line = 0;
        for (int i = 0; i < model.getLineCount(); i++) {
            MultilineTextField.StringView view = model.getLineView(i);
            if (cursor >= view.beginIndex() && cursor <= view.endIndex()) {
                line = i;
                break;
            }
        }
        return Mth.clamp(padding + line * font.lineHeight, 0, Math.max(0, height()));
    }
}

package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.CursorType;
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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TextField extends Widget implements UiRoot.ScrollTarget {
    private String value = "";
    private int cursor;
    private int selectionAnchor;
    private int scrollOffset;
    private int maxLength = 64;
    private Predicate<String> filter = s -> true;
    @Nullable
    private Predicate<String> validator;
    @Nullable
    private Component hint;
    @Nullable
    private Consumer<String> responder;
    @Nullable
    private Consumer<String> onCommit;
    @Nullable
    private BiFunction<String, Boolean, Optional<String>> onScroll;
    private boolean editable = true;
    private boolean dirty;
    private boolean settingProgrammatically;
    private boolean commitOnFocusLoss = true;
    private boolean selectAllOnFocus;
    private int padding = 4;
    private boolean drawBackground = true;
    private int textColorOverride = 0;

    public TextField() {
        focusable(true);
    }

    public TextField(Rect bounds) {
        super(bounds);
        focusable(true);
    }

    public static TextField numeric() {
        return new TextField().filter(s -> s.isEmpty() || s.equals("-") || s.chars().allMatch(c -> Character.isDigit(c) || c == '-'));
    }

    public TextField maxLength(int length) {
        this.maxLength = length;
        if (value.length() > length) {
            setValue(value.substring(0, length));
        }
        return this;
    }

    public TextField filter(Predicate<String> filter) {
        this.filter = filter;
        return this;
    }

    public TextField validator(@Nullable Predicate<String> validator) {
        this.validator = validator;
        return this;
    }

    public boolean isValid() {
        return validator == null || validator.test(value);
    }

    public TextField hint(Component hint) {
        this.hint = hint;
        return this;
    }

    public TextField responder(Consumer<String> responder) {
        this.responder = responder;
        return this;
    }

    public TextField onCommit(Consumer<String> onCommit) {
        this.onCommit = onCommit;
        return this;
    }

    public TextField onScroll(@Nullable BiFunction<String, Boolean, Optional<String>> onScroll) {
        this.onScroll = onScroll;
        return this;
    }

    public TextField editable(boolean editable) {
        this.editable = editable;
        return this;
    }

    public TextField commitOnFocusLoss(boolean commit) {
        this.commitOnFocusLoss = commit;
        return this;
    }

    public TextField selectAllOnFocus(boolean select) {
        this.selectAllOnFocus = select;
        return this;
    }

    public TextField background(boolean draw) {
        this.drawBackground = draw;
        return this;
    }

    public TextField textColor(int argb) {
        this.textColorOverride = argb;
        return this;
    }

    public String value() {
        return value;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isEditable() {
        return editable;
    }

    @Override
    public boolean acceptsScrollWhileFocused() {
        return onScroll != null;
    }

    @Override
    @Nullable
    public CursorType cursor() {
        return editable && isEnabled() ? CursorType.IBEAM : null;
    }

    public void setValue(String text) {
        settingProgrammatically = true;
        try {
            String clean = truncate(text == null ? "" : text);
            if (!filter.test(clean)) return;
            value = clean;
            cursor = Math.min(cursor, value.length());
            selectionAnchor = cursor;
            moveCursorToEnd();
            dirty = false;
            if (responder != null) responder.accept(value);
        } finally {
            settingProgrammatically = false;
        }
    }

    public boolean seed(String text) {
        if (isFocused() || dirty) return false;
        if (value.equals(text)) return false;
        setValue(text);
        return true;
    }

    public void commit() {
        if (!isValid()) return;
        if (onCommit != null) onCommit.accept(value);
        dirty = false;
    }

    public void commitIfDirty() {
        if (dirty) commit();
    }

    public void markDirty() {
        dirty = true;
    }

    private String truncate(String text) {
        String stripped = StringUtil.filterText(text);
        return stripped.length() > maxLength ? stripped.substring(0, maxLength) : stripped;
    }

    protected void applyEdit(String newValue, int newCursor) {
        if (!editable) return;
        String truncated = truncate(newValue);
        if (!filter.test(truncated)) return;
        value = truncated;
        cursor = Mth.clamp(newCursor, 0, value.length());
        selectionAnchor = cursor;
        if (!settingProgrammatically) dirty = true;
        onChanged();
        if (responder != null) responder.accept(value);
    }

    protected void onChanged() {
    }

    public void replaceText(String newValue) {
        String truncated = truncate(newValue);
        if (!filter.test(truncated)) return;
        value = truncated;
        cursor = value.length();
        selectionAnchor = cursor;
        dirty = true;
        onChanged();
        if (responder != null) responder.accept(value);
    }

    private boolean hasSelection() {
        return selectionAnchor != cursor;
    }

    private int selectionStart() {
        return Math.min(cursor, selectionAnchor);
    }

    private int selectionEnd() {
        return Math.max(cursor, selectionAnchor);
    }

    public String selectedText() {
        return value.substring(selectionStart(), selectionEnd());
    }

    public void insertText(String text) {
        if (!editable) return;
        String filtered = StringUtil.filterText(text);
        int start = selectionStart();
        int end = selectionEnd();
        int room = maxLength - value.length() + (end - start);
        if (room <= 0) return;
        if (filtered.length() > room) filtered = filtered.substring(0, room);
        String next = value.substring(0, start) + filtered + value.substring(end);
        applyEdit(next, start + filtered.length());
    }

    private void deleteChars(int direction) {
        if (!editable || value.isEmpty()) return;
        if (hasSelection()) {
            int start = selectionStart();
            int end = selectionEnd();
            applyEdit(value.substring(0, start) + value.substring(end), start);
            return;
        }
        int target = Mth.clamp(cursor + direction, 0, value.length());
        int start = Math.min(cursor, target);
        int end = Math.max(cursor, target);
        if (start == end) return;
        applyEdit(value.substring(0, start) + value.substring(end), start);
    }

    private void deleteWord(int direction) {
        if (!editable) return;
        int target = wordPosition(direction);
        int start = Math.min(cursor, target);
        int end = Math.max(cursor, target);
        if (start == end) return;
        applyEdit(value.substring(0, start) + value.substring(end), start);
    }

    private int wordPosition(int direction) {
        int pos = cursor;
        if (direction < 0) {
            while (pos > 0 && value.charAt(pos - 1) == ' ') pos--;
            while (pos > 0 && value.charAt(pos - 1) != ' ') pos--;
        } else {
            while (pos < value.length() && value.charAt(pos) != ' ') pos++;
            while (pos < value.length() && value.charAt(pos) == ' ') pos++;
        }
        return pos;
    }

    private void moveCursor(int position, boolean keepSelection) {
        cursor = Mth.clamp(position, 0, value.length());
        if (!keepSelection) selectionAnchor = cursor;
    }

    public void moveCursorToEnd() {
        moveCursor(value.length(), false);
    }

    public void moveCursorToStart() {
        moveCursor(0, false);
    }

    public void selectAll() {
        selectionAnchor = 0;
        cursor = value.length();
    }

    public void selectAllFromStart() {
        cursor = 0;
        selectionAnchor = value.length();
    }

    @Override
    protected Size measure() {
        return new Size(width() > 0 ? width() : 100, height() > 0 ? height() : 14);
    }

    private Rect textArea() {
        return localRect().inset(padding, 0, padding, 0);
    }

    private void ensureCursorVisible(Font font, int availableWidth) {
        int cursorX = font.width(value.substring(0, cursor));
        if (cursorX < scrollOffset) {
            scrollOffset = cursorX;
        } else if (cursorX - scrollOffset > availableWidth) {
            scrollOffset = cursorX - availableWidth;
        }
        int total = font.width(value);
        if (total - scrollOffset < availableWidth) {
            scrollOffset = Math.max(0, total - availableWidth);
        }
    }

    protected int textColor(UiGraphics g) {
        if (textColorOverride != 0) return textColorOverride;
        if (!isValid()) return g.color(ColorToken.NEGATIVE);
        if (!isEnabled() || !editable) return g.color(ColorToken.TEXT_DISABLED);
        return g.color(ColorToken.TEXT);
    }

    @Override
    protected void paint(UiGraphics g) {
        Rect r = localRect();
        boolean focused = isFocused();
        if (drawBackground) {
            Painter.textField(g, r, focused, isEnabled() && editable);
        }
        Rect area = textArea();
        Font font = g.font();
        ensureCursorVisible(font, area.w());
        int textY = r.y() + (r.h() - 8) / 2;
        int color = textColor(g);
        g.pushScissor(area);
        if (value.isEmpty() && hint != null && !focused) {
            g.text(hint, area.x(), textY, g.color(ColorToken.TEXT_DIM), false);
        } else {
            if (hasSelection()) {
                int sx = area.x() + font.width(value.substring(0, selectionStart())) - scrollOffset;
                int ex = area.x() + font.width(value.substring(0, selectionEnd())) - scrollOffset;
                g.fill(sx, textY - 1, ex, textY + 9, g.color(ColorToken.SELECTED));
            }
            g.text(value, area.x() - scrollOffset, textY, color, g.theme().textShadow());
        }
        if (focused && (Util.getMillis() / 500) % 2 == 0) {
            int cx = area.x() + font.width(value.substring(0, cursor)) - scrollOffset;
            if (cursor == value.length()) {
                g.text("_", cx, textY, color, false);
            } else {
                g.fill(cx, textY - 1, cx + 1, textY + 9, color);
            }
        }
        g.popScissor();
    }

    private int cursorFromMouse(double lx) {
        Font font = Minecraft.getInstance().font;
        int relative = (int) (lx - textArea().x() + scrollOffset);
        return font.plainSubstrByWidth(value, Math.max(0, relative)).length();
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (button != 0) return false;
        boolean wasFocused = isFocused();
        requestFocus();
        if (!wasFocused && selectAllOnFocus) {
            selectAll();
        } else {
            moveCursor(cursorFromMouse(lx), Screen.hasShiftDown());
        }
        return true;
    }

    @Override
    protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
        if (button != 0 || !isFocused()) return false;
        moveCursor(cursorFromMouse(lx), true);
        return true;
    }

    @Override
    protected boolean onMouseScrolled(double lx, double ly, double scrollX, double scrollY) {
        if (onScroll == null || scrollY == 0) return false;
        Optional<String> result = onScroll.apply(value, scrollY > 0);
        if (result.isEmpty()) return false;
        replaceText(result.get());
        return true;
    }

    @Override
    public void onFocusChanged(boolean focused) {
        if (focused) {
            if (selectAllOnFocus) selectAll();
        } else {
            selectionAnchor = cursor;
            if (commitOnFocusLoss && dirty) {
                commit();
            }
        }
    }

    protected boolean onEnter() {
        if (!isValid()) return true;
        commit();
        UiRoot root = root();
        if (root != null) root.clearFocus();
        return true;
    }

    @Override
    protected boolean onKeyPressed(int key, int scanCode, int modifiers) {
        if (!isFocused()) return false;
        if (key == InputConstants.KEY_ESCAPE) {
            UiRoot root = root();
            if (root != null) root.clearFocus();
            return true;
        }
        if (Screen.isSelectAll(key)) {
            selectAll();
            return true;
        }
        if (Screen.isCopy(key)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(selectedText());
            return true;
        }
        if (Screen.isPaste(key)) {
            insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
            return true;
        }
        if (Screen.isCut(key)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(selectedText());
            insertText("");
            return true;
        }
        boolean shift = Screen.hasShiftDown();
        boolean ctrl = Screen.hasControlDown();
        switch (key) {
            case InputConstants.KEY_BACKSPACE -> {
                if (ctrl) deleteWord(-1);
                else deleteChars(-1);
                return true;
            }
            case InputConstants.KEY_DELETE -> {
                if (ctrl) deleteWord(1);
                else deleteChars(1);
                return true;
            }
            case InputConstants.KEY_LEFT -> {
                moveCursor(ctrl ? wordPosition(-1) : cursor - 1, shift);
                return true;
            }
            case InputConstants.KEY_RIGHT -> {
                moveCursor(ctrl ? wordPosition(1) : cursor + 1, shift);
                return true;
            }
            case InputConstants.KEY_HOME -> {
                moveCursor(0, shift);
                return true;
            }
            case InputConstants.KEY_END -> {
                moveCursor(value.length(), shift);
                return true;
            }
            case InputConstants.KEY_RETURN, InputConstants.KEY_NUMPADENTER -> {
                if (Screen.hasShiftDown()) return false;
                return onEnter();
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    protected boolean onCharTyped(char character, int modifiers) {
        if (!isFocused() || !editable) return false;
        if (!StringUtil.isAllowedChatCharacter(character)) return false;
        insertText(Character.toString(character));
        return true;
    }
}

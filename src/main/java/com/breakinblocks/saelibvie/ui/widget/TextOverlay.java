package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.AcceptsShiftEnter;
import com.breakinblocks.saelibvie.ui.core.EditSession;
import com.breakinblocks.saelibvie.ui.core.LayerOptions;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class TextOverlay extends Panel implements AcceptsShiftEnter {
    public static final int BUTTON_SIZE = 16;
    public static final int MIN_WIDTH = 100;

    private final TextField field;
    @Nullable
    private final Component title;
    private final Button acceptButton;
    private final Button cancelButton;
    private boolean showButtons = true;
    @Nullable
    private EditSession session;
    @Nullable
    private Consumer<String> onAccept;
    private boolean editable = true;

    public TextOverlay(@Nullable Component title, TextField field, @Nullable String initialText) {
        this.title = title;
        this.field = field;
        chrome(Chrome.PANEL);
        padding(0);
        int textWidth = initialText == null ? 0 : TextUtil.font().width(initialText) + 86;
        int titleHeight = title == null ? 0 : 14;
        int width = Math.max(MIN_WIDTH, textWidth);
        int height = 16 + titleHeight;
        setSize(width, height);
        field.setBounds(new Rect(2, titleHeight + 1, width - 36, 14));
        if (initialText != null) {
            field.setValue(initialText);
        }
        field.selectAllFromStart();
        field.commitOnFocusLoss(false);
        field.onCommit(v -> accept());
        add(field);
        acceptButton = new Button(Component.literal("✓"), this::accept).enabledWhen(() -> editable && field.isValid())
                .tooltip(List.of(Component.translatable("gui.accept"), TextUtil.hotkey("Enter")));
        cancelButton = new Button(Component.literal("✗"), this::cancel)
                .tooltip(List.of(Component.translatable("gui.cancel"), TextUtil.hotkey("Esc")));
        acceptButton.setBounds(new Rect(width - 34, titleHeight, BUTTON_SIZE, BUTTON_SIZE));
        cancelButton.setBounds(new Rect(width - 17, titleHeight, BUTTON_SIZE, BUTTON_SIZE));
        add(acceptButton);
        add(cancelButton);
    }

    public TextOverlay showButtons(boolean show) {
        this.showButtons = show;
        acceptButton.setVisible(show);
        cancelButton.setVisible(show);
        int titleHeight = title == null ? 0 : 14;
        field.setBounds(new Rect(2, titleHeight + 1, show ? width() - 36 : width() - 4, 14));
        return this;
    }

    public TextOverlay editable(boolean value) {
        this.editable = value;
        field.editable(value);
        return this;
    }

    public TextOverlay onAccept(Consumer<String> consumer) {
        this.onAccept = consumer;
        return this;
    }

    public TextField field() {
        return field;
    }

    @Nullable
    public EditSession session() {
        return session;
    }

    public void open(UiRoot root, Widget anchor, int offsetX, int offsetY, EditSession session) {
        this.session = session;
        root.pushLayerAt(this, anchor, offsetX, offsetY, LayerOptions.modal().scrim(false).session(session));
        field.requestFocus();
        field.selectAllFromStart();
    }

    public void openAtMouse(UiRoot root, EditSession session) {
        this.session = session;
        root.pushLayerAtMouse(this, LayerOptions.modal().scrim(false).session(session));
        field.requestFocus();
        field.selectAllFromStart();
    }

    public void accept() {
        if (!editable || !field.isValid()) return;
        if (onAccept != null) onAccept.accept(field.value());
        if (session != null) session.accept();
        remove();
    }

    public void cancel() {
        if (session != null) session.cancel();
        remove();
    }

    private void remove() {
        UiRoot root = root();
        if (root != null) {
            root.removeLayer(this);
        }
    }

    @Override
    public void onShiftEnter() {
        accept();
    }

    @Override
    protected void paintBackground(UiGraphics g) {
        super.paintBackground(g);
        if (title != null) {
            g.text(title, 3, 3, g.color(ColorToken.TEXT_TITLE));
        }
    }

    @Override
    protected boolean onKeyPressed(int key, int scanCode, int modifiers) {
        if (key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER) {
            accept();
            return true;
        }
        return false;
    }

    @Override
    protected boolean onMouseScrolled(double lx, double ly, double scrollX, double scrollY) {
        return field.mouseScrolled(lx - field.x(), ly - field.y(), scrollX, scrollY);
    }
}

package com.breakinblocks.saelibvie.ui.screen;

import com.breakinblocks.saelibvie.ui.color.Theme;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.widget.FormWindow;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public abstract class FormScreen extends SaeScreen {
    protected FormWindow form;

    protected FormScreen(Component title) {
        super(title);
    }

    protected FormScreen(Component title, @Nullable Screen parent) {
        super(title, parent);
    }

    protected FormScreen(Component title, @Nullable Screen parent, Theme theme) {
        super(title, parent, theme);
    }

    protected abstract void buildForm(FormWindow form);

    protected Rect formBounds() {
        int w = Math.min(width - 20, Math.round(width * 0.75f));
        int h = Math.min(height - 20, Math.round(height * 0.9f));
        return new Rect((width - w) / 2, (height - h) / 2, w, h);
    }

    @Override
    protected void build(UiRoot root) {
        form = new FormWindow(getTitle());
        form.onAccept(this::onAccept);
        form.onCancel(this::onCancel);
        form.setBounds(formBounds());
        root.add(form);
        buildForm(form);
    }

    public FormWindow form() {
        return form;
    }

    protected void onAccept() {
        close();
    }

    protected void onCancel() {
        close();
    }

    @Override
    protected void onEscape() {
        onCancel();
    }

    @Override
    protected boolean closesOnInventoryKey() {
        return root.focused() == null;
    }
}

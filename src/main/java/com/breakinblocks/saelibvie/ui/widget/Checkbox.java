package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import com.breakinblocks.saelibvie.ui.util.UiSounds;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Checkbox extends Widget {
    private Supplier<Component> label;
    private BooleanSupplier checked;
    @Nullable
    private Consumer<Boolean> onChange;
    private boolean localValue;
    private int boxSize = 10;

    public Checkbox(Component label) {
        this.label = () -> label;
        this.checked = () -> localValue;
        focusable(true);
    }

    public Checkbox(Component label, boolean initial, Consumer<Boolean> onChange) {
        this(label);
        this.localValue = initial;
        this.onChange = onChange;
    }

    public Checkbox bind(BooleanSupplier checked, Consumer<Boolean> onChange) {
        this.checked = checked;
        this.onChange = onChange;
        return this;
    }

    public Checkbox onChange(Consumer<Boolean> onChange) {
        this.onChange = onChange;
        return this;
    }

    public Checkbox label(Supplier<Component> label) {
        this.label = label;
        return this;
    }

    public Checkbox boxSize(int size) {
        this.boxSize = size;
        return this;
    }

    public boolean isChecked() {
        return checked.getAsBoolean();
    }

    public void setChecked(boolean value) {
        localValue = value;
    }

    public Checkbox radio(boolean radio) {
        this.radio = radio;
        return this;
    }

    public boolean isRadio() {
        return radio;
    }

    private boolean radio;

    public void toggle() {
        if (radio && isChecked()) return;
        boolean next = !isChecked();
        localValue = next;
        UiSounds.click();
        if (onChange != null) onChange.accept(next);
    }

    @Override
    protected Size measure() {
        int w = width() > 0 ? width() : boxSize + 4 + TextUtil.font().width(label.get());
        int h = height() > 0 ? height() : Math.max(boxSize, TextUtil.font().lineHeight);
        return new Size(w, h);
    }

    @Override
    protected void paint(UiGraphics g) {
        Rect box = new Rect(0, (height() - boxSize) / 2, boxSize, boxSize);
        Painter.checkbox(g, box, isChecked(), isHovered(), isEnabled());
        if (isFocused()) {
            Painter.focusRing(g, box);
        }
        Component text = label.get();
        int color = isEnabled() ? g.color(isChecked() ? ColorToken.TEXT : ColorToken.TEXT_DIM) : g.color(ColorToken.TEXT_DISABLED);
        g.text(text, boxSize + 4, (height() - 8) / 2, color);
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (button != 0) return false;
        requestFocus();
        toggle();
        return true;
    }

    @Override
    protected boolean onKeyPressed(int key, int scanCode, int modifiers) {
        if (key == InputConstants.KEY_SPACE || key == InputConstants.KEY_RETURN) {
            toggle();
            return true;
        }
        return false;
    }
}

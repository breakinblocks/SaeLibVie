package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.color.Skin;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;

public class Slider extends Widget {
    private double min;
    private double max;
    private double step;
    private DoubleSupplier value;
    private double localValue;
    @Nullable
    private DoubleConsumer onChange;
    @Nullable
    private DoubleConsumer onRelease;
    @Nullable
    private DoubleFunction<Component> formatter;
    @Nullable
    private Component label;
    private boolean dragging;
    private int handleWidth = 8;

    public Slider(double min, double max) {
        this.min = min;
        this.max = max;
        this.localValue = min;
        this.value = () -> localValue;
        focusable(true);
    }

    public Slider(double min, double max, double initial, DoubleConsumer onChange) {
        this(min, max);
        this.localValue = Mth.clamp(initial, min, max);
        this.onChange = onChange;
    }

    public Slider bind(DoubleSupplier value, DoubleConsumer onChange) {
        this.value = value;
        this.onChange = onChange;
        return this;
    }

    public Slider step(double step) {
        this.step = step;
        return this;
    }

    public Slider range(double min, double max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public Slider onChange(DoubleConsumer onChange) {
        this.onChange = onChange;
        return this;
    }

    public Slider onRelease(DoubleConsumer onRelease) {
        this.onRelease = onRelease;
        return this;
    }

    public Slider formatter(DoubleFunction<Component> formatter) {
        this.formatter = formatter;
        return this;
    }

    public Slider label(Component label) {
        this.label = label;
        return this;
    }

    public Slider handleWidth(int width) {
        this.handleWidth = width;
        return this;
    }

    public double value() {
        return Mth.clamp(value.getAsDouble(), min, max);
    }

    public void setValue(double newValue) {
        double snapped = snap(Mth.clamp(newValue, min, max));
        if (snapped == value()) return;
        localValue = snapped;
        if (onChange != null) onChange.accept(snapped);
    }

    private double snap(double v) {
        if (step <= 0) return v;
        return min + Math.round((v - min) / step) * step;
    }

    private double fraction() {
        double span = max - min;
        return span <= 0 ? 0 : (value() - min) / span;
    }

    @Override
    protected Size measure() {
        return new Size(width() > 0 ? width() : 100, height() > 0 ? height() : 14);
    }

    private Component displayText() {
        double v = value();
        Component valueText = formatter != null ? formatter.apply(v) : Component.literal(step >= 1 || (step == 0 && Math.rint(v) == v) ? Long.toString(Math.round(v)) : String.format(Locale.ROOT, "%.2f", v));
        if (label == null) return valueText;
        return Component.empty().append(label).append(": ").append(valueText);
    }

    @Override
    protected void paint(UiGraphics g) {
        Rect r = localRect();
        var skin = g.theme().skin();
        boolean skinned = skin != null && skin.has(Skin.SLIDER_TRACK) && skin.has(Skin.SLIDER_HANDLE);
        if (skinned) {
            g.sprite(skin.sprite(Skin.SLIDER_TRACK), r);
        } else {
            Painter.inset(g, r);
        }
        int travel = r.w() - handleWidth;
        int handleX = r.x() + (int) Math.round(travel * fraction());
        Rect handle = new Rect(handleX, r.y(), handleWidth, r.h());
        if (!skinned) {
            Rect fill = new Rect(r.x() + 1, r.y() + 1, Math.max(0, handleX - r.x()), r.h() - 2);
            g.fill(fill, g.color(ColorToken.PROGRESS_FILL));
        }
        if (skinned) {
            boolean hot = isHovered() || dragging || isFocused();
            g.sprite(skin.sprite(hot && skin.has(Skin.SLIDER_HANDLE_HOVER) ? Skin.SLIDER_HANDLE_HOVER : Skin.SLIDER_HANDLE), handle);
        } else {
            Painter.button(g, handle, new Painter.ButtonState(isEnabled(), isHovered() || dragging, dragging, false, isFocused()));
        }
        int color = isEnabled() ? g.color(ColorToken.TEXT) : g.color(ColorToken.TEXT_DISABLED);
        g.centeredText(displayText(), r.centerX(), r.y() + (r.h() - 8) / 2, color, true);
    }

    private void setFromMouse(double lx) {
        double travel = width() - handleWidth;
        if (travel <= 0) return;
        double f = Mth.clamp((lx - handleWidth / 2.0) / travel, 0.0, 1.0);
        setValue(min + f * (max - min));
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (button != 0) return false;
        requestFocus();
        dragging = true;
        setFromMouse(lx);
        return true;
    }

    @Override
    protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
        if (!dragging) return false;
        setFromMouse(lx);
        return true;
    }

    @Override
    protected boolean onMouseReleased(double lx, double ly, int button) {
        if (!dragging) return false;
        dragging = false;
        if (onRelease != null) onRelease.accept(value());
        return true;
    }

    @Override
    protected boolean onMouseScrolled(double lx, double ly, double scrollX, double scrollY) {
        double delta = step > 0 ? step : (max - min) / 100.0;
        setValue(value() + Math.signum(scrollY) * delta);
        return true;
    }

    @Override
    protected boolean onKeyPressed(int key, int scanCode, int modifiers) {
        double delta = step > 0 ? step : (max - min) / 100.0;
        if (key == InputConstants.KEY_LEFT) {
            setValue(value() - delta);
            return true;
        }
        if (key == InputConstants.KEY_RIGHT) {
            setValue(value() + delta);
            return true;
        }
        return false;
    }
}

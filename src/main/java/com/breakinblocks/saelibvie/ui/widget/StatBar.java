package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class StatBar extends Widget {
    private final Supplier<Float> fraction;
    private IntSupplier color;
    @Nullable
    private Supplier<Component> label;
    private int barHeight = 5;
    private boolean labelBelow = true;

    public StatBar(Supplier<Float> fraction, int color) {
        this.fraction = fraction;
        this.color = () -> color;
    }

    public StatBar(Supplier<Float> fraction, IntSupplier color) {
        this.fraction = fraction;
        this.color = color;
    }

    public static StatBar of(Supplier<Float> current, Supplier<Float> max, int color) {
        return new StatBar(() -> {
            float m = max.get();
            return m <= 0f ? 0f : current.get() / m;
        }, color);
    }

    public StatBar label(Component label) {
        this.label = () -> label;
        return this;
    }

    public StatBar label(Supplier<Component> label) {
        this.label = label;
        return this;
    }

    public StatBar barHeight(int height) {
        this.barHeight = height;
        return this;
    }

    public StatBar labelAbove() {
        this.labelBelow = false;
        return this;
    }

    @Override
    protected Size measure() {
        int h = barHeight + (label != null ? 10 : 0);
        return new Size(width() > 0 ? width() : 100, height() > 0 ? height() : h);
    }

    @Override
    protected void paint(UiGraphics g) {
        Component text = label == null ? null : label.get();
        int barY = 0;
        int labelY = barHeight + 2;
        if (text != null && !labelBelow) {
            barY = 10;
            labelY = 0;
        }
        Rect bar = new Rect(0, barY, width(), barHeight);
        Painter.statBar(g, bar, Mth.clamp(fraction.get(), 0f, 1f), color.getAsInt());
        if (text != null) {
            g.text(text, 0, labelY, g.color(ColorToken.TEXT_DIM));
        }
    }
}

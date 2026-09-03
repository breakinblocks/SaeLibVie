package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Align;
import com.breakinblocks.saelibvie.ui.geom.Axis;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class TextBlock extends Widget {
    private Supplier<Component> text;
    @Nullable
    private IntSupplier color;
    private ColorToken colorToken = ColorToken.TEXT;
    private Align horizontal = Align.START;
    private int lineSpacing;
    @Nullable
    private Boolean shadow;
    private int maxLines = Integer.MAX_VALUE;

    public TextBlock(Component text) {
        this.text = () -> text;
    }

    public TextBlock(Supplier<Component> text) {
        this.text = text;
    }

    public TextBlock text(Component text) {
        this.text = () -> text;
        return this;
    }

    public TextBlock text(Supplier<Component> text) {
        this.text = text;
        return this;
    }

    public TextBlock color(int argb) {
        this.color = () -> argb;
        return this;
    }

    public TextBlock color(IntSupplier argb) {
        this.color = argb;
        return this;
    }

    public TextBlock colorToken(ColorToken token) {
        this.colorToken = token;
        this.color = null;
        return this;
    }

    public TextBlock align(Align horizontal) {
        this.horizontal = horizontal;
        return this;
    }

    public TextBlock lineSpacing(int spacing) {
        this.lineSpacing = spacing;
        return this;
    }

    public TextBlock shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public TextBlock maxLines(int lines) {
        this.maxLines = Math.max(1, lines);
        return this;
    }

    public int lineHeight() {
        return TextUtil.font().lineHeight + lineSpacing;
    }

    public int measureHeight(int width) {
        List<FormattedCharSequence> lines = TextUtil.font().split(text.get(), Math.max(1, width));
        int count = Math.min(lines.size(), maxLines);
        return count * lineHeight();
    }

    @Override
    public int measureAlong(Axis axis, int crossSize) {
        return axis == Axis.VERTICAL && crossSize > 0 ? measureHeight(crossSize) : 0;
    }

    @Override
    protected Size measure() {
        int w = width() > 0 ? width() : 100;
        return new Size(w, measureHeight(w));
    }

    @Override
    protected void paint(UiGraphics g) {
        Component component = text.get();
        if (component == null) return;
        int resolved = !isEnabled() ? g.color(ColorToken.TEXT_DISABLED) : color != null ? color.getAsInt() : g.color(colorToken);
        boolean useShadow = shadow != null ? shadow : g.theme().textShadow();
        List<FormattedCharSequence> lines = g.wrap(component, Math.max(1, width()));
        int y = 0;
        int count = 0;
        for (FormattedCharSequence line : lines) {
            if (count++ >= maxLines) break;
            int lineWidth = g.font().width(line);
            int x = horizontal.offset(width(), lineWidth);
            g.text(line, x, y, resolved, useShadow);
            y += lineHeight();
        }
    }
}

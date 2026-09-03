package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Align;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class Label extends Widget {
    private Supplier<Component> text;
    @Nullable
    private IntSupplier color;
    private ColorToken colorToken = ColorToken.TEXT;
    private Align horizontal = Align.START;
    private Align vertical = Align.START;
    @Nullable
    private Boolean shadow;
    private boolean fitToWidth;
    private boolean shrinkToFit;
    private float scale = 1f;
    private boolean header;

    public Label(Component text) {
        this.text = () -> text;
    }

    public Label(Supplier<Component> text) {
        this.text = text;
    }

    public static Label of(String literal) {
        return new Label(Component.literal(literal));
    }

    public Label text(Component text) {
        this.text = () -> text;
        return this;
    }

    public Label text(Supplier<Component> text) {
        this.text = text;
        return this;
    }

    public Component currentText() {
        return text.get();
    }

    public Label color(int argb) {
        this.color = () -> argb;
        return this;
    }

    public Label color(IntSupplier argb) {
        this.color = argb;
        return this;
    }

    public Label colorToken(ColorToken token) {
        this.colorToken = token;
        this.color = null;
        return this;
    }

    public Label dim() {
        return colorToken(ColorToken.TEXT_DIM);
    }

    public Label title() {
        return colorToken(ColorToken.TEXT_TITLE);
    }

    public Label align(Align horizontal) {
        this.horizontal = horizontal;
        return this;
    }

    public Label align(Align horizontal, Align vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
        return this;
    }

    public Label centered() {
        return align(Align.CENTER, Align.CENTER);
    }

    public Label shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public Label fitToWidth(boolean fit) {
        this.fitToWidth = fit;
        return this;
    }

    public Label shrinkToFit(boolean shrink) {
        this.shrinkToFit = shrink;
        return this;
    }

    public Label scale(float scale) {
        this.scale = Math.max(0.1f, scale);
        return this;
    }

    public Label header(boolean header) {
        this.header = header;
        return this;
    }

    @Override
    protected Size measure() {
        Component component = text.get();
        var font = TextUtil.font();
        int w = Math.round(font.width(component) * scale);
        int h = Math.round(font.lineHeight * scale);
        if (width() > 0 && height() > 0) {
            return new Size(width(), height());
        }
        return new Size(w, Math.max(h, height()));
    }

    protected int resolveColor(UiGraphics g) {
        if (!isEnabled()) return g.color(ColorToken.TEXT_DISABLED);
        return color != null ? color.getAsInt() : g.color(colorToken);
    }

    @Override
    protected void paint(UiGraphics g) {
        Component component = text.get();
        if (component == null) return;
        boolean useShadow = shadow != null ? shadow : g.theme().textShadow();
        int color = resolveColor(g);
        String rendered = header ? g.headerText(component, Integer.MAX_VALUE) : null;
        var font = g.font();
        int available = width() > 0 ? width() : Integer.MAX_VALUE;
        float effectiveScale = scale;
        if (shrinkToFit && available != Integer.MAX_VALUE) {
            int textWidth = rendered != null ? font.width(rendered) : font.width(component);
            effectiveScale = Math.min(scale, TextUtil.fitScale(font, rendered != null ? rendered : component.getString(), available) * scale);
        }
        if (fitToWidth && available != Integer.MAX_VALUE) {
            rendered = g.fit(rendered != null ? rendered : component.getString(), Math.round(available / effectiveScale));
        }
        int textWidth = rendered != null ? font.width(rendered) : font.width(component);
        int textHeight = font.lineHeight - 1;
        int drawW = Math.round(textWidth * effectiveScale);
        int drawH = Math.round(textHeight * effectiveScale);
        int x = horizontal.offset(width() > 0 ? width() : drawW, drawW);
        int y = vertical.offset(height() > 0 ? height() : drawH, drawH);
        if (effectiveScale != 1f) {
            g.pushTransform(x, y, effectiveScale);
            if (rendered != null) {
                g.text(rendered, 0, 0, color, useShadow);
            } else {
                g.text(component, 0, 0, color, useShadow);
            }
            g.popTransform();
        } else if (rendered != null) {
            g.text(rendered, x, y, color, useShadow);
        } else {
            g.text(component, x, y, color, useShadow);
        }
    }
}

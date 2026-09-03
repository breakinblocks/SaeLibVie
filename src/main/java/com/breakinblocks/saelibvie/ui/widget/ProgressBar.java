package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Axis;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class ProgressBar extends Widget {
    private final Supplier<Float> progress;
    private Axis axis = Axis.HORIZONTAL;
    private boolean reverse;
    @Nullable
    private ResourceLocation texture;
    private int textureU;
    private int textureV;
    private int fillU;
    private int fillV;
    private int textureW = 256;
    private int textureH = 256;
    @Nullable
    private BooleanSupplier blocked;
    private int blockedFill = 0x55FF3030;
    private int blockedBorder = 0xAAFF3030;
    private boolean drawBackground = true;

    public ProgressBar(Supplier<Float> progress) {
        this.progress = progress;
    }

    public ProgressBar(Rect bounds, Supplier<Float> progress) {
        super(bounds);
        this.progress = progress;
    }

    public ProgressBar axis(Axis axis) {
        this.axis = axis;
        return this;
    }

    public ProgressBar vertical() {
        return axis(Axis.VERTICAL);
    }

    public ProgressBar reverse(boolean reverse) {
        this.reverse = reverse;
        return this;
    }

    public ProgressBar textured(ResourceLocation texture, int backgroundU, int backgroundV, int fillU, int fillV) {
        this.texture = texture;
        this.textureU = backgroundU;
        this.textureV = backgroundV;
        this.fillU = fillU;
        this.fillV = fillV;
        return this;
    }

    public ProgressBar textureSize(int w, int h) {
        this.textureW = w;
        this.textureH = h;
        return this;
    }

    public ProgressBar background(boolean draw) {
        this.drawBackground = draw;
        return this;
    }

    public ProgressBar blockedWhen(BooleanSupplier blocked) {
        this.blocked = blocked;
        return this;
    }

    public ProgressBar blockedColors(int fill, int border) {
        this.blockedFill = fill;
        this.blockedBorder = border;
        return this;
    }

    public float fraction() {
        Float value = progress.get();
        return value == null ? 0f : Mth.clamp(value, 0f, 1f);
    }

    @Override
    protected void paint(UiGraphics g) {
        Rect r = localRect();
        float fraction = fraction();
        if (texture != null) {
            paintTextured(g, r, fraction);
        } else if (drawBackground) {
            Painter.progress(g, r, fraction, axis, reverse);
        } else {
            paintFillOnly(g, r, fraction);
        }
        if (blocked != null && blocked.getAsBoolean()) {
            g.fill(r, blockedFill);
            g.outline(r, blockedBorder);
        }
    }

    private void paintFillOnly(UiGraphics g, Rect r, float fraction) {
        Rect fill;
        if (axis == Axis.HORIZONTAL) {
            int w = Math.round(r.w() * fraction);
            fill = reverse ? r.rightPart(w) : r.leftPart(w);
        } else {
            int h = Math.round(r.h() * fraction);
            fill = reverse ? r.topPart(h) : r.bottomPart(h);
        }
        g.fill(fill, g.color(ColorToken.PROGRESS_FILL));
    }

    private void paintTextured(UiGraphics g, Rect r, float fraction) {
        if (drawBackground) {
            g.blit(texture, r.x(), r.y(), textureU, textureV, r.w(), r.h(), textureW, textureH);
        }
        if (axis == Axis.HORIZONTAL) {
            int w = Math.round(r.w() * fraction);
            if (w <= 0) return;
            if (reverse) {
                g.blit(texture, r.right() - w, r.y(), fillU + (r.w() - w), fillV, w, r.h(), textureW, textureH);
            } else {
                g.blit(texture, r.x(), r.y(), fillU, fillV, w, r.h(), textureW, textureH);
            }
        } else {
            int h = Math.round(r.h() * fraction);
            if (h <= 0) return;
            if (reverse) {
                g.blit(texture, r.x(), r.y(), fillU, fillV, r.w(), h, textureW, textureH);
            } else {
                g.blit(texture, r.x(), r.bottom() - h, fillU, fillV + (r.h() - h), r.w(), h, textureW, textureH);
            }
        }
    }
}

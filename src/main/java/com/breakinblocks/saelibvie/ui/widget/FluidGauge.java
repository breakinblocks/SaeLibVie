package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.item.FluidTextures;
import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.PositionedIngredient;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Axis;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class FluidGauge extends Widget {
    private final Supplier<FluidStack> fluid;
    private final IntSupplier capacity;
    private Axis axis = Axis.VERTICAL;
    private boolean graduations = true;
    private boolean drawBackground = true;
    private boolean autoTooltip = true;

    public FluidGauge(Supplier<FluidStack> fluid, int capacity) {
        this(fluid, () -> capacity);
    }

    public FluidGauge(Supplier<FluidStack> fluid, IntSupplier capacity) {
        this.fluid = fluid;
        this.capacity = capacity;
    }

    public FluidGauge(Rect bounds, Supplier<FluidStack> fluid, int capacity) {
        this(fluid, capacity);
        bounds(bounds);
    }

    public FluidGauge axis(Axis axis) {
        this.axis = axis;
        return this;
    }

    public FluidGauge graduations(boolean show) {
        this.graduations = show;
        return this;
    }

    public FluidGauge background(boolean draw) {
        this.drawBackground = draw;
        return this;
    }

    public FluidGauge autoTooltip(boolean auto) {
        this.autoTooltip = auto;
        return this;
    }

    public static void drawFluid(UiGraphics g, FluidStack stack, int x, int y, int w, int h) {
        if (stack.isEmpty() || w <= 0 || h <= 0) return;
        TextureAtlasSprite sprite = FluidTextures.stillSprite(stack);
        if (sprite == null) return;
        g.tiledAtlasSprite(sprite, x, y, w, h, FluidTextures.color(stack));
    }

    @Override
    protected void paint(UiGraphics g) {
        Rect r = localRect();
        if (drawBackground) {
            Painter.inset(g, r);
        }
        Rect inner = drawBackground ? r.inset(1) : r;
        FluidStack stack = fluid.get();
        int cap = Math.max(1, capacity.getAsInt());
        if (!stack.isEmpty()) {
            if (axis == Axis.VERTICAL) {
                int filled = (int) ((long) stack.getAmount() * inner.h() / cap);
                filled = Math.max(1, Math.min(filled, inner.h()));
                drawFluid(g, stack, inner.x(), inner.bottom() - filled, inner.w(), filled);
            } else {
                int filled = (int) ((long) stack.getAmount() * inner.w() / cap);
                filled = Math.max(1, Math.min(filled, inner.w()));
                drawFluid(g, stack, inner.x(), inner.y(), filled, inner.h());
            }
        }
        if (graduations) {
            int color = g.color(ColorToken.TEXT_DIM);
            for (int quarter = 1; quarter <= 3; quarter++) {
                if (axis == Axis.VERTICAL) {
                    int y = inner.y() + inner.h() * quarter / 4;
                    g.fill(inner.x(), y, inner.x() + 3, y + 1, color);
                } else {
                    int x = inner.x() + inner.w() * quarter / 4;
                    g.fill(x, inner.y(), x + 1, inner.y() + 3, color);
                }
            }
        }
        if (autoTooltip && isHovered() && !hasTooltip()) {
            g.tooltip(tooltipLines());
        }
    }

    @Override
    public Optional<PositionedIngredient> ingredientUnderMouse() {
        FluidStack current = fluid.get();
        if (current.isEmpty()) return Optional.empty();
        return PositionedIngredient.optional(current, this);
    }

    public List<Component> tooltipLines() {
        FluidStack stack = fluid.get();
        List<Component> lines = new ArrayList<>();
        if (stack.isEmpty()) {
            lines.add(Component.translatable("gui.saelibvie.fluid.empty"));
        } else {
            lines.add(stack.getHoverName());
        }
        lines.add(Component.translatable("gui.saelibvie.fluid.amount", TextUtil.grouped(stack.getAmount()), TextUtil.grouped(capacity.getAsInt())));
        return lines;
    }
}

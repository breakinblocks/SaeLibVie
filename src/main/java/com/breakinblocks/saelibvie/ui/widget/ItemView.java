package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.core.PositionedIngredient;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemView extends Widget {
    private final Supplier<ItemStack> stack;
    private boolean decorations = true;
    private boolean slotBackground;
    private boolean autoTooltip = true;
    private boolean hoverHighlight;
    @Nullable
    private Consumer<Integer> onClick;
    @Nullable
    private Supplier<String> countText;

    public ItemView(ItemStack stack) {
        this(() -> stack);
    }

    public ItemView(Supplier<ItemStack> stack) {
        this.stack = stack;
        size(16, 16);
    }

    public ItemView decorations(boolean draw) {
        this.decorations = draw;
        return this;
    }

    public ItemView slotBackground(boolean draw) {
        this.slotBackground = draw;
        if (draw) size(18, 18);
        return this;
    }

    public ItemView autoTooltip(boolean auto) {
        this.autoTooltip = auto;
        return this;
    }

    public ItemView hoverHighlight(boolean highlight) {
        this.hoverHighlight = highlight;
        return this;
    }

    public ItemView countText(Supplier<String> text) {
        this.countText = text;
        return this;
    }

    public ItemView onClick(Consumer<Integer> action) {
        this.onClick = action;
        return this;
    }

    public ItemStack stack() {
        return stack.get();
    }

    @Override
    public Optional<PositionedIngredient> ingredientUnderMouse() {
        ItemStack current = stack.get();
        if (current.isEmpty()) return Optional.empty();
        return PositionedIngredient.optional(current, this);
    }

    @Override
    protected Size measure() {
        return new Size(width() > 0 ? width() : 16, height() > 0 ? height() : 16);
    }

    @Override
    protected void paint(UiGraphics g) {
        int inset = slotBackground ? 1 : 0;
        if (slotBackground) {
            Painter.slot(g, 0, 0, Math.min(width(), height()));
        }
        ItemStack current = stack.get();
        int ix = inset + (width() - inset * 2 - 16) / 2;
        int iy = inset + (height() - inset * 2 - 16) / 2;
        if (!current.isEmpty()) {
            g.pushTranslate(ix, iy);
            if (decorations) {
                g.itemWithDecorations(current, 0, 0, countText == null ? null : countText.get());
            } else {
                g.item(current, 0, 0);
            }
            g.popTransform();
        }
        if (hoverHighlight && isHovered()) {
            Painter.hoverTint(g, localRect().inset(inset));
        }
        if (autoTooltip && isHovered() && !current.isEmpty() && !hasTooltip()) {
            g.itemTooltip(current);
        }
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (onClick == null) return false;
        onClick.accept(button);
        return true;
    }
}

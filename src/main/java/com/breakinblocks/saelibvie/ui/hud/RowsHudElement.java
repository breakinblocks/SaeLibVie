package com.breakinblocks.saelibvie.ui.hud;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class RowsHudElement<T> extends HudElement {
    public static final int LINE_HEIGHT = 11;

    private final Function<Minecraft, @Nullable T> target;
    private final List<Function<T, Component>> rows = new ArrayList<>();
    private boolean shadow = true;
    private boolean backdrop;
    private int textColor = 0xFFFFFFFF;

    public RowsHudElement(int width, int lines, Function<Minecraft, @Nullable T> target) {
        super(width, LINE_HEIGHT * lines);
        this.target = target;
    }

    public static <B extends BlockEntity> Function<Minecraft, @Nullable B> lookedAtBlockEntity(Class<B> type) {
        return mc -> {
            HitResult hit = mc.hitResult;
            if (hit == null || hit.getType() != HitResult.Type.BLOCK || mc.level == null) return null;
            BlockEntity be = mc.level.getBlockEntity(((BlockHitResult) hit).getBlockPos());
            return type.isInstance(be) ? type.cast(be) : null;
        };
    }

    public RowsHudElement<T> rows(Consumer<Consumer<Function<T, Component>>> gatherer) {
        gatherer.accept(rows::add);
        setSize(width(), LINE_HEIGHT * Math.max(1, rows.size()));
        return this;
    }

    public RowsHudElement<T> row(Function<T, Component> row) {
        rows.add(row);
        setSize(width(), LINE_HEIGHT * rows.size());
        return this;
    }

    public RowsHudElement<T> shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public RowsHudElement<T> backdrop(boolean backdrop) {
        this.backdrop = backdrop;
        return this;
    }

    public RowsHudElement<T> textColor(int argb) {
        this.textColor = argb;
        return this;
    }

    @Nullable
    protected T currentTarget(Minecraft mc) {
        return target.apply(mc);
    }

    @Override
    public boolean shouldRender(Minecraft minecraft) {
        return currentTarget(minecraft) != null;
    }

    @Override
    protected void paintBackground(UiGraphics g) {
        super.paintBackground(g);
        if (backdrop) {
            g.fill(localRect(), g.color(ColorToken.OVERLAY_DIM));
        }
        Minecraft mc = Minecraft.getInstance();
        T current = currentTarget(mc);
        int y = 1;
        for (Function<T, Component> row : rows) {
            Component text = current != null ? row.apply(current) : Component.literal("?");
            g.text(text, backdrop ? 2 : 0, y, textColor, shadow);
            y += LINE_HEIGHT;
        }
    }
}

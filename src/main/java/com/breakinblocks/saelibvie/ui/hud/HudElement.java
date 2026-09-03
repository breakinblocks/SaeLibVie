package com.breakinblocks.saelibvie.ui.hud;

import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.geom.Anchor;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public abstract class HudElement extends Panel {
    @Nullable
    private ResourceLocation key;
    private Anchor origin = Anchor.TOP_LEFT;

    protected HudElement(int width, int height) {
        size(width, height);
    }

    @Nullable
    public ResourceLocation key() {
        return key;
    }

    void setKey(ResourceLocation key) {
        this.key = key;
    }

    public Anchor origin() {
        return origin;
    }

    public HudElement origin(Anchor origin) {
        this.origin = origin;
        return this;
    }

    public float hudScale() {
        return hudScale;
    }

    public void setHudScale(float scale) {
        this.hudScale = Math.max(0.1f, scale);
    }

    public int scaledWidth() {
        return Math.round(width() * hudScale);
    }

    public int scaledHeight() {
        return Math.round(height() * hudScale);
    }

    @Override
    public boolean contains(double lx, double ly) {
        return lx >= 0 && ly >= 0 && lx < scaledWidth() && ly < scaledHeight();
    }

    @Override
    protected void paint(UiGraphics g) {
        if (hudScale != 1f) {
            g.pushTransform(0, 0, hudScale);
            super.paint(g);
            g.popTransform();
            return;
        }
        super.paint(g);
    }

    public boolean shouldRender(Minecraft minecraft) {
        return true;
    }

    public void onEditModeChanged(boolean editing) {
    }

    private float hudScale = 1f;
}

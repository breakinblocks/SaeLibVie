package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Image extends Widget {
    private Supplier<ResourceLocation> texture;
    private boolean sprite;
    private int u;
    private int v;
    private int regionW = -1;
    private int regionH = -1;
    private int textureW = 256;
    private int textureH = 256;
    @Nullable
    private Supplier<Integer> tint;

    private Image(Supplier<ResourceLocation> texture, boolean sprite) {
        this.texture = texture;
        this.sprite = sprite;
    }

    public static Image sprite(ResourceLocation sprite) {
        return new Image(() -> sprite, true);
    }

    public static Image sprite(Supplier<ResourceLocation> sprite) {
        return new Image(sprite, true);
    }

    public static Image texture(ResourceLocation texture, int u, int v, int w, int h) {
        Image image = new Image(() -> texture, false);
        image.u = u;
        image.v = v;
        image.regionW = w;
        image.regionH = h;
        image.size(w, h);
        return image;
    }

    public static Image texture(ResourceLocation texture, int u, int v, int w, int h, int textureW, int textureH) {
        Image image = texture(texture, u, v, w, h);
        image.textureW = textureW;
        image.textureH = textureH;
        return image;
    }

    public Image region(int u, int v, int w, int h) {
        this.u = u;
        this.v = v;
        this.regionW = w;
        this.regionH = h;
        return this;
    }

    public Image textureSize(int w, int h) {
        this.textureW = w;
        this.textureH = h;
        return this;
    }

    public Image tint(int argb) {
        this.tint = () -> argb;
        return this;
    }

    @Override
    protected Size measure() {
        if (width() > 0 && height() > 0) return new Size(width(), height());
        if (regionW > 0 && regionH > 0) return new Size(regionW, regionH);
        return new Size(16, 16);
    }

    @Override
    protected void paint(UiGraphics g) {
        ResourceLocation location = texture.get();
        if (location == null) return;
        if (sprite) {
            g.sprite(location, 0, 0, width(), height());
            return;
        }
        int rw = regionW > 0 ? regionW : width();
        int rh = regionH > 0 ? regionH : height();
        if (rw == width() && rh == height()) {
            g.blit(location, 0, 0, u, v, rw, rh, textureW, textureH);
        } else {
            g.blitScaled(location, 0, 0, width(), height(), u, v, rw, rh, textureW, textureH);
        }
    }
}

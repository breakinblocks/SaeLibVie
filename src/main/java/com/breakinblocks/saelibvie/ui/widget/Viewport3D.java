package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Quaternionf;

import java.util.function.Supplier;

public class Viewport3D extends Widget {
    @FunctionalInterface
    public interface Renderer {
        void render(PoseStack pose, MultiBufferSource.BufferSource buffers, float partialTick);
    }

    public enum LightMode {
        ITEMS,
        ENTITY
    }

    private Renderer renderer;
    private float yaw = 225f;
    private float pitch = 30f;
    private float zoom = 1f;
    private float minZoom = 0.25f;
    private float maxZoom = 4f;
    private float baseScale = 16f;
    private boolean orbit = true;
    private boolean wheelZoom = true;
    private boolean drawBackground = true;
    private LightMode lightMode = LightMode.ITEMS;
    private float centerX;
    private float centerY;
    private float centerZ;
    private boolean dragging;
    private float autoSpin;

    public Viewport3D(Renderer renderer) {
        this.renderer = renderer;
    }

    public Viewport3D(Rect bounds, Renderer renderer) {
        super(bounds);
        this.renderer = renderer;
    }

    public static Viewport3D ofBlock(Supplier<BlockState> state) {
        return new Viewport3D((pose, buffers, partial) -> {
            BlockState current = state.get();
            if (current == null || current.isAir()) return;
            pose.pushPose();
            pose.translate(-0.5f, -0.5f, -0.5f);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(current, pose, buffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY, null);
            pose.popPose();
        });
    }

    public Viewport3D renderer(Renderer renderer) {
        this.renderer = renderer;
        return this;
    }

    public Viewport3D angles(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        return this;
    }

    public Viewport3D zoom(float zoom) {
        this.zoom = Mth.clamp(zoom, minZoom, maxZoom);
        return this;
    }

    public Viewport3D zoomRange(float min, float max) {
        this.minZoom = min;
        this.maxZoom = max;
        return this;
    }

    public Viewport3D baseScale(float pixelsPerUnit) {
        this.baseScale = pixelsPerUnit;
        return this;
    }

    public Viewport3D orbit(boolean orbit) {
        this.orbit = orbit;
        return this;
    }

    public Viewport3D wheelZoom(boolean zoom) {
        this.wheelZoom = zoom;
        return this;
    }

    public Viewport3D background(boolean draw) {
        this.drawBackground = draw;
        return this;
    }

    public Viewport3D lighting(LightMode mode) {
        this.lightMode = mode;
        return this;
    }

    public Viewport3D center(float x, float y, float z) {
        this.centerX = x;
        this.centerY = y;
        this.centerZ = z;
        return this;
    }

    public Viewport3D autoSpin(float degreesPerTick) {
        this.autoSpin = degreesPerTick;
        return this;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    public float currentZoom() {
        return zoom;
    }

    @Override
    protected Size measure() {
        return new Size(width() > 0 ? width() : 80, height() > 0 ? height() : 80);
    }

    @Override
    protected void onTick() {
        if (autoSpin != 0f && !dragging) {
            yaw = (yaw + autoSpin) % 360f;
        }
    }

    @Override
    protected void paint(UiGraphics g) {
        Rect r = localRect();
        if (drawBackground) {
            Painter.inset(g, r);
        }
        Rect inner = drawBackground ? r.inset(1) : r;
        if (inner.isEmpty()) return;
        g.gui().flush();
        g.pushScissor(inner);
        PoseStack pose = g.gui().pose();
        pose.pushPose();
        float scale = baseScale * zoom * g.currentScale();
        pose.translate(inner.centerX(), inner.centerY(), Painter.Z_FLOATING);
        pose.scale(scale, -scale, scale);
        pose.mulPose(new Quaternionf().rotationXYZ((float) Math.toRadians(pitch), (float) Math.toRadians(yaw), 0f));
        pose.translate(-centerX, -centerY, -centerZ);
        if (lightMode == LightMode.ENTITY) {
            Lighting.setupForEntityInInventory();
        } else {
            Lighting.setupFor3DItems();
        }
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        renderer.render(pose, buffers, g.partialTick());
        buffers.endBatch();
        Lighting.setupFor3DItems();
        pose.popPose();
        g.popScissor();
        g.gui().flush();
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (!orbit || button != 0) return false;
        dragging = true;
        return true;
    }

    @Override
    protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
        if (!dragging) return false;
        yaw = (float) ((yaw + dx * 1.5) % 360.0);
        pitch = (float) Mth.clamp(pitch + dy, -89.0, 89.0);
        return true;
    }

    @Override
    protected boolean onMouseReleased(double lx, double ly, int button) {
        if (!dragging) return false;
        dragging = false;
        return true;
    }

    @Override
    protected boolean onMouseScrolled(double lx, double ly, double scrollX, double scrollY) {
        if (!wheelZoom || scrollY == 0) return false;
        zoom = Mth.clamp(zoom * (scrollY > 0 ? 1.15f : 1f / 1.15f), minZoom, maxZoom);
        return true;
    }
}

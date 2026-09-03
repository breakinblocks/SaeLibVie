package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class EntityView extends Widget {
    private final Supplier<@Nullable LivingEntity> entity;
    private int scale = 30;
    private float yOffset;
    private boolean followMouse = true;
    private float yaw = 200f;
    private float pitch = 0f;
    private float spinSpeed;
    private boolean drawBackground = true;
    private boolean orbitDrag = true;
    private boolean dragging;

    public EntityView(Supplier<@Nullable LivingEntity> entity) {
        this.entity = entity;
    }

    public EntityView(Rect bounds, Supplier<@Nullable LivingEntity> entity) {
        super(bounds);
        this.entity = entity;
    }

    public EntityView scale(int scale) {
        this.scale = scale;
        return this;
    }

    public EntityView yOffset(float offset) {
        this.yOffset = offset;
        return this;
    }

    public EntityView followMouse(boolean follow) {
        this.followMouse = follow;
        return this;
    }

    public EntityView yaw(float yaw) {
        this.yaw = yaw;
        return this;
    }

    public EntityView pitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    public EntityView spin(float degreesPerTick) {
        this.spinSpeed = degreesPerTick;
        this.followMouse = false;
        return this;
    }

    public EntityView background(boolean draw) {
        this.drawBackground = draw;
        return this;
    }

    public EntityView orbitDrag(boolean orbit) {
        this.orbitDrag = orbit;
        return this;
    }

    @Override
    protected Size measure() {
        return new Size(width() > 0 ? width() : 60, height() > 0 ? height() : 80);
    }

    @Override
    protected void onTick() {
        if (spinSpeed != 0f) {
            yaw = (yaw + spinSpeed) % 360f;
        }
    }

    @Override
    protected void paint(UiGraphics g) {
        Rect r = localRect();
        if (drawBackground) {
            Painter.inset(g, r);
        }
        LivingEntity living = entity.get();
        if (living == null) return;
        Rect inner = r.inset(2);
        g.pushScissor(inner);
        float cx = inner.x() + inner.w() / 2f;
        float cy = inner.y() + inner.h() / 2f;
        float oldBodyYaw = living.yBodyRot;
        float oldYaw = living.getYRot();
        float oldPitch = living.getXRot();
        float oldHeadYawO = living.yHeadRotO;
        float oldHeadYaw = living.yHeadRot;
        Quaternionf pose;
        Quaternionf camera;
        if (followMouse) {
            float angleX = (float) Math.atan((cx - g.localMouseX()) / 40.0);
            float angleY = (float) Math.atan((cy - g.localMouseY()) / 40.0);
            pose = new Quaternionf().rotateZ((float) Math.PI);
            camera = new Quaternionf().rotateX(angleY * 20f * (float) (Math.PI / 180.0));
            pose.mul(camera);
            living.yBodyRot = 180f + angleX * 20f;
            living.setYRot(180f + angleX * 40f);
            living.setXRot(-angleY * 20f);
            living.yHeadRot = living.getYRot();
            living.yHeadRotO = living.getYRot();
        } else {
            pose = new Quaternionf().rotateZ((float) Math.PI).rotateY((float) Math.toRadians(yaw)).rotateX((float) Math.toRadians(pitch));
            camera = null;
            living.yBodyRot = 0f;
            living.setYRot(0f);
            living.setXRot(0f);
            living.yHeadRot = 0f;
            living.yHeadRotO = 0f;
        }
        Vector3f translate = new Vector3f(0f, living.getBbHeight() / 2f + yOffset, 0f);
        InventoryScreen.renderEntityInInventory(g.gui(), cx, cy, scale, translate, pose, camera, living);
        living.yBodyRot = oldBodyYaw;
        living.setYRot(oldYaw);
        living.setXRot(oldPitch);
        living.yHeadRotO = oldHeadYawO;
        living.yHeadRot = oldHeadYaw;
        g.popScissor();
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (!orbitDrag || followMouse || button != 0) return false;
        dragging = true;
        return true;
    }

    @Override
    protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
        if (!dragging) return false;
        yaw = (float) ((yaw + dx * 1.5) % 360.0);
        pitch = (float) Math.max(-60.0, Math.min(60.0, pitch + dy));
        return true;
    }

    @Override
    protected boolean onMouseReleased(double lx, double ly, int button) {
        if (!dragging) return false;
        dragging = false;
        return true;
    }
}

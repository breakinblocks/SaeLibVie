package com.breakinblocks.saelibvie.ui.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.joml.Quaternionf;

public class ViewportRenderer extends PictureInPictureRenderer<ViewportRenderState> {
    public ViewportRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<ViewportRenderState> getRenderStateClass() {
        return ViewportRenderState.class;
    }

    @Override
    protected void renderToTexture(ViewportRenderState state, PoseStack poseStack) {
        Minecraft.getInstance().gameRenderer.getLighting()
                .setupFor(state.entityLighting() ? Lighting.Entry.ENTITY_IN_UI : Lighting.Entry.ITEMS_3D);
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.PI));
        poseStack.mulPose(new Quaternionf().rotationXYZ(
                (float) Math.toRadians(state.pitch()),
                (float) Math.toRadians(state.yaw()),
                0f));
        poseStack.translate(-state.centerX(), -state.centerY(), -state.centerZ());
        FeatureRenderDispatcher features = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
        state.renderer().render(poseStack, this.bufferSource, features.getSubmitNodeStorage(), state.partialTick());
        features.renderAllFeatures();
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0F;
    }

    @Override
    protected String getTextureLabel() {
        return "saelibvie viewport";
    }
}

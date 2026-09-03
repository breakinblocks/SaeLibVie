package com.breakinblocks.saelibvie.ui.render;

import com.breakinblocks.saelibvie.ui.widget.Viewport3D;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.Nullable;

public record ViewportRenderState(Viewport3D.Renderer renderer,
                                  float yaw,
                                  float pitch,
                                  float centerX,
                                  float centerY,
                                  float centerZ,
                                  boolean entityLighting,
                                  float partialTick,
                                  int x0,
                                  int y0,
                                  int x1,
                                  int y1,
                                  float scale,
                                  @Nullable ScreenRectangle scissorArea,
                                  @Nullable ScreenRectangle bounds) implements PictureInPictureRenderState {

    public ViewportRenderState(Viewport3D.Renderer renderer, float yaw, float pitch,
                               float centerX, float centerY, float centerZ,
                               boolean entityLighting, float partialTick,
                               int x0, int y0, int x1, int y1, float scale,
                               @Nullable ScreenRectangle scissorArea) {
        this(renderer, yaw, pitch, centerX, centerY, centerZ, entityLighting, partialTick,
                x0, y0, x1, y1, scale, scissorArea,
                PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
    }
}

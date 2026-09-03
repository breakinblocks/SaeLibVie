package com.breakinblocks.saelibvie.ui.hud;

import com.breakinblocks.saelibvie.SaeLibVie;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public final class HudLayer implements LayeredDraw.Layer {
    public static final ResourceLocation ID = SaeLibVie.id("hud_elements");

    private HudLayer() {
    }

    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ID, new HudLayer());
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        HudRegistry.render(graphics, deltaTracker);
    }
}

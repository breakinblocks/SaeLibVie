package com.breakinblocks.saelibvie.ui.hud;

import com.breakinblocks.saelibvie.SaeLibVie;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public final class HudLayer implements GuiLayer {
    public static final Identifier ID = SaeLibVie.id("hud_elements");

    private HudLayer() {
    }

    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ID, new HudLayer());
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        HudRegistry.render(graphics, deltaTracker);
    }
}

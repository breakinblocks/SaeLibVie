package com.breakinblocks.saelibvie;

import com.breakinblocks.saelibvie.client.SaeLibVieClient;
import com.breakinblocks.saelibvie.command.SaeCommands;
import com.breakinblocks.saelibvie.nbtedit.NbtEditSessions;
import com.breakinblocks.saelibvie.nbtedit.NbtResponseHandlers;
import com.breakinblocks.saelibvie.text.FormatParser;
import com.breakinblocks.saelibvie.text.RainbowTextColor;
import com.breakinblocks.saelibvie.text.RegisterTextColorsEvent;
import com.breakinblocks.saelibvie.text.TextColors;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Mod(SaeLibVie.MOD_ID)
public class SaeLibVie {
    public static final String MOD_ID = "saelibvie";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public SaeLibVie(IEventBus eventBus, ModContainer container, Dist dist) {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> SaeCommands.register(event));
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> NbtResponseHandlers.registerBuiltIn());
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> NbtEditSessions.clearAll());
        NeoForge.EVENT_BUS.addListener((RegisterTextColorsEvent event) -> event.register(RainbowTextColor.ID, RainbowTextColor.INSTANCE));
        eventBus.addListener((FMLCommonSetupEvent event) -> {
            Map<String, TextColor> colors = new HashMap<>();
            NeoForge.EVENT_BUS.post(new RegisterTextColorsEvent(colors));
            colors.forEach(TextColors::addCustomColor);
            FormatParser.SPECIAL_CODES.put('z', RainbowTextColor.INSTANCE);
        });
        if (dist.isClient()) {
            SaeLibVieClient.init(eventBus, container);
        }
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}

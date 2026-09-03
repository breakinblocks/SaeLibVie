package com.breakinblocks.saelibvie.client;

import com.breakinblocks.saelibvie.SaeLibVie;
import com.breakinblocks.saelibvie.client.demo.DemoScreen;
import com.breakinblocks.saelibvie.ui.color.ThemeLoader;
import com.breakinblocks.saelibvie.ui.color.Themes;
import com.breakinblocks.saelibvie.ui.hud.HudEditScreen;
import com.breakinblocks.saelibvie.ui.hud.HudLayer;
import com.breakinblocks.saelibvie.ui.hud.HudRegistry;
import com.breakinblocks.saelibvie.ui.screen.SaeLibVieCursor;
import com.breakinblocks.saelibvie.ui.util.ClientTasks;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

public final class SaeLibVieClient {
    public static final KeyMapping EDIT_HUD = new KeyMapping("key.saelibvie.edit_hud", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.saelibvie");

    private SaeLibVieClient() {
    }

    public static void init(IEventBus modBus, ModContainer container) {
        if (Minecraft.getInstance() == null) {
            return;
        }
        modBus.addListener(SaeLibVieClient::registerReloadListeners);
        modBus.addListener(SaeLibVieClient::registerGuiLayers);
        modBus.addListener(SaeLibVieClient::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(SaeLibVieClient::registerClientCommands);
        NeoForge.EVENT_BUS.addListener(SaeLibVieClient::onClientTick);
    }

    private static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ThemeLoader());
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        HudLayer.register(event);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(EDIT_HUD);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientTasks.drain();
        SaeLibVieCursor.tick();
        while (EDIT_HUD.consumeClick()) {
            if (mc.screen == null && mc.player != null) {
                HudEditScreen.open();
            }
        }
        if (mc.screen == null && mc.player != null) {
            HudRegistry.tick();
        }
    }

    private static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(SaeLibVie.MOD_ID)
                .then(Commands.literal("hud").executes(ctx -> {
                    Minecraft.getInstance().tell(HudEditScreen::open);
                    return 1;
                }))
                .then(Commands.literal("demo").executes(ctx -> {
                    Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new DemoScreen(null)));
                    return 1;
                }))
                .then(Commands.literal("themes").executes(ctx -> {
                    StringBuilder builder = new StringBuilder();
                    for (ResourceLocation id : Themes.ids()) {
                        if (builder.length() > 0) builder.append(", ");
                        builder.append(id);
                    }
                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.saelibvie.themes", Themes.getDefaultId().toString(), builder.toString()), false);
                    return 1;
                }))
                .then(Commands.literal("theme")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .suggests((ctx, builder) -> {
                                    for (ResourceLocation id : Themes.ids()) {
                                        builder.suggest(id.toString());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    ResourceLocation id = ResourceLocation.tryParse(StringArgumentType.getString(ctx, "id"));
                                    if (id == null || Themes.find(id) == null) {
                                        ctx.getSource().sendFailure(Component.translatable("commands.saelibvie.theme.unknown", String.valueOf(id)));
                                        return 0;
                                    }
                                    Themes.setDefault(id);
                                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.saelibvie.theme.set", id.toString()), false);
                                    return 1;
                                })))
                .then(Commands.literal("reload").executes(ctx -> {
                    ThemeLoader.loadConfig();
                    HudRegistry.load();
                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.saelibvie.reloaded"), false);
                    return 1;
                })));
    }
}

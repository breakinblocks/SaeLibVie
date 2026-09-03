package com.breakinblocks.saelibvie.client;

import com.breakinblocks.saelibvie.SaeLibVie;
import com.breakinblocks.saelibvie.ui.util.ClientTasks;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

public final class ClientUtil {
    public static final BooleanSupplier IS_CLIENT_OP = () -> {
        var player = Minecraft.getInstance().player;
        return player != null && player.hasPermissions(1);
    };

    private static final Map<String, @Nullable Method> STATIC_METHOD_CACHE = new HashMap<>();

    private ClientUtil() {
    }

    public static void runLater(Runnable runnable) {
        ClientTasks.later(runnable);
    }

    public static void execClientCommand(String command, boolean printChat) {
        Minecraft mc = Minecraft.getInstance();
        if (command.isEmpty() || mc.player == null) return;
        String message = ClientHooks.onClientSendMessage(command);
        if (message.isEmpty()) return;
        if (printChat) {
            mc.gui.getChat().addRecentChat(message);
        }
        String body = message.startsWith("/") ? message.substring(1) : message;
        mc.player.connection.sendCommand(body);
    }

    @Nullable
    public static <T> T getGuiAs(@Nullable Screen screen, Class<T> type) {
        if (type.isInstance(screen)) {
            return type.cast(screen);
        }
        return null;
    }

    @Nullable
    public static <T> T getCurrentGuiAs(Class<T> type) {
        return getGuiAs(Minecraft.getInstance().screen, type);
    }

    public static boolean handleClick(String scheme, String path) {
        Minecraft mc = Minecraft.getInstance();
        try {
            switch (scheme) {
                case "http", "https" -> {
                    URI uri = new URI(scheme + ":" + path);
                    if (mc.options.chatLinksPrompt().get()) {
                        Screen previous = mc.screen;
                        mc.setScreen(new ConfirmLinkScreen(accepted -> {
                            if (accepted) {
                                Util.getPlatform().openUri(uri);
                            }
                            mc.setScreen(previous);
                        }, uri.toString(), false));
                    } else {
                        Util.getPlatform().openUri(uri);
                    }
                    return true;
                }
                case "file" -> {
                    Util.getPlatform().openUri(new URI("file:" + path));
                    return true;
                }
                case "command" -> {
                    execClientCommand(path, false);
                    return true;
                }
                case "static_method" -> {
                    Method method = STATIC_METHOD_CACHE.computeIfAbsent(path, ClientUtil::resolveStaticMethod);
                    if (method == null) return false;
                    method.invoke(null);
                    return true;
                }
                case "custom" -> {
                    return fireCustom(ResourceLocation.parse(path));
                }
                default -> {
                    return fireCustom(ResourceLocation.parse(scheme + ":" + path));
                }
            }
        } catch (Exception e) {
            SaeLibVie.LOGGER.warn("Failed to handle click {}:{}", scheme, path, e);
            return false;
        }
    }

    private static boolean fireCustom(ResourceLocation id) {
        CustomClickEvent event = new CustomClickEvent(id);
        NeoForge.EVENT_BUS.post(event);
        return event.isHandled();
    }

    @Nullable
    private static Method resolveStaticMethod(String path) {
        try {
            int colon = path.lastIndexOf(':');
            if (colon < 0) return null;
            Class<?> type = Class.forName(path.substring(0, colon));
            Method method = type.getMethod(path.substring(colon + 1));
            if (!Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers()) || method.getReturnType() != void.class || method.getParameterCount() != 0) {
                return null;
            }
            return method;
        } catch (Exception e) {
            return null;
        }
    }

    public static RegistryAccess registryAccess() {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            throw new IllegalStateException("No client level");
        }
        return level.registryAccess();
    }
}

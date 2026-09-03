package com.breakinblocks.saelibvie.text;

import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TextColors {
    private static final Map<String, TextColor> CUSTOM = new ConcurrentHashMap<>();

    private TextColors() {
    }

    public static void addCustomColor(String id, TextColor color) {
        CUSTOM.put(id, color);
    }

    @Nullable
    public static TextColor get(String id) {
        return CUSTOM.get(id);
    }

    public static Map<String, TextColor> getCustomColors() {
        return Collections.unmodifiableMap(CUSTOM);
    }
}

package com.breakinblocks.saelibvie.text;

import net.minecraft.network.chat.TextColor;
import net.neoforged.bus.api.Event;

import java.util.Map;

public class RegisterTextColorsEvent extends Event {
    private final Map<String, TextColor> colors;

    public RegisterTextColorsEvent(Map<String, TextColor> colors) {
        this.colors = colors;
    }

    public void register(String id, TextColor color) {
        colors.put(id, color);
    }
}

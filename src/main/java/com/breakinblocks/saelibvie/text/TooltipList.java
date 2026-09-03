package com.breakinblocks.saelibvie.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

public class TooltipList {
    private final List<Component> lines = new ArrayList<>();
    public int backgroundColor = 0xC0100010;
    public int borderColorStart = 0x505000FF;
    public int borderColorEnd = ((borderColorStart & 0xFEFEFE) >> 1) | (borderColorStart & 0xFF000000);
    public int maxWidth = 0;
    public int xOffset = 0;
    public int yOffset = 0;

    public boolean shouldRender() {
        return !lines.isEmpty();
    }

    public void reset() {
        lines.clear();
        backgroundColor = 0xC0100010;
        borderColorStart = 0x505000FF;
        borderColorEnd = ((borderColorStart & 0xFEFEFE) >> 1) | (borderColorStart & 0xFF000000);
        maxWidth = 0;
        xOffset = 0;
        yOffset = 0;
    }

    public TooltipList add(Component component) {
        lines.add(component);
        return this;
    }

    public TooltipList blankLine() {
        return add(Component.empty());
    }

    public TooltipList styledString(String text, Style style) {
        return add(Component.literal(text).withStyle(style));
    }

    public TooltipList styledString(String text, ChatFormatting formatting) {
        return add(Component.literal(text).withStyle(formatting));
    }

    public TooltipList styledTranslate(String key, Style style, Object... args) {
        return add(Component.translatable(key, args).withStyle(style));
    }

    public TooltipList string(String text) {
        return add(Component.literal(text));
    }

    public TooltipList translate(String key, Object... args) {
        return add(Component.translatable(key, args));
    }

    public List<Component> getLines() {
        return lines;
    }
}

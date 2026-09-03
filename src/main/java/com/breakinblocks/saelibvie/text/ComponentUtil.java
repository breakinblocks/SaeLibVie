package com.breakinblocks.saelibvie.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.CommonHooks;

public final class ComponentUtil {
    private ComponentUtil() {
    }

    public static Component withLinks(String message) {
        return CommonHooks.newChatWithLinks(message);
    }

    public static MutableComponent hotkeyTooltip(String text) {
        return Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(text).withStyle(ChatFormatting.GRAY))
                .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static MutableComponent hotkeyTooltip(Component text) {
        return Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
                .append(text.copy().withStyle(ChatFormatting.GRAY))
                .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static MutableComponent translatedDimension(ResourceLocation id) {
        return Component.translatableWithFallback("dimension." + id.getNamespace() + "." + id.getPath(), id.toString());
    }

    public static MutableComponent translatedDimension(ResourceKey<?> key) {
        return translatedDimension(key.location());
    }
}

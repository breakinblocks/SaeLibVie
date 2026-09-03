package com.breakinblocks.saelibvie.nbtedit;

import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NbtEditInfo {
    private final CompoundTag tag = new CompoundTag();
    private final ListTag lines = new ListTag();

    private NbtEditInfo(String type) {
        tag.putString("type", type);
    }

    public static NbtEditInfo of(String type) {
        return new NbtEditInfo(type);
    }

    public NbtEditInfo title(Component title) {
        tag.putString("title", Component.Serializer.toJson(title, RegistryAccess.EMPTY));
        return this;
    }

    public NbtEditInfo line(String key, Object value) {
        MutableComponent component = Component.literal(key).withStyle(ChatFormatting.BLUE)
                .append(Component.literal(": ").withStyle(ChatFormatting.RESET))
                .append(Component.literal(String.valueOf(value)).withStyle(ChatFormatting.GOLD));
        lines.add(StringTag.valueOf(Component.Serializer.toJson(component, RegistryAccess.EMPTY)));
        return this;
    }

    public NbtEditInfo put(String key, Tag value) {
        tag.put(key, value);
        return this;
    }

    public CompoundTag build() {
        CompoundTag copy = tag.copy();
        if (!lines.isEmpty()) {
            copy.put("text", lines.copy());
        }
        return copy;
    }

    public static List<Component> readLines(CompoundTag info) {
        List<Component> out = new ArrayList<>();
        if (!info.contains("text", Tag.TAG_LIST)) return out;
        ListTag list = info.getList("text", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            try {
                Component component = Component.Serializer.fromJson(list.getString(i), RegistryAccess.EMPTY);
                if (component != null) out.add(component);
            } catch (Exception e) {
                out.add(Component.literal(list.getString(i)));
            }
        }
        return out;
    }

    public static String rootLabel(CompoundTag info) {
        if (info.contains("title", Tag.TAG_STRING)) {
            try {
                Component component = Component.Serializer.fromJson(info.getString("title"), RegistryAccess.EMPTY);
                if (component != null) return component.getString();
            } catch (Exception ignored) {
            }
        }
        if (info.contains("type", Tag.TAG_STRING)) {
            return info.getString("type").toUpperCase(Locale.ROOT);
        }
        return "ROOT";
    }
}

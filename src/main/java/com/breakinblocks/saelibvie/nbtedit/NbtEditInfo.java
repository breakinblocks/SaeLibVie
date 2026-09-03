package com.breakinblocks.saelibvie.nbtedit;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
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
        tag.store("title", ComponentSerialization.CODEC, title);
        return this;
    }

    public NbtEditInfo line(String key, Object value) {
        MutableComponent component = Component.literal(key).withStyle(ChatFormatting.BLUE)
                .append(Component.literal(": ").withStyle(ChatFormatting.RESET))
                .append(Component.literal(String.valueOf(value)).withStyle(ChatFormatting.GOLD));
        lines.add(ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, component).getOrThrow());
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
        ListTag list = info.getListOrEmpty("text");
        for (int i = 0; i < list.size(); i++) {
            ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, list.get(i)).result().ifPresent(out::add);
        }
        return out;
    }

    public static String rootLabel(CompoundTag info) {
        return info.read("title", ComponentSerialization.CODEC)
                .map(Component::getString)
                .or(() -> info.getString("type").map(type -> type.toUpperCase(Locale.ROOT)))
                .orElse("ROOT");
    }
}

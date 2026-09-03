package com.breakinblocks.saelibvie.nbtedit;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NbtEditSessions {
    private static final Map<UUID, CompoundTag> OPEN = new ConcurrentHashMap<>();

    private NbtEditSessions() {
    }

    public static void put(UUID player, CompoundTag info) {
        OPEN.put(player, info.copy());
    }

    @Nullable
    public static CompoundTag get(UUID player) {
        return OPEN.get(player);
    }

    public static void clear(UUID player) {
        OPEN.remove(player);
    }

    public static void clearAll() {
        OPEN.clear();
    }
}

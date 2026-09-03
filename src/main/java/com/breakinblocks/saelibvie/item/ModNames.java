package com.breakinblocks.saelibvie.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModList;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ModNames {
    private static Map<String, String> names;

    private ModNames() {
    }

    private static Map<String, String> names() {
        if (names == null) {
            Map<String, String> map = new HashMap<>();
            ModList.get().forEachModContainer((id, container) -> map.put(id, container.getModInfo().getDisplayName()));
            names = map;
        }
        return names;
    }

    public static Optional<String> getModName(String modId) {
        return Optional.ofNullable(names().get(modId));
    }

    public static Optional<String> getModName(Item item) {
        return getModName(BuiltInRegistries.ITEM.getKey(item).getNamespace());
    }

    public static Optional<String> getModName(Fluid fluid) {
        return getModName(BuiltInRegistries.FLUID.getKey(fluid).getNamespace());
    }
}

package com.breakinblocks.saelibvie.item;

import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public interface CraftingRemainderSetter {
    void saelibvie$setCraftingRemainingItem(@Nullable Item item);

    static void set(Item target, @Nullable Item remainder) {
        ((CraftingRemainderSetter) target).saelibvie$setCraftingRemainingItem(remainder);
    }
}

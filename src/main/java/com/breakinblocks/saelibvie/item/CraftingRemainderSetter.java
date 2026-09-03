package com.breakinblocks.saelibvie.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

public interface CraftingRemainderSetter {
    void saelibvie$setCraftingRemainder(@Nullable ItemStackTemplate template);

    static void set(Item target, @Nullable ItemStackTemplate remainder) {
        ((CraftingRemainderSetter) target).saelibvie$setCraftingRemainder(remainder);
    }

    static void set(Item target, @Nullable Item remainder) {
        set(target, remainder == null ? null : new ItemStackTemplate(remainder));
    }
}

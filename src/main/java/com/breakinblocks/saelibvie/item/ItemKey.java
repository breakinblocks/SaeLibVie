package com.breakinblocks.saelibvie.item;

import net.minecraft.world.item.ItemStack;

public record ItemKey(ItemStack stack) {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ItemKey other && ItemStack.isSameItemSameComponents(stack, other.stack);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(stack);
    }
}

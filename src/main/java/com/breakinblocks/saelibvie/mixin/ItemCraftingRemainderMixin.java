package com.breakinblocks.saelibvie.mixin;

import com.breakinblocks.saelibvie.item.CraftingRemainderSetter;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Item.class)
public class ItemCraftingRemainderMixin implements CraftingRemainderSetter {
    @Shadow
    @Final
    @Mutable
    @Nullable
    private Item craftingRemainingItem;

    @Override
    public void saelibvie$setCraftingRemainingItem(@Nullable Item item) {
        this.craftingRemainingItem = item;
    }
}

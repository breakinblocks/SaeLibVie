package com.breakinblocks.saelibvie.mixin;

import com.breakinblocks.saelibvie.item.CraftingRemainderSetter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
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
    private ItemStackTemplate craftingRemainingItem;

    @Override
    public void saelibvie$setCraftingRemainder(@Nullable ItemStackTemplate template) {
        this.craftingRemainingItem = template;
    }
}

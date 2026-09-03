package com.breakinblocks.saelibvie.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class FluidTextures {
    private FluidTextures() {
    }

    @Nullable
    public static ResourceLocation stillTexture(FluidStack stack) {
        if (stack.isEmpty()) return null;
        return IClientFluidTypeExtensions.of(stack.getFluid()).getStillTexture(stack);
    }

    public static int color(FluidStack stack) {
        if (stack.isEmpty()) return 0xFFFFFFFF;
        return IClientFluidTypeExtensions.of(stack.getFluid()).getTintColor(stack);
    }

    @Nullable
    public static TextureAtlasSprite stillSprite(FluidStack stack) {
        ResourceLocation texture = stillTexture(stack);
        if (texture == null) return null;
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
    }
}

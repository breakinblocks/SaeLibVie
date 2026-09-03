package com.breakinblocks.saelibvie.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class FluidTextures {
    private FluidTextures() {
    }

    @Nullable
    public static FluidModel model(FluidStack stack) {
        if (stack.isEmpty()) return null;
        return Minecraft.getInstance().getModelManager().getFluidStateModelSet()
                .get(stack.getFluid().defaultFluidState());
    }

    @Nullable
    public static Identifier stillTexture(FluidStack stack) {
        FluidModel model = model(stack);
        return model == null ? null : model.stillMaterial().sprite().atlasLocation();
    }

    public static int color(FluidStack stack) {
        FluidModel model = model(stack);
        if (model == null || model.fluidTintSource() == null) return 0xFFFFFFFF;
        int tint = model.fluidTintSource().colorAsStack(stack);
        return (tint >>> 24) == 0 ? tint | 0xFF000000 : tint;
    }

    @Nullable
    public static TextureAtlasSprite stillSprite(FluidStack stack) {
        FluidModel model = model(stack);
        return model == null ? null : model.stillMaterial().sprite();
    }
}

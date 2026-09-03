package com.breakinblocks.saelibvie.item;

import net.neoforged.neoforge.fluids.FluidStack;

public record FluidKey(FluidStack stack) {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof FluidKey other && FluidStack.isSameFluidSameComponents(stack, other.stack);
    }

    @Override
    public int hashCode() {
        return FluidStack.hashFluidAndComponents(stack);
    }
}

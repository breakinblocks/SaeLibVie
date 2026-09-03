package com.breakinblocks.saelibvie.item;

import com.breakinblocks.saelibvie.mixin.CompoundContainerAccessor;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;

public record ContainerKey(Container container) {
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ContainerKey other)) return false;
        if (container == other.container) return true;
        if (container instanceof CompoundContainer a && other.container instanceof CompoundContainer b) {
            Container a1 = ((CompoundContainerAccessor) a).saelibvie$container1();
            Container a2 = ((CompoundContainerAccessor) a).saelibvie$container2();
            Container b1 = ((CompoundContainerAccessor) b).saelibvie$container1();
            Container b2 = ((CompoundContainerAccessor) b).saelibvie$container2();
            return (a1 == b1 && a2 == b2) || (a1 == b2 && a2 == b1);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (container instanceof CompoundContainer compound) {
            CompoundContainerAccessor accessor = (CompoundContainerAccessor) compound;
            return System.identityHashCode(accessor.saelibvie$container1()) ^ System.identityHashCode(accessor.saelibvie$container2());
        }
        return container.hashCode();
    }
}

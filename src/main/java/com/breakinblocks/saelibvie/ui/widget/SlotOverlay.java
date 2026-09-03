package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;

public class SlotOverlay extends Widget {
    private final List<Slot> slots;
    private final IntPredicate shouldTint;
    private IntUnaryOperator colorFor;

    public SlotOverlay(List<Slot> slots, IntPredicate shouldTint, int color) {
        this.slots = slots;
        this.shouldTint = shouldTint;
        this.colorFor = index -> color;
    }

    public SlotOverlay(List<Slot> slots, IntPredicate shouldTint, IntUnaryOperator colorFor) {
        this.slots = slots;
        this.shouldTint = shouldTint;
        this.colorFor = colorFor;
    }

    public static SlotOverlay locked(List<Slot> slots, IntSupplier accessibleCount, int color) {
        return new SlotOverlay(slots, index -> index >= accessibleCount.getAsInt(), color);
    }

    @Override
    public boolean contains(double lx, double ly) {
        return false;
    }

    @Override
    protected void paint(UiGraphics g) {
        for (int i = 0; i < slots.size(); i++) {
            if (!shouldTint.test(i)) continue;
            Slot slot = slots.get(i);
            if (!slot.isActive()) continue;
            g.pushZ(200);
            g.fill(new Rect(slot.x, slot.y, 16, 16), colorFor.applyAsInt(i));
            g.popZ();
        }
    }
}

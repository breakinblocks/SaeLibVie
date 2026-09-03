package com.breakinblocks.saelibvie.ui.widget;

import net.minecraft.util.Mth;

import java.util.Optional;
import java.util.function.IntConsumer;

public class IntField extends TextField {
    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;
    private int step = 1;
    private boolean snapping;

    public IntField() {
        filter(s -> s.isEmpty() || s.equals("-") || s.matches("-?\\d+"));
        onScroll((text, up) -> Optional.of(Integer.toString(clamp((long) intValue() + (up ? step : -step)))));
    }

    public IntField range(int min, int max) {
        this.min = min;
        this.max = max;
        if (!snapping) snap();
        return this;
    }

    public IntField step(int step) {
        this.step = Math.max(1, step);
        return this;
    }

    public IntField onValue(IntConsumer consumer) {
        responder(s -> consumer.accept(intValue()));
        return this;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    private int clamp(long v) {
        return (int) Mth.clamp(v, min, max);
    }

    public int intValue() {
        String text = value();
        if (text.isEmpty() || text.equals("-")) {
            return clamp(0);
        }
        try {
            return clamp(Long.parseLong(text));
        } catch (NumberFormatException e) {
            return clamp(0);
        }
    }

    public void setValue(int v) {
        setValue(Integer.toString(clamp(v)));
    }

    @Override
    protected void onChanged() {
        if (snapping) return;
        snap();
    }

    private void snap() {
        String text = value();
        if (text.isEmpty() || text.equals("-")) return;
        try {
            long parsed = Long.parseLong(text);
            int clamped = clamp(parsed);
            if (parsed != clamped) {
                snapping = true;
                try {
                    replaceText(Integer.toString(clamped));
                } finally {
                    snapping = false;
                }
            }
        } catch (NumberFormatException ignored) {
        }
    }
}

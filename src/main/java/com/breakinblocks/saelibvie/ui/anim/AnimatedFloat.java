package com.breakinblocks.saelibvie.ui.anim;

import net.minecraft.Util;
import net.minecraft.util.Mth;

public final class AnimatedFloat {
    private float start;
    private float target;
    private long startTime;
    private long duration;
    private Easing easing = Easing.EASE_OUT;

    public AnimatedFloat(float initial) {
        this.start = initial;
        this.target = initial;
        this.startTime = Util.getMillis();
        this.duration = 0;
    }

    public AnimatedFloat easing(Easing easing) {
        this.easing = easing;
        return this;
    }

    public void set(float value) {
        this.start = value;
        this.target = value;
        this.duration = 0;
    }

    public void animateTo(float value, long durationMillis) {
        if (value == target && duration > 0) return;
        this.start = get();
        this.target = value;
        this.startTime = Util.getMillis();
        this.duration = Math.max(0, durationMillis);
    }

    public float target() {
        return target;
    }

    public float get() {
        if (duration <= 0) return target;
        long elapsed = Util.getMillis() - startTime;
        if (elapsed >= duration) return target;
        float t = easing.apply((float) elapsed / duration);
        return Mth.lerp(t, start, target);
    }

    public boolean isAnimating() {
        return duration > 0 && Util.getMillis() - startTime < duration;
    }
}

package com.breakinblocks.saelibvie.ui.anim;

public enum Easing {
    LINEAR,
    EASE_IN,
    EASE_OUT,
    EASE_IN_OUT,
    BACK_OUT,
    BOUNCE_OUT;

    public float apply(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return switch (this) {
            case LINEAR -> t;
            case EASE_IN -> t * t;
            case EASE_OUT -> 1f - (1f - t) * (1f - t);
            case EASE_IN_OUT -> t < 0.5f ? 2f * t * t : 1f - (float) Math.pow(-2f * t + 2f, 2) / 2f;
            case BACK_OUT -> {
                float c1 = 1.70158f;
                float c3 = c1 + 1f;
                yield 1f + c3 * (float) Math.pow(t - 1f, 3) + c1 * (float) Math.pow(t - 1f, 2);
            }
            case BOUNCE_OUT -> {
                float n1 = 7.5625f;
                float d1 = 2.75f;
                if (t < 1f / d1) {
                    yield n1 * t * t;
                } else if (t < 2f / d1) {
                    t -= 1.5f / d1;
                    yield n1 * t * t + 0.75f;
                } else if (t < 2.5f / d1) {
                    t -= 2.25f / d1;
                    yield n1 * t * t + 0.9375f;
                } else {
                    t -= 2.625f / d1;
                    yield n1 * t * t + 0.984375f;
                }
            }
        };
    }
}

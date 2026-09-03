package com.breakinblocks.saelibvie.ui.color;

import net.minecraft.util.Mth;

public final class Colors {
    public static final int TRANSPARENT = 0x00000000;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int BLACK = 0xFF000000;

    private Colors() {
    }

    public static int argb(int a, int r, int g, int b) {
        return (clamp(a) << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    public static int rgb(int r, int g, int b) {
        return argb(255, r, g, b);
    }

    public static int argb(float a, float r, float g, float b) {
        return argb(Math.round(a * 255f), Math.round(r * 255f), Math.round(g * 255f), Math.round(b * 255f));
    }

    public static int alpha(int color) {
        return (color >>> 24) & 0xFF;
    }

    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    public static float alphaF(int color) {
        return alpha(color) / 255f;
    }

    public static float redF(int color) {
        return red(color) / 255f;
    }

    public static float greenF(int color) {
        return green(color) / 255f;
    }

    public static float blueF(int color) {
        return blue(color) / 255f;
    }

    public static int withAlpha(int color, int alpha) {
        return (clamp(alpha) << 24) | (color & 0x00FFFFFF);
    }

    public static int withAlpha(int color, float alpha) {
        return withAlpha(color, Math.round(Mth.clamp(alpha, 0f, 1f) * 255f));
    }

    public static int multiplyAlpha(int color, float factor) {
        if (factor >= 1f) return color;
        return withAlpha(color, Math.round(alpha(color) * Mth.clamp(factor, 0f, 1f)));
    }

    public static int opaque(int color) {
        return color | 0xFF000000;
    }

    public static int lerp(float t, int from, int to) {
        t = Mth.clamp(t, 0f, 1f);
        int a = Math.round(Mth.lerp(t, alpha(from), alpha(to)));
        int r = Math.round(Mth.lerp(t, red(from), red(to)));
        int g = Math.round(Mth.lerp(t, green(from), green(to)));
        int b = Math.round(Mth.lerp(t, blue(from), blue(to)));
        return argb(a, r, g, b);
    }

    public static int multiply(int color, float factor) {
        return argb(alpha(color), Math.round(red(color) * factor), Math.round(green(color) * factor), Math.round(blue(color) * factor));
    }

    public static int lighten(int color, float amount) {
        return lerp(amount, color, withAlpha(WHITE, alpha(color)));
    }

    public static int darken(int color, float amount) {
        return lerp(amount, color, withAlpha(BLACK, alpha(color)));
    }

    public static int blend(int top, int bottom) {
        float ta = alphaF(top);
        if (ta >= 1f) return top;
        if (ta <= 0f) return bottom;
        float ba = alphaF(bottom);
        float outA = ta + ba * (1f - ta);
        if (outA <= 0f) return TRANSPARENT;
        float r = (redF(top) * ta + redF(bottom) * ba * (1f - ta)) / outA;
        float g = (greenF(top) * ta + greenF(bottom) * ba * (1f - ta)) / outA;
        float b = (blueF(top) * ta + blueF(bottom) * ba * (1f - ta)) / outA;
        return argb(outA, r, g, b);
    }

    public static int hsv(float hue, float saturation, float value) {
        return hsv(hue, saturation, value, 1f);
    }

    public static int hsv(float hue, float saturation, float value, float alpha) {
        hue = ((hue % 1f) + 1f) % 1f;
        float h6 = hue * 6f;
        int sector = (int) Math.floor(h6);
        float f = h6 - sector;
        float p = value * (1f - saturation);
        float q = value * (1f - saturation * f);
        float t = value * (1f - saturation * (1f - f));
        float r;
        float g;
        float b;
        switch (sector % 6) {
            case 0 -> { r = value; g = t; b = p; }
            case 1 -> { r = q; g = value; b = p; }
            case 2 -> { r = p; g = value; b = t; }
            case 3 -> { r = p; g = q; b = value; }
            case 4 -> { r = t; g = p; b = value; }
            default -> { r = value; g = p; b = q; }
        }
        return argb(alpha, r, g, b);
    }

    public static float[] toHsv(int color) {
        float r = redF(color);
        float g = greenF(color);
        float b = blueF(color);
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        float hue = 0f;
        if (delta > 0f) {
            if (max == r) {
                hue = ((g - b) / delta) % 6f;
            } else if (max == g) {
                hue = (b - r) / delta + 2f;
            } else {
                hue = (r - g) / delta + 4f;
            }
            hue /= 6f;
            if (hue < 0f) hue += 1f;
        }
        float saturation = max <= 0f ? 0f : delta / max;
        return new float[]{hue, saturation, max};
    }

    public static int luminance(int color) {
        return Math.round(0.2126f * red(color) + 0.7152f * green(color) + 0.0722f * blue(color));
    }

    public static int contrastText(int background) {
        return luminance(background) > 140 ? BLACK : WHITE;
    }

    public static int parse(String text, int fallback) {
        if (text == null) return fallback;
        String s = text.trim();
        if (s.startsWith("#")) s = s.substring(1);
        else if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
        try {
            long value = Long.parseLong(s, 16);
            if (s.length() <= 6) {
                return opaque((int) value);
            }
            return (int) value;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static String toHex(int color) {
        return String.format("#%08X", color);
    }

    private static int clamp(int channel) {
        return Math.max(0, Math.min(255, channel));
    }
}

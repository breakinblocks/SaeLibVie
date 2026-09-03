package com.breakinblocks.saelibvie.color;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.util.Mth;

public class MutableColor extends Color {
    public static final MutableColor TEMP = new MutableColor(255, 255, 255, 255);

    MutableColor(int red, int green, int blue, int alpha) {
        super(red, green, blue, alpha);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public MutableColor copy() {
        return new MutableColor(red, green, blue, alpha);
    }

    @Override
    public MutableColor mutable() {
        return this;
    }

    public MutableColor set(int r, int g, int b, int a) {
        red = r & 255;
        green = g & 255;
        blue = b & 255;
        alpha = a & 255;
        return this;
    }

    public MutableColor set(Color color, int a) {
        return set(color.redi(), color.greeni(), color.bluei(), a);
    }

    public MutableColor set(Color color) {
        return set(color.redi(), color.greeni(), color.bluei(), color.alphai());
    }

    public MutableColor set(int rgb, int a) {
        return set(rgb >> 16, rgb >> 8, rgb, a);
    }

    public MutableColor set(int argb) {
        return set(argb >> 16, argb >> 8, argb, argb >>> 24);
    }

    public MutableColor setAlpha(int a) {
        alpha = a;
        return this;
    }

    public MutableColor addBrightness(int amount) {
        red = Mth.clamp(red + amount, 0, 255);
        green = Mth.clamp(green + amount, 0, 255);
        blue = Mth.clamp(blue + amount, 0, 255);
        return this;
    }

    public MutableColor setFromHSB(float h, float s, float b) {
        if (s <= 0F || b <= 0F) {
            int v = (int) (b * 255F + 0.5F);
            red = green = blue = Mth.clamp(v, 0, 255);
            return this;
        }
        s = Math.min(s, 1F);
        b = Math.min(b, 1F);
        int rgb = HSBtoRGB(h, s, b);
        red = (rgb >> 16) & 255;
        green = (rgb >> 8) & 255;
        blue = rgb & 255;
        return this;
    }

    @Override
    public JsonElement getJson() {
        if (isEmpty()) return JsonNull.INSTANCE;
        JsonObject json = new JsonObject();
        json.addProperty("red", red);
        json.addProperty("green", green);
        json.addProperty("blue", blue);
        if (alpha < 255) {
            json.addProperty("alpha", alpha);
        }
        json.addProperty("mutable", true);
        return json;
    }

    static final class NoneColor extends MutableColor {
        private boolean assigned;

        NoneColor() {
            super(255, 255, 255, 255);
        }

        @Override
        public boolean isEmpty() {
            return !assigned;
        }

        @Override
        public MutableColor set(int r, int g, int b, int a) {
            assigned = true;
            return super.set(r, g, b, a);
        }

        @Override
        public int hashCode() {
            return assigned ? super.hashCode() : 0;
        }

        @Override
        public boolean equals(Object obj) {
            return assigned ? super.equals(obj) : obj == this;
        }

        @Override
        public String toString() {
            return assigned ? super.toString() : "";
        }
    }
}

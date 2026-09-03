package com.breakinblocks.saelibvie.color;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class Color {
    public static final Color EMPTY = new EmptyColor();
    private static final Color[] BLACK_A = new Color[256];
    private static final Color[] WHITE_A = new Color[256];

    static {
        for (int i = 0; i < 256; i++) {
            BLACK_A[i] = new Color(0, 0, 0, i);
            WHITE_A[i] = new Color(255, 255, 255, i);
        }
    }

    public static final Color BLACK = BLACK_A[255];
    public static final Color DARK_GRAY = rgb(0x212121);
    public static final Color GRAY = rgb(0x999999);
    public static final Color WHITE = WHITE_A[255];
    public static final Color RED = rgb(0xFF0000);
    public static final Color GREEN = rgb(0x00FF00);
    public static final Color BLUE = rgb(0x0000FF);
    public static final Color LIGHT_RED = rgb(0xFF5656);
    public static final Color LIGHT_GREEN = rgb(0x56FF56);
    public static final Color LIGHT_BLUE = rgb(0x5656FF);

    public static final Codec<Color> JSON_CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> fromJson(dynamic.convert(JsonOps.INSTANCE).getValue()),
            color -> new Dynamic<>(JsonOps.INSTANCE, color.getJson()));

    public static final Codec<Color> STRING_CODEC = Codec.STRING.comapFlatMap(s -> {
        Color color = fromString(s);
        return color.isEmpty() ? DataResult.error(() -> "Invalid color: " + s) : DataResult.success(color);
    }, Color::toString);

    public static final StreamCodec<ByteBuf, Color> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Color::fromString, Color::toString);

    protected int red;
    protected int green;
    protected int blue;
    protected int alpha;

    protected Color(int red, int green, int blue, int alpha) {
        this.red = red & 255;
        this.green = green & 255;
        this.blue = blue & 255;
        this.alpha = alpha & 255;
    }

    public static Color rgba(int r, int g, int b, int a) {
        r &= 255;
        g &= 255;
        b &= 255;
        a &= 255;
        if (a == 0) return EMPTY;
        if (r == 0 && g == 0 && b == 0) return BLACK_A[a];
        if (r == 255 && g == 255 && b == 255) return WHITE_A[a];
        return new Color(r, g, b, a);
    }

    public static Color rgb(int r, int g, int b) {
        return rgba(r, g, b, 255);
    }

    public static Color rgba(int argb) {
        return rgba(argb >> 16, argb >> 8, argb, argb >>> 24);
    }

    public static Color rgb(int rgb) {
        return rgba(rgb >> 16, rgb >> 8, rgb, 255);
    }

    public static Color rgb(Vec3 vec) {
        return rgb((int) (vec.x * 255D), (int) (vec.y * 255D), (int) (vec.z * 255D));
    }

    public static Color hsb(float h, float s, float b) {
        return rgb(HSBtoRGB(h, s, b));
    }

    public static Color of(ChatFormatting formatting) {
        return ColorTables.chat(formatting);
    }

    public static Color fromString(@Nullable String s) {
        if (s == null || s.isEmpty()) return EMPTY;
        if ((s.length() == 7 || s.length() == 9) && s.charAt(0) == '#') {
            long value = Long.parseLong(s.substring(1), 16);
            return s.length() == 9 ? rgba((int) value) : rgb((int) value);
        }
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "transparent" -> EMPTY;
            case "black" -> BLACK;
            case "dark_gray" -> DARK_GRAY;
            case "gray" -> GRAY;
            case "white" -> WHITE;
            case "red" -> RED;
            case "green" -> GREEN;
            case "blue" -> BLUE;
            case "light_red" -> LIGHT_RED;
            case "light_green" -> LIGHT_GREEN;
            case "light_blue" -> LIGHT_BLUE;
            default -> EMPTY;
        };
    }

    public static Color fromJson(@Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) return EMPTY;
        if (element.isJsonPrimitive()) return fromString(element.getAsString());
        if (element.isJsonArray()) {
            var array = element.getAsJsonArray();
            if (array.size() >= 3) {
                int a = array.size() >= 4 ? array.get(3).getAsInt() : 255;
                return rgba(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt(), a);
            }
            return EMPTY;
        }
        if (element.isJsonObject()) {
            JsonObject json = element.getAsJsonObject();
            if (json.has("red") && json.has("green") && json.has("blue")) {
                int a = json.has("alpha") ? json.get("alpha").getAsInt() : 255;
                Color color = rgba(json.get("red").getAsInt(), json.get("green").getAsInt(), json.get("blue").getAsInt(), a);
                if (json.has("mutable") && json.get("mutable").getAsBoolean()) {
                    return color.mutable();
                }
                return color;
            }
        }
        return EMPTY;
    }

    public int redi() {
        return red;
    }

    public int greeni() {
        return green;
    }

    public int bluei() {
        return blue;
    }

    public int alphai() {
        return alpha;
    }

    public float redf() {
        return red / 255F;
    }

    public float greenf() {
        return green / 255F;
    }

    public float bluef() {
        return blue / 255F;
    }

    public float alphaf() {
        return alpha / 255F;
    }

    public int rgba() {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public int rgb() {
        return (red << 16) | (green << 8) | blue;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean isMutable() {
        return false;
    }

    public Color copy() {
        return this;
    }

    public MutableColor mutable() {
        return new MutableColor(red, green, blue, alpha);
    }

    public Color whiteIfEmpty() {
        return isEmpty() ? WHITE : this;
    }

    public Style toStyle() {
        return Style.EMPTY.withColor(TextColor.fromRgb(rgb()));
    }

    @Override
    public int hashCode() {
        return rgba();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Color other && other.rgba() == rgba();
    }

    @Override
    public String toString() {
        if (alpha == 255) {
            return String.format(Locale.ROOT, "#%06X", rgb());
        }
        return String.format(Locale.ROOT, "#%08X", rgba());
    }

    public JsonElement getJson() {
        return isEmpty() ? JsonNull.INSTANCE : new JsonPrimitive(toString());
    }

    public Color withAlpha(int a) {
        if ((a & 255) == alpha) return this;
        return rgba(red, green, blue, a);
    }

    public Color withAlphaf(float f) {
        return withAlpha((int) (f * 255F));
    }

    public Color withTint(Color tint) {
        if (isEmpty()) return this;
        if (tint.isEmpty()) return EMPTY;
        if (tint.rgb() == 0xFFFFFF) return this;
        float a = tint.alphaf();
        int r = (int) Mth.lerp(a, red, tint.red);
        int g = (int) Mth.lerp(a, green, tint.green);
        int b = (int) Mth.lerp(a, blue, tint.blue);
        return rgba(r, g, b, alpha);
    }

    public Color lerp(Color other, float m) {
        m = Mth.clamp(m, 0F, 1F);
        int r = (int) (Mth.lerp(m, redf(), other.redf()) * 255F);
        int g = (int) (Mth.lerp(m, greenf(), other.greenf()) * 255F);
        int b = (int) (Mth.lerp(m, bluef(), other.bluef()) * 255F);
        int a = (int) (Mth.lerp(m, alphaf(), other.alphaf()) * 255F);
        return rgba(r, g, b, a);
    }

    public Color addBrightness(float percent) {
        float[] hsb = RGBtoHSB(red, green, blue, null);
        hsb[2] = Mth.clamp(hsb[2] + percent, 0F, 1F);
        int rgb = HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        return rgba(rgb >> 16, rgb >> 8, rgb, alpha);
    }

    public static float[] RGBtoHSB(int r, int g, int b, @Nullable float[] out) {
        if (out == null) out = new float[3];
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        float brightness = max / 255F;
        float saturation = max == 0 ? 0F : (max - min) / (float) max;
        float hue;
        if (saturation == 0F) {
            hue = 0F;
        } else {
            float delta = max - min;
            float rc = (max - r) / delta;
            float gc = (max - g) / delta;
            float bc = (max - b) / delta;
            if (r == max) hue = bc - gc;
            else if (g == max) hue = 2F + rc - bc;
            else hue = 4F + gc - rc;
            hue /= 6F;
            if (hue < 0F) hue += 1F;
        }
        out[0] = hue;
        out[1] = saturation;
        out[2] = brightness;
        return out;
    }

    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0;
        int g = 0;
        int b = 0;
        if (saturation == 0F) {
            r = g = b = (int) (brightness * 255F + 0.5F);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6F;
            float f = h - (float) Math.floor(h);
            float p = brightness * (1F - saturation);
            float q = brightness * (1F - saturation * f);
            float t = brightness * (1F - saturation * (1F - f));
            switch ((int) h) {
                case 0 -> { r = (int) (brightness * 255F + 0.5F); g = (int) (t * 255F + 0.5F); b = (int) (p * 255F + 0.5F); }
                case 1 -> { r = (int) (q * 255F + 0.5F); g = (int) (brightness * 255F + 0.5F); b = (int) (p * 255F + 0.5F); }
                case 2 -> { r = (int) (p * 255F + 0.5F); g = (int) (brightness * 255F + 0.5F); b = (int) (t * 255F + 0.5F); }
                case 3 -> { r = (int) (p * 255F + 0.5F); g = (int) (q * 255F + 0.5F); b = (int) (brightness * 255F + 0.5F); }
                case 4 -> { r = (int) (t * 255F + 0.5F); g = (int) (p * 255F + 0.5F); b = (int) (brightness * 255F + 0.5F); }
                case 5 -> { r = (int) (brightness * 255F + 0.5F); g = (int) (p * 255F + 0.5F); b = (int) (q * 255F + 0.5F); }
                default -> {
                }
            }
        }
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static void fill(GuiGraphicsExtractor graphics, int x, int y, int w, int h, Color color) {
        if (color.isEmpty() || w <= 0 || h <= 0) return;
        graphics.fill(x, y, x + w, y + h, color.rgba());
    }

    private static final class EmptyColor extends Color {
        private EmptyColor() {
            super(255, 255, 255, 255);
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public MutableColor mutable() {
            return new MutableColor.NoneColor();
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }

        @Override
        public String toString() {
            return "";
        }

        @Override
        public Color withAlpha(int a) {
            return this;
        }

        @Override
        public Color withTint(Color tint) {
            return this;
        }

        @Override
        public Color addBrightness(float percent) {
            return this;
        }
    }
}

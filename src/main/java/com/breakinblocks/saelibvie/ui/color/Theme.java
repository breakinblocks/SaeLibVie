package com.breakinblocks.saelibvie.ui.color;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class Theme {
    public static final String BORDER = "border";
    public static final String HEADER_HEIGHT = "header_height";
    public static final String TITLE_BAR_HEIGHT = "title_bar_height";
    public static final String PADDING = "padding";
    public static final String GAP = "gap";
    public static final String ROW_HEIGHT = "row_height";
    public static final String BUTTON_HEIGHT = "button_height";
    public static final String SCROLLBAR_WIDTH = "scrollbar_width";
    public static final String SLOT_SIZE = "slot_size";
    public static final String RESIZE_GRIP_SIZE = "resize_grip_size";
    public static final String TEXT_SHADOW = "text_shadow";
    public static final String UPPERCASE_HEADERS = "uppercase_headers";

    public enum Style {
        FLAT,
        BEVEL
    }

    private final ResourceLocation id;
    private final Style style;
    private final float opacity;
    private final Map<ColorToken, Integer> colors;
    private final Map<String, Integer> metrics;
    @Nullable
    private final Skin skin;

    private Theme(ResourceLocation id, Style style, float opacity, Map<ColorToken, Integer> colors, Map<String, Integer> metrics, @Nullable Skin skin) {
        this.id = id;
        this.style = style;
        this.opacity = opacity;
        this.colors = Collections.unmodifiableMap(new LinkedHashMap<>(colors));
        this.metrics = Collections.unmodifiableMap(new LinkedHashMap<>(metrics));
        this.skin = skin;
    }

    public ResourceLocation id() {
        return id;
    }

    public Style style() {
        return style;
    }

    public float opacity() {
        return opacity;
    }

    @Nullable
    public Skin skin() {
        return skin;
    }

    public boolean hasSkin() {
        return skin != null;
    }

    public int color(ColorToken token) {
        Integer value = colors.get(token);
        if (value == null) {
            return 0xFFFF00FF;
        }
        return value;
    }

    public int color(ColorToken token, int fallback) {
        Integer value = colors.get(token);
        return value == null ? fallback : value;
    }

    public boolean has(ColorToken token) {
        return colors.containsKey(token);
    }

    public int metric(String key, int fallback) {
        Integer value = metrics.get(key);
        return value == null ? fallback : value;
    }

    public boolean flag(String key, boolean fallback) {
        Integer value = metrics.get(key);
        return value == null ? fallback : value != 0;
    }

    public int border() {
        return metric(BORDER, 1);
    }

    public int headerHeight() {
        return metric(HEADER_HEIGHT, 11);
    }

    public int titleBarHeight() {
        return metric(TITLE_BAR_HEIGHT, 12);
    }

    public int padding() {
        return metric(PADDING, 4);
    }

    public int gap() {
        return metric(GAP, 2);
    }

    public int rowHeight() {
        return metric(ROW_HEIGHT, 12);
    }

    public int buttonHeight() {
        return metric(BUTTON_HEIGHT, 14);
    }

    public int scrollbarWidth() {
        return metric(SCROLLBAR_WIDTH, 4);
    }

    public int slotSize() {
        return metric(SLOT_SIZE, 18);
    }

    public int resizeGripSize() {
        return metric(RESIZE_GRIP_SIZE, 6);
    }

    public boolean textShadow() {
        return flag(TEXT_SHADOW, false);
    }

    public boolean uppercaseHeaders() {
        return flag(UPPERCASE_HEADERS, false);
    }

    public Map<ColorToken, Integer> colors() {
        return colors;
    }

    public Map<String, Integer> metrics() {
        return metrics;
    }

    public Theme withOpacity(float newOpacity) {
        return new Theme(id, style, newOpacity, colors, metrics, skin);
    }

    public Theme withColor(ColorToken token, int color) {
        return derive(id).color(token, color).build();
    }

    public Theme mapColors(Function<Integer, Integer> mapper) {
        Builder builder = derive(id);
        colors.forEach((token, color) -> builder.color(token, mapper.apply(color)));
        return builder.build();
    }

    public Builder derive(ResourceLocation newId) {
        Builder builder = new Builder(newId);
        builder.style = style;
        builder.opacity = opacity;
        builder.colors.putAll(colors);
        builder.metrics.putAll(metrics);
        builder.skin = skin;
        return builder;
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("style", style.name().toLowerCase(Locale.ROOT));
        root.addProperty("opacity", opacity);
        JsonObject colorObj = new JsonObject();
        colors.forEach((token, color) -> colorObj.addProperty(token.name(), Colors.toHex(color)));
        root.add("colors", colorObj);
        JsonObject metricObj = new JsonObject();
        metrics.forEach(metricObj::addProperty);
        root.add("metrics", metricObj);
        if (skin != null) {
            root.add("skin", skin.toJson());
        }
        return root;
    }

    public static Theme fromJson(ResourceLocation id, JsonObject json, @Nullable Theme parent) {
        Builder builder = parent != null ? parent.derive(id) : builder(id);
        if (json.has("style")) {
            String styleName = json.get("style").getAsString();
            builder.style(Style.valueOf(styleName.toUpperCase(Locale.ROOT)));
        }
        if (json.has("opacity")) {
            builder.opacity(json.get("opacity").getAsFloat());
        }
        if (json.has("colors") && json.get("colors").isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("colors").entrySet()) {
                JsonElement value = entry.getValue();
                int color;
                if (value.isJsonPrimitive() && ((JsonPrimitive) value).isNumber()) {
                    color = value.getAsInt();
                } else {
                    color = Colors.parse(value.getAsString(), 0xFFFF00FF);
                }
                builder.color(ColorToken.of(entry.getKey()), color);
            }
        }
        if (json.has("metrics") && json.get("metrics").isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("metrics").entrySet()) {
                JsonElement value = entry.getValue();
                if (value.isJsonPrimitive() && ((JsonPrimitive) value).isBoolean()) {
                    builder.metric(entry.getKey(), value.getAsBoolean() ? 1 : 0);
                } else {
                    builder.metric(entry.getKey(), value.getAsInt());
                }
            }
        }
        if (json.has("skin")) {
            JsonElement skinElement = json.get("skin");
            if (skinElement.isJsonNull()) {
                builder.skin(null);
            } else if (skinElement.isJsonObject()) {
                builder.skin(Skin.fromJson(skinElement.getAsJsonObject(), builder.skin));
            }
        }
        return builder.build();
    }

    public static final class Builder {
        private final ResourceLocation id;
        private Style style = Style.FLAT;
        private float opacity = 1f;
        private final Map<ColorToken, Integer> colors = new LinkedHashMap<>();
        private final Map<String, Integer> metrics = new LinkedHashMap<>();
        @Nullable
        private Skin skin;

        private Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder style(Style style) {
            this.style = style;
            return this;
        }

        public Builder opacity(float opacity) {
            this.opacity = Math.max(0f, Math.min(1f, opacity));
            return this;
        }

        public Builder color(ColorToken token, int color) {
            colors.put(token, color);
            return this;
        }

        public Builder metric(String key, int value) {
            metrics.put(key, value);
            return this;
        }

        public Builder flag(String key, boolean value) {
            metrics.put(key, value ? 1 : 0);
            return this;
        }

        public Builder skin(@Nullable Skin skin) {
            this.skin = skin;
            return this;
        }

        public Theme build() {
            return new Theme(id, style, opacity, colors, metrics, skin);
        }
    }
}

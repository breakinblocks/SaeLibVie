package com.breakinblocks.saelibvie.ui.color;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Skin {
    public static final String WINDOW = "window";
    public static final String PANEL = "panel";
    public static final String INSET = "inset";
    public static final String HEADER = "header";
    public static final String BUTTON = "button";
    public static final String BUTTON_HOVER = "button_hover";
    public static final String BUTTON_DISABLED = "button_disabled";
    public static final String BUTTON_SELECTED = "button_selected";
    public static final String SLOT = "slot";
    public static final String SCROLL_TRACK = "scroll_track";
    public static final String SCROLL_THUMB = "scroll_thumb";
    public static final String TEXT_FIELD = "text_field";
    public static final String TEXT_FIELD_FOCUSED = "text_field_focused";
    public static final String SLIDER_TRACK = "slider_track";
    public static final String SLIDER_HANDLE = "slider_handle";
    public static final String SLIDER_HANDLE_HOVER = "slider_handle_hover";
    public static final String CHECKBOX = "checkbox";
    public static final String CHECKBOX_CHECKED = "checkbox_checked";
    public static final String TAB = "tab";
    public static final String TAB_SELECTED = "tab_selected";
    public static final String TITLE_BAR = "title_bar";
    public static final String PROGRESS_BG = "progress_bg";
    public static final String PROGRESS_FILL = "progress_fill";

    private final Map<String, ResourceLocation> sprites;

    private Skin(Map<String, ResourceLocation> sprites) {
        this.sprites = Collections.unmodifiableMap(new LinkedHashMap<>(sprites));
    }

    @Nullable
    public ResourceLocation sprite(String key) {
        return sprites.get(key);
    }

    public boolean has(String key) {
        return sprites.containsKey(key);
    }

    public Map<String, ResourceLocation> sprites() {
        return sprites;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        sprites.forEach((key, value) -> json.addProperty(key, value.toString()));
        return json;
    }

    public static Skin fromJson(JsonObject json, @Nullable Skin parent) {
        Builder builder = builder();
        if (parent != null) {
            builder.sprites.putAll(parent.sprites);
        }
        json.entrySet().forEach(entry -> {
            if (entry.getValue().isJsonNull()) {
                builder.sprites.remove(entry.getKey());
            } else {
                builder.sprite(entry.getKey(), ResourceLocation.parse(entry.getValue().getAsString()));
            }
        });
        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Skin vanilla() {
        return builder()
                .sprite(BUTTON, ResourceLocation.withDefaultNamespace("widget/button"))
                .sprite(BUTTON_HOVER, ResourceLocation.withDefaultNamespace("widget/button_highlighted"))
                .sprite(BUTTON_DISABLED, ResourceLocation.withDefaultNamespace("widget/button_disabled"))
                .sprite(SLOT, ResourceLocation.withDefaultNamespace("container/slot"))
                .sprite(SCROLL_TRACK, ResourceLocation.withDefaultNamespace("widget/scroller_background"))
                .sprite(SCROLL_THUMB, ResourceLocation.withDefaultNamespace("widget/scroller"))
                .sprite(TEXT_FIELD, ResourceLocation.withDefaultNamespace("widget/text_field"))
                .sprite(TEXT_FIELD_FOCUSED, ResourceLocation.withDefaultNamespace("widget/text_field_highlighted"))
                .sprite(SLIDER_TRACK, ResourceLocation.withDefaultNamespace("widget/slider"))
                .sprite(SLIDER_HANDLE, ResourceLocation.withDefaultNamespace("widget/slider_handle"))
                .sprite(SLIDER_HANDLE_HOVER, ResourceLocation.withDefaultNamespace("widget/slider_handle_highlighted"))
                .build();
    }

    public static final class Builder {
        private final Map<String, ResourceLocation> sprites = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder sprite(String key, ResourceLocation sprite) {
            sprites.put(key, sprite);
            return this;
        }

        public Skin build() {
            return new Skin(sprites);
        }
    }
}

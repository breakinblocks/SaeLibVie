package com.breakinblocks.saelibvie.ui.color;

import com.breakinblocks.saelibvie.SaeLibVie;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class Themes {
    private static final Map<Identifier, Theme> CODE = new LinkedHashMap<>();
    private static final Map<Identifier, JsonObject> RESOURCE_JSON = new LinkedHashMap<>();
    private static final Map<Identifier, JsonObject> CONFIG_JSON = new LinkedHashMap<>();
    private static final Map<Identifier, Theme> RESOLVED = new LinkedHashMap<>();
    private static final List<Consumer<Theme>> LISTENERS = new ArrayList<>();

    public static final Identifier DARK_ID = SaeLibVie.id("dark");
    public static final Identifier MIDNIGHT_ID = SaeLibVie.id("midnight");
    public static final Identifier BLOOD_ID = SaeLibVie.id("blood");
    public static final Identifier VANILLA_ID = SaeLibVie.id("vanilla");

    public static final Theme DARK = Theme.builder(DARK_ID)
            .style(Theme.Style.FLAT)
            .color(ColorToken.WINDOW_BG, 0xFF16191C)
            .color(ColorToken.PANEL_BG, 0xFF1B1F23)
            .color(ColorToken.INSET_BG, 0xFF0E1113)
            .color(ColorToken.HEADER_BG, 0xFF12403C)
            .color(ColorToken.BORDER_OUT, 0xFF9BA3A9)
            .color(ColorToken.BORDER_IN, 0xFF000000)
            .color(ColorToken.BORDER_SOFT, 0xFF454C52)
            .color(ColorToken.TEXT, 0xFFE4E9EC)
            .color(ColorToken.TEXT_DIM, 0xFF8C969C)
            .color(ColorToken.TEXT_TITLE, 0xFFE4E9EC)
            .color(ColorToken.TEXT_DISABLED, 0xFF5C666C)
            .color(ColorToken.ACCENT, 0xFF35F1D7)
            .color(ColorToken.ACCENT_DIM, 0xFF17544B)
            .color(ColorToken.POSITIVE, 0xFF58E07E)
            .color(ColorToken.WARNING, 0xFFE0B458)
            .color(ColorToken.NEGATIVE, 0xFFE05858)
            .color(ColorToken.HOVER, 0x2AFFFFFF)
            .color(ColorToken.SELECTED, 0x6017544B)
            .color(ColorToken.FOCUS, 0xFF35F1D7)
            .color(ColorToken.BUTTON_BG, 0xFF1B1F23)
            .color(ColorToken.BUTTON_HOVER, 0xFF12403C)
            .color(ColorToken.BUTTON_DISABLED, 0xFF0E1113)
            .color(ColorToken.SCROLL_TRACK, 0x40FFFFFF)
            .color(ColorToken.SCROLL_THUMB, 0xFF8C969C)
            .color(ColorToken.SLOT_BG, 0xFF0E1113)
            .color(ColorToken.SLOT_BORDER, 0xFF454C52)
            .color(ColorToken.TOOLTIP_BG, 0xF0100010)
            .color(ColorToken.TOOLTIP_BORDER, 0xFF17544B)
            .color(ColorToken.OVERLAY_DIM, 0xA0000000)
            .color(ColorToken.PROGRESS_BG, 0xFF0E1113)
            .color(ColorToken.PROGRESS_FILL, 0xFF17544B)
            .color(ColorToken.PROGRESS_EDGE, 0xFF35F1D7)
            .color(ColorToken.GRAPH_LINE, 0xFF35F1D7)
            .color(ColorToken.GRAPH_FILL, 0xFF17544B)
            .color(ColorToken.TITLE_BAR, 0xFF0E1113)
            .color(ColorToken.TITLE_BAR_ACTIVE, 0xFF12403C)
            .color(ColorToken.RESIZE_GRIP, 0xFF454C52)
            .metric(Theme.BORDER, 1)
            .metric(Theme.HEADER_HEIGHT, 11)
            .metric(Theme.TITLE_BAR_HEIGHT, 12)
            .metric(Theme.PADDING, 4)
            .metric(Theme.GAP, 2)
            .metric(Theme.ROW_HEIGHT, 12)
            .metric(Theme.BUTTON_HEIGHT, 14)
            .metric(Theme.SCROLLBAR_WIDTH, 4)
            .metric(Theme.SLOT_SIZE, 18)
            .metric(Theme.RESIZE_GRIP_SIZE, 6)
            .flag(Theme.TEXT_SHADOW, false)
            .flag(Theme.UPPERCASE_HEADERS, true)
            .build();

    public static final Theme MIDNIGHT = DARK.derive(MIDNIGHT_ID)
            .color(ColorToken.WINDOW_BG, 0xFF1B1B26)
            .color(ColorToken.PANEL_BG, 0xFF1B1B26)
            .color(ColorToken.INSET_BG, 0xFF10101A)
            .color(ColorToken.HEADER_BG, 0xFF262638)
            .color(ColorToken.BORDER_OUT, 0xFF3C3C58)
            .color(ColorToken.BORDER_IN, 0xFF3C3C58)
            .color(ColorToken.BORDER_SOFT, 0xFF3C3C58)
            .color(ColorToken.TEXT, 0xFFE8E8F2)
            .color(ColorToken.TEXT_DIM, 0xFF9A9AB4)
            .color(ColorToken.TEXT_TITLE, 0xFFF2E9C9)
            .color(ColorToken.TEXT_DISABLED, 0xFF5A5A74)
            .color(ColorToken.ACCENT, 0xFF6FD7E8)
            .color(ColorToken.ACCENT_DIM, 0xFF2C5A66)
            .color(ColorToken.POSITIVE, 0xFF7FD46B)
            .color(ColorToken.WARNING, 0xFFF5C542)
            .color(ColorToken.NEGATIVE, 0xFFE0556A)
            .color(ColorToken.FOCUS, 0xFF6FD7E8)
            .color(ColorToken.BUTTON_BG, 0xFF262638)
            .color(ColorToken.BUTTON_HOVER, 0xFF34344C)
            .color(ColorToken.BUTTON_DISABLED, 0xFF10101A)
            .color(ColorToken.SCROLL_THUMB, 0xFF6FD7E8)
            .color(ColorToken.SLOT_BG, 0xFF10101A)
            .color(ColorToken.SLOT_BORDER, 0xFF3C3C58)
            .color(ColorToken.TOOLTIP_BORDER, 0xFF3C3C58)
            .color(ColorToken.OVERLAY_DIM, 0xE0101018)
            .color(ColorToken.PROGRESS_BG, 0xFF10101A)
            .color(ColorToken.PROGRESS_FILL, 0xFF2C5A66)
            .color(ColorToken.PROGRESS_EDGE, 0xFF6FD7E8)
            .color(ColorToken.GRAPH_LINE, 0xFF6FD7E8)
            .color(ColorToken.GRAPH_FILL, 0xFF2C5A66)
            .color(ColorToken.TITLE_BAR, 0xFF10101A)
            .color(ColorToken.TITLE_BAR_ACTIVE, 0xFF262638)
            .flag(Theme.UPPERCASE_HEADERS, false)
            .flag(Theme.TEXT_SHADOW, true)
            .build();

    public static final Theme BLOOD = DARK.derive(BLOOD_ID)
            .opacity(1f)
            .color(ColorToken.WINDOW_BG, 0xCC2A1520)
            .color(ColorToken.PANEL_BG, 0xCC1A0A0A)
            .color(ColorToken.INSET_BG, 0xCC120606)
            .color(ColorToken.HEADER_BG, 0xFF4A1A2A)
            .color(ColorToken.BORDER_OUT, 0xFF6B1A1A)
            .color(ColorToken.BORDER_IN, 0xFF1A0A0A)
            .color(ColorToken.BORDER_SOFT, 0xFF6B1A1A)
            .color(ColorToken.TEXT, 0xFFCFCFCF)
            .color(ColorToken.TEXT_DIM, 0xFF8A7A7A)
            .color(ColorToken.TEXT_TITLE, 0xFFA05050)
            .color(ColorToken.TEXT_DISABLED, 0xFF5A4444)
            .color(ColorToken.ACCENT, 0xFFE7B0C0)
            .color(ColorToken.ACCENT_DIM, 0xFF8B3A3A)
            .color(ColorToken.HOVER, 0x44FFFFFF)
            .color(ColorToken.SELECTED, 0xAA4A1A2A)
            .color(ColorToken.FOCUS, 0xFFE7B0C0)
            .color(ColorToken.BUTTON_BG, 0xCC2A1520)
            .color(ColorToken.BUTTON_HOVER, 0xFF4A1A2A)
            .color(ColorToken.BUTTON_DISABLED, 0xCC120606)
            .color(ColorToken.SCROLL_TRACK, 0x40FFFFFF)
            .color(ColorToken.SCROLL_THUMB, 0xFF8B3A3A)
            .color(ColorToken.SLOT_BG, 0xCC120606)
            .color(ColorToken.SLOT_BORDER, 0xFF6B1A1A)
            .color(ColorToken.TOOLTIP_BORDER, 0xFF6B1A1A)
            .color(ColorToken.PROGRESS_BG, 0xCC120606)
            .color(ColorToken.PROGRESS_FILL, 0xFF8B3A3A)
            .color(ColorToken.PROGRESS_EDGE, 0xFFE7B0C0)
            .color(ColorToken.GRAPH_LINE, 0xFFE7B0C0)
            .color(ColorToken.GRAPH_FILL, 0xFF8B3A3A)
            .color(ColorToken.TITLE_BAR, 0xCC1A0A0A)
            .color(ColorToken.TITLE_BAR_ACTIVE, 0xFF4A1A2A)
            .color(ColorToken.RESIZE_GRIP, 0xFF6B1A1A)
            .flag(Theme.UPPERCASE_HEADERS, false)
            .build();

    public static final Theme VANILLA = Theme.builder(VANILLA_ID)
            .style(Theme.Style.BEVEL)
            .color(ColorToken.WINDOW_BG, 0xFFC6C6C6)
            .color(ColorToken.PANEL_BG, 0xFFC6C6C6)
            .color(ColorToken.INSET_BG, 0xFF8B8B8B)
            .color(ColorToken.HEADER_BG, 0xFFA0A0A0)
            .color(ColorToken.BORDER_OUT, 0xFF000000)
            .color(ColorToken.BORDER_IN, 0xFFFFFFFF)
            .color(ColorToken.BORDER_SOFT, 0xFF555555)
            .color(ColorToken.TEXT, 0xFF404040)
            .color(ColorToken.TEXT_DIM, 0xFF808080)
            .color(ColorToken.TEXT_TITLE, 0xFF404040)
            .color(ColorToken.TEXT_DISABLED, 0xFFA0A0A0)
            .color(ColorToken.ACCENT, 0xFF3F76E4)
            .color(ColorToken.ACCENT_DIM, 0xFF7A9AD6)
            .color(ColorToken.POSITIVE, 0xFF2E7D32)
            .color(ColorToken.WARNING, 0xFFB8860B)
            .color(ColorToken.NEGATIVE, 0xFFB22222)
            .color(ColorToken.HOVER, 0x80FFFFFF)
            .color(ColorToken.SELECTED, 0x803F76E4)
            .color(ColorToken.FOCUS, 0xFFFFFFFF)
            .color(ColorToken.BUTTON_BG, 0xFF6F6F6F)
            .color(ColorToken.BUTTON_HOVER, 0xFF7F8FBF)
            .color(ColorToken.BUTTON_DISABLED, 0xFF4F4F4F)
            .color(ColorToken.SCROLL_TRACK, 0xFF000000)
            .color(ColorToken.SCROLL_THUMB, 0xFF808080)
            .color(ColorToken.SLOT_BG, 0xFF8B8B8B)
            .color(ColorToken.SLOT_BORDER, 0xFF373737)
            .color(ColorToken.TOOLTIP_BG, 0xF0100010)
            .color(ColorToken.TOOLTIP_BORDER, 0xFF5000FF)
            .color(ColorToken.OVERLAY_DIM, 0xC0101010)
            .color(ColorToken.PROGRESS_BG, 0xFF8B8B8B)
            .color(ColorToken.PROGRESS_FILL, 0xFF3F76E4)
            .color(ColorToken.PROGRESS_EDGE, 0xFFFFFFFF)
            .color(ColorToken.GRAPH_LINE, 0xFF3F76E4)
            .color(ColorToken.GRAPH_FILL, 0xFF7A9AD6)
            .color(ColorToken.TITLE_BAR, 0xFFC6C6C6)
            .color(ColorToken.TITLE_BAR_ACTIVE, 0xFFA0A0A0)
            .color(ColorToken.RESIZE_GRIP, 0xFF555555)
            .metric(Theme.BORDER, 1)
            .metric(Theme.HEADER_HEIGHT, 12)
            .metric(Theme.TITLE_BAR_HEIGHT, 14)
            .metric(Theme.PADDING, 6)
            .metric(Theme.GAP, 4)
            .metric(Theme.ROW_HEIGHT, 12)
            .metric(Theme.BUTTON_HEIGHT, 20)
            .metric(Theme.SCROLLBAR_WIDTH, 6)
            .metric(Theme.SLOT_SIZE, 18)
            .metric(Theme.RESIZE_GRIP_SIZE, 6)
            .flag(Theme.TEXT_SHADOW, false)
            .flag(Theme.UPPERCASE_HEADERS, false)
            .skin(Skin.vanilla())
            .build();

    private static Identifier defaultId = DARK_ID;
    private static float globalOpacity = 1f;

    static {
        register(DARK);
        register(MIDNIGHT);
        register(BLOOD);
        register(VANILLA);
    }

    private Themes() {
    }

    public static synchronized void register(Theme theme) {
        CODE.put(theme.id(), theme);
        resolveAll();
    }

    public static synchronized Theme get(Identifier id) {
        Theme theme = RESOLVED.get(id);
        if (theme == null) {
            theme = RESOLVED.get(defaultId);
        }
        return theme == null ? DARK : theme;
    }

    @Nullable
    public static synchronized Theme find(Identifier id) {
        return RESOLVED.get(id);
    }

    public static synchronized Theme getDefault() {
        return get(defaultId);
    }

    public static synchronized Identifier getDefaultId() {
        return defaultId;
    }

    public static synchronized void setDefault(Identifier id) {
        defaultId = id;
        notifyListeners();
    }

    public static synchronized float globalOpacity() {
        return globalOpacity;
    }

    public static synchronized void setGlobalOpacity(float opacity) {
        globalOpacity = Math.max(0f, Math.min(1f, opacity));
        resolveAll();
    }

    public static synchronized List<Identifier> ids() {
        return Collections.unmodifiableList(new ArrayList<>(RESOLVED.keySet()));
    }

    public static synchronized void addListener(Consumer<Theme> listener) {
        LISTENERS.add(listener);
    }

    public static synchronized void setResourceJson(Map<Identifier, JsonObject> json) {
        RESOURCE_JSON.clear();
        RESOURCE_JSON.putAll(json);
        resolveAll();
    }

    public static synchronized void setConfigJson(Map<Identifier, JsonObject> json) {
        CONFIG_JSON.clear();
        CONFIG_JSON.putAll(json);
        resolveAll();
    }

    private static void resolveAll() {
        RESOLVED.clear();
        Set<Identifier> all = new HashSet<>(CODE.keySet());
        all.addAll(RESOURCE_JSON.keySet());
        all.addAll(CONFIG_JSON.keySet());
        for (Identifier id : all) {
            resolve(id, new HashSet<>());
        }
        notifyListeners();
    }

    @Nullable
    private static Theme resolve(Identifier id, Set<Identifier> visiting) {
        Theme cached = RESOLVED.get(id);
        if (cached != null) {
            return cached;
        }
        if (!visiting.add(id)) {
            SaeLibVie.LOGGER.warn("Theme parent cycle detected at {}", id);
            return CODE.get(id);
        }
        Theme base = CODE.get(id);
        JsonObject resource = RESOURCE_JSON.get(id);
        if (resource != null) {
            Theme parent = base;
            if (resource.has("parent")) {
                Identifier parentId = Identifier.parse(resource.get("parent").getAsString());
                Theme resolvedParent = resolve(parentId, visiting);
                if (resolvedParent != null) {
                    parent = resolvedParent;
                }
            }
            try {
                base = Theme.fromJson(id, resource, parent);
            } catch (Exception e) {
                SaeLibVie.LOGGER.error("Failed to parse theme {} from resource pack", id, e);
            }
        }
        JsonObject config = CONFIG_JSON.get(id);
        if (config != null) {
            Theme parent = base;
            if (config.has("parent")) {
                Identifier parentId = Identifier.parse(config.get("parent").getAsString());
                Theme resolvedParent = resolve(parentId, visiting);
                if (resolvedParent != null) {
                    parent = resolvedParent;
                }
            }
            try {
                base = Theme.fromJson(id, config, parent);
            } catch (Exception e) {
                SaeLibVie.LOGGER.error("Failed to parse theme override {} from config", id, e);
            }
        }
        if (base == null) {
            visiting.remove(id);
            return null;
        }
        if (globalOpacity < 1f) {
            base = base.withOpacity(base.opacity() * globalOpacity);
        }
        RESOLVED.put(id, base);
        visiting.remove(id);
        return base;
    }

    private static void notifyListeners() {
        Theme current = get(defaultId);
        for (Consumer<Theme> listener : LISTENERS) {
            try {
                listener.accept(current);
            } catch (Exception e) {
                SaeLibVie.LOGGER.error("Theme listener failed", e);
            }
        }
    }
}

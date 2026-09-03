package com.breakinblocks.saelibvie.ui.hud;

import com.breakinblocks.saelibvie.SaeLibVie;
import com.breakinblocks.saelibvie.ui.color.Colors;
import com.breakinblocks.saelibvie.ui.color.Themes;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.geom.Anchor;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public final class HudRegistry {
    public record Placement(Anchor anchor, float fractionX, float fractionY, int offsetX, int offsetY, float scale, boolean enabled) {
        public static Placement at(float fractionX, float fractionY) {
            return new Placement(Anchor.TOP_LEFT, fractionX, fractionY, 0, 0, 1f, true);
        }

        public static Placement anchored(Anchor anchor, int offsetX, int offsetY) {
            return new Placement(anchor, anchor.fractionX(), anchor.fractionY(), offsetX, offsetY, 1f, true);
        }

        public Placement withPosition(float fx, float fy) {
            return new Placement(anchor, fx, fy, 0, 0, scale, enabled);
        }

        public Placement withEnabled(boolean value) {
            return new Placement(anchor, fractionX, fractionY, offsetX, offsetY, scale, value);
        }

        public Placement withScale(float value) {
            return new Placement(anchor, fractionX, fractionY, offsetX, offsetY, value, enabled);
        }

        public Rect resolve(int screenW, int screenH, int elementW, int elementH) {
            int w = Math.round(elementW * scale);
            int h = Math.round(elementH * scale);
            int px = Math.round(screenW * fractionX) + offsetX;
            int py = Math.round(screenH * fractionY) + offsetY;
            int x = px - Math.round(w * anchor.fractionX());
            int y = py - Math.round(h * anchor.fractionY());
            return new Rect(x, y, w, h);
        }

        public static Placement fromPixels(Anchor anchor, int x, int y, int w, int h, int screenW, int screenH, float scale, boolean enabled) {
            int px = x + Math.round(w * anchor.fractionX());
            int py = y + Math.round(h * anchor.fractionY());
            return new Placement(anchor, screenW <= 0 ? 0f : (float) px / screenW, screenH <= 0 ? 0f : (float) py / screenH, 0, 0, scale, enabled);
        }

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("anchor", anchor.name().toLowerCase(Locale.ROOT));
            json.addProperty("x", fractionX);
            json.addProperty("y", fractionY);
            json.addProperty("offset_x", offsetX);
            json.addProperty("offset_y", offsetY);
            json.addProperty("scale", scale);
            json.addProperty("enabled", enabled);
            return json;
        }

        static Placement fromJson(JsonObject json, Placement fallback) {
            Anchor anchor = json.has("anchor") ? Anchor.fromName(json.get("anchor").getAsString(), fallback.anchor) : fallback.anchor;
            float fx = json.has("x") ? json.get("x").getAsFloat() : fallback.fractionX;
            float fy = json.has("y") ? json.get("y").getAsFloat() : fallback.fractionY;
            int ox = json.has("offset_x") ? json.get("offset_x").getAsInt() : fallback.offsetX;
            int oy = json.has("offset_y") ? json.get("offset_y").getAsInt() : fallback.offsetY;
            float scale = json.has("scale") ? json.get("scale").getAsFloat() : fallback.scale;
            boolean enabled = !json.has("enabled") || json.get("enabled").getAsBoolean();
            return new Placement(anchor, fx, fy, ox, oy, scale, enabled);
        }
    }

    public static final class Entry {
        private final Identifier key;
        private final HudElement element;
        private final Placement defaults;
        private Placement placement;
        private final int editColor;

        Entry(Identifier key, HudElement element, Placement defaults, int editColor) {
            this.key = key;
            this.element = element;
            this.defaults = defaults;
            this.placement = defaults;
            this.editColor = editColor;
        }

        public Identifier key() {
            return key;
        }

        public HudElement element() {
            return element;
        }

        public Placement defaults() {
            return defaults;
        }

        public Placement placement() {
            return placement;
        }

        public void setPlacement(Placement placement) {
            this.placement = placement;
        }

        public void reset() {
            this.placement = defaults;
        }

        public int editColor() {
            return editColor;
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Random RANDOM = new Random();
    private static final Map<Identifier, Entry> ENTRIES = new LinkedHashMap<>();
    private static final UiRoot HUD_ROOT = new UiRoot(Themes.getDefault());
    private static boolean loaded;
    private static boolean editing;

    private HudRegistry() {
    }

    public static Path configFile() {
        return FMLPaths.CONFIGDIR.get().resolve(SaeLibVie.MOD_ID).resolve("hud.json");
    }

    public static synchronized Entry register(Identifier key, HudElement element, Placement defaults) {
        Entry existing = ENTRIES.remove(key);
        if (existing != null) {
            HUD_ROOT.remove(existing.element);
        }
        element.setKey(key);
        Entry entry = new Entry(key, element, defaults, randomEditColor());
        ENTRIES.put(key, entry);
        HUD_ROOT.add(element);
        if (loaded) {
            applyLoaded(entry);
        }
        return entry;
    }

    public static synchronized void unregister(Identifier key) {
        Entry entry = ENTRIES.remove(key);
        if (entry != null) {
            HUD_ROOT.remove(entry.element);
        }
    }

    @Nullable
    public static synchronized Entry get(Identifier key) {
        return ENTRIES.get(key);
    }

    public static synchronized List<Entry> entries() {
        return Collections.unmodifiableList(new ArrayList<>(ENTRIES.values()));
    }

    public static UiRoot hudRoot() {
        return HUD_ROOT;
    }

    public static boolean isEditing() {
        return editing;
    }

    public static synchronized void setEditing(boolean value) {
        if (editing == value) return;
        editing = value;
        for (Entry entry : ENTRIES.values()) {
            entry.element.onEditModeChanged(value);
        }
    }

    public static synchronized void resetAll() {
        for (Entry entry : ENTRIES.values()) {
            entry.reset();
        }
    }

    private static Map<Identifier, JsonObject> loadedJson = new LinkedHashMap<>();

    private static void applyLoaded(Entry entry) {
        JsonObject json = loadedJson.get(entry.key);
        if (json != null) {
            entry.placement = Placement.fromJson(json, entry.defaults);
        }
    }

    public static synchronized void load() {
        loaded = true;
        loadedJson = new LinkedHashMap<>();
        Path file = configFile();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                JsonElement root = JsonParser.parseReader(reader);
                if (root.isJsonObject()) {
                    for (Map.Entry<String, JsonElement> e : root.getAsJsonObject().entrySet()) {
                        if (e.getValue().isJsonObject()) {
                            loadedJson.put(Identifier.parse(e.getKey()), e.getValue().getAsJsonObject());
                        }
                    }
                }
            } catch (Exception e) {
                SaeLibVie.LOGGER.error("Failed to read HUD layout {}", file, e);
            }
        }
        for (Entry entry : ENTRIES.values()) {
            applyLoaded(entry);
        }
    }

    public static synchronized void save() {
        JsonObject root = new JsonObject();
        for (Entry entry : ENTRIES.values()) {
            root.add(entry.key.toString(), entry.placement.toJson());
        }
        for (Map.Entry<Identifier, JsonObject> e : loadedJson.entrySet()) {
            if (!root.has(e.getKey().toString())) {
                root.add(e.getKey().toString(), e.getValue());
            }
        }
        Path file = configFile();
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            SaeLibVie.LOGGER.error("Failed to save HUD layout {}", file, e);
        }
    }

    public static Rect resolve(Entry entry, int screenW, int screenH) {
        HudElement element = entry.element;
        return entry.placement.resolve(screenW, screenH, element.width(), element.height());
    }

    public static synchronized void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui || mc.getDebugOverlay().showDebugScreen() || editing) {
            return;
        }
        if (!loaded) load();
        int screenW = graphics.guiWidth();
        int screenH = graphics.guiHeight();
        HUD_ROOT.setTheme(Themes.getDefault());
        HUD_ROOT.setBounds(new Rect(0, 0, screenW, screenH));
        HUD_ROOT.setScreenSize(screenW, screenH);
        for (Entry entry : ENTRIES.values()) {
            HudElement element = entry.element;
            if (element.parent() != HUD_ROOT) {
                HUD_ROOT.add(element);
            }
            boolean visible = entry.placement.enabled() && element.shouldRender(mc);
            element.setVisible(visible);
            if (!visible) continue;
            Rect placed = resolve(entry, screenW, screenH);
            element.setPos(placed.x(), placed.y());
            element.setHudScale(entry.placement.scale());
        }
        HUD_ROOT.renderFrame(graphics, -1, -1, deltaTracker.getGameTimeDeltaPartialTick(false));
    }

    public static synchronized void tick() {
        if (editing) return;
        for (Entry entry : ENTRIES.values()) {
            if (entry.element.isVisible()) {
                entry.element.tick();
            }
        }
    }

    private static int randomEditColor() {
        float r = RANDOM.nextFloat() / 2f + 0.5f;
        float g = RANDOM.nextFloat() / 2f + 0.5f;
        float b = RANDOM.nextFloat() / 2f + 0.5f;
        return Colors.argb(0.5f, r, g, b);
    }
}

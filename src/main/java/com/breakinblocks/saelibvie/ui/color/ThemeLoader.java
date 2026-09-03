package com.breakinblocks.saelibvie.ui.color;

import com.breakinblocks.saelibvie.SaeLibVie;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ThemeLoader extends SimpleJsonResourceReloadListener<JsonElement> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final String FOLDER = "saelibvie/themes";

    public ThemeLoader() {
        super(ExtraCodecs.JSON, FileToIdConverter.json(FOLDER));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, JsonObject> themes = new LinkedHashMap<>();
        map.forEach((id, element) -> {
            if (element.isJsonObject()) {
                themes.put(id, element.getAsJsonObject());
            }
        });
        Themes.setResourceJson(themes);
        loadConfig();
        SaeLibVie.LOGGER.info("Loaded {} theme definitions from resource packs, {} themes available", themes.size(), Themes.ids().size());
    }

    public static Path configFile() {
        return FMLPaths.CONFIGDIR.get().resolve(SaeLibVie.MOD_ID).resolve("themes.json");
    }

    public static void loadConfig() {
        Path file = configFile();
        if (!Files.exists(file)) {
            writeDefaultConfig(file);
            return;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                return;
            }
            JsonObject obj = root.getAsJsonObject();
            Map<Identifier, JsonObject> overrides = new LinkedHashMap<>();
            if (obj.has("overrides") && obj.get("overrides").isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("overrides").entrySet()) {
                    if (entry.getValue().isJsonObject()) {
                        overrides.put(Identifier.parse(entry.getKey()), entry.getValue().getAsJsonObject());
                    }
                }
            }
            if (obj.has("opacity")) {
                Themes.setGlobalOpacity(obj.get("opacity").getAsFloat());
            }
            Themes.setConfigJson(overrides);
            if (obj.has("default")) {
                Themes.setDefault(Identifier.parse(obj.get("default").getAsString()));
            }
        } catch (Exception e) {
            SaeLibVie.LOGGER.error("Failed to read theme config {}", file, e);
        }
    }

    private static void writeDefaultConfig(Path file) {
        JsonObject root = new JsonObject();
        root.addProperty("default", Themes.getDefaultId().toString());
        root.addProperty("opacity", 1.0f);
        JsonObject overrides = new JsonObject();
        JsonObject example = new JsonObject();
        example.addProperty("parent", Themes.DARK_ID.toString());
        JsonObject colors = new JsonObject();
        colors.addProperty(ColorToken.ACCENT.name(), Colors.toHex(Themes.DARK.color(ColorToken.ACCENT)));
        example.add("colors", colors);
        overrides.add("saelibvie:example", example);
        root.add("overrides", overrides);
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            SaeLibVie.LOGGER.error("Failed to write default theme config {}", file, e);
        }
    }
}

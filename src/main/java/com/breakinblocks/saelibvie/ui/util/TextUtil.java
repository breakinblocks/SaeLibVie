package com.breakinblocks.saelibvie.ui.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TextUtil {
    private static final DecimalFormat GROUPED = new DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private TextUtil() {
    }

    public static Font font() {
        return Minecraft.getInstance().font;
    }

    public static String abbreviate(long value) {
        if (Math.abs(value) >= 1_000_000_000L) {
            return trim(value / 1_000_000_000.0) + "G";
        }
        if (Math.abs(value) >= 1_000_000L) {
            return trim(value / 1_000_000.0) + "M";
        }
        if (Math.abs(value) >= 1_000L) {
            return trim(value / 1_000.0) + "k";
        }
        return Long.toString(value);
    }

    private static String trim(double value) {
        if (value >= 100) return String.format(Locale.ROOT, "%.0f", value);
        if (value >= 10) return String.format(Locale.ROOT, "%.1f", value);
        return String.format(Locale.ROOT, "%.2f", value);
    }

    public static String grouped(long value) {
        synchronized (GROUPED) {
            return GROUPED.format(value);
        }
    }

    public static String decimal(double value, int places) {
        return String.format(Locale.ROOT, "%." + places + "f", value);
    }

    public static String compactDecimal(double value) {
        if (value == Math.rint(value)) {
            return Long.toString((long) value);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    public static String percent(double fraction) {
        return Math.round(fraction * 100.0) + "%";
    }

    public static String duration(int seconds) {
        if (seconds < 60) return seconds + "s";
        if (seconds < 3600) {
            int minutes = seconds / 60;
            int rest = seconds % 60;
            return rest == 0 ? minutes + "m" : minutes + "m " + rest + "s";
        }
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        return minutes == 0 ? hours + "h" : hours + "h " + minutes + "m";
    }

    public static String ticksToDuration(int ticks) {
        return duration(Math.max(0, ticks) / 20);
    }

    public static String prettify(String identifierPath) {
        String[] parts = identifierPath.replace('/', '_').split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) out.append(part.substring(1));
        }
        return out.toString();
    }

    public static Component prettify(Identifier id) {
        return Component.literal(prettify(id.getPath()));
    }

    public static Component translatableOrPretty(String key, Identifier id) {
        return Component.translatableWithFallback(key, prettify(id.getPath()));
    }

    public static MutableComponent hotkey(String text) {
        return Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(text).withStyle(ChatFormatting.GRAY))
                .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static MutableComponent hotkey(Component text) {
        return Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
                .append(text.copy().withStyle(ChatFormatting.GRAY))
                .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static MutableComponent joinLines(Component... lines) {
        MutableComponent root = Component.empty();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) root.append("\n");
            root.append(lines[i]);
        }
        return root;
    }

    public static List<Component> wrapWords(String text, int maxWidth, ChatFormatting... styles) {
        Font font = font();
        List<Component> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (font.width(test) > maxWidth && line.length() > 0) {
                lines.add(Component.literal(line.toString()).withStyle(styles));
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) {
            lines.add(Component.literal(line.toString()).withStyle(styles));
        }
        return lines;
    }

    public static String repeat(String glyph, int count) {
        return glyph.repeat(Math.max(0, count));
    }

    public static float fitScale(Font font, String text, int availableWidth) {
        int width = font.width(text);
        if (width <= availableWidth || width == 0) return 1f;
        return (float) availableWidth / width;
    }
}

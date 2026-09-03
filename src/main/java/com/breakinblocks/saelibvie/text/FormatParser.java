package com.breakinblocks.saelibvie.text;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class FormatParser {
    public static final Map<Character, TextColor> SPECIAL_CODES = new HashMap<>();
    private static final List<BiFunction<String, Map<String, String>, @Nullable Component>> CUSTOM_PARSERS = new ArrayList<>();

    private FormatParser() {
    }

    public static void addCustomParser(BiFunction<String, Map<String, String>, @Nullable Component> parser) {
        CUSTOM_PARSERS.add(parser);
    }

    private static final class ParseException extends RuntimeException {
        ParseException(String message) {
            super(message);
        }
    }

    public static Component parse(String text, @Nullable Function<String, @Nullable Component> substitutes) {
        text = text.replace("\\n", "\n");
        if (text.indexOf('{') < 0 && text.indexOf('&') < 0 && text.indexOf(StringUtil.FORMATTING_CHAR) < 0) {
            return Component.literal(text);
        }
        try {
            return unwrap(parse0(text, substitutes));
        } catch (Exception e) {
            String message = e instanceof ParseException ? e.getMessage() : e.toString();
            return Component.literal("Invalid formatting! " + message).withStyle(ChatFormatting.RED);
        }
    }

    private static MutableComponent parse0(String text, @Nullable Function<String, @Nullable Component> substitutes) {
        MutableComponent out = Component.literal("");
        Style style = Style.EMPTY;
        StringBuilder buffer = new StringBuilder();
        boolean substituting = false;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            boolean escaped = i > 0 && chars[i - 1] == '\\';
            if (c == '\\' && !escaped) {
                continue;
            }
            if (!escaped && (c == '&' || c == StringUtil.FORMATTING_CHAR)) {
                flush(out, buffer, style, substitutes, substituting);
                substituting = false;
                if (i == chars.length - 1) {
                    throw new ParseException("Can't end string with &!");
                }
                char code = chars[++i];
                if (code == '#') {
                    String hex = i + 6 < chars.length ? new String(chars, i + 1, 6) : "";
                    TextColor color = TextColor.parseColor("#" + hex).result().orElse(TextColor.fromRgb(0xFFFFFF));
                    style = style.withColor(color);
                    i += 6;
                } else if (code == ' ') {
                    throw new ParseException("You must escape whitespace after & with \\&!");
                } else if (SPECIAL_CODES.containsKey(code)) {
                    style = style.withColor(SPECIAL_CODES.get(code));
                } else {
                    ChatFormatting formatting = ChatFormatting.getByCode(code);
                    if (formatting == null || Character.isUpperCase(code)) {
                        throw new ParseException("Unknown formatting symbol after &: '" + code + "'!");
                    }
                    style = style.applyFormat(formatting);
                }
                continue;
            }
            if (!escaped && c == '{') {
                if (substituting) {
                    throw new ParseException("Can't nest multiple substitutes!");
                }
                flush(out, buffer, style, substitutes, false);
                if (i == chars.length - 1) {
                    throw new ParseException("Can't end string with {!");
                }
                substituting = true;
                buffer.append('{');
                continue;
            }
            if (!escaped && c == '}' && substituting) {
                flush(out, buffer, style, substitutes, true);
                substituting = false;
                continue;
            }
            buffer.append(c);
        }
        flush(out, buffer, style, substitutes, substituting);
        return out;
    }

    private static void flush(MutableComponent out, StringBuilder buffer, Style style, @Nullable Function<String, @Nullable Component> substitutes, boolean substituting) {
        if (buffer.isEmpty()) return;
        String text = buffer.toString();
        buffer.setLength(0);
        if (!substituting || text.length() < 2 || text.charAt(0) != '{') {
            out.append(Component.literal(text).withStyle(style));
            return;
        }
        String name = text.substring(1);
        Component substituted = substitutes == null ? null : substitutes.apply(name);
        if (substituted == null) {
            throw new ParseException("Unknown substitute: " + name);
        }
        Style merged = style;
        Style inner = substituted.getStyle();
        if (inner.getHoverEvent() != null) merged = merged.withHoverEvent(inner.getHoverEvent());
        if (inner.getClickEvent() != null) merged = merged.withClickEvent(inner.getClickEvent());
        if (inner.getInsertion() != null) merged = merged.withInsertion(inner.getInsertion());
        out.append(Component.literal("").append(substituted).withStyle(merged));
    }

    private static Component unwrap(MutableComponent component) {
        Component current = component;
        while (current.getContents() instanceof PlainTextContents plain && plain.text().isEmpty()
                && current.getStyle().isEmpty() && current.getSiblings().size() == 1) {
            current = current.getSiblings().get(0);
        }
        return current;
    }

    public static Component parseClient(String text) {
        return parse(text, FormatParser::defaultSubstitute);
    }

    @Nullable
    private static Component defaultSubstitute(String name) {
        if (name.isEmpty()) return Component.empty();
        if (name.indexOf(':') >= 0) {
            Map<String, String> properties = StringUtil.splitProperties(name);
            for (var parser : CUSTOM_PARSERS) {
                Component result = parser.apply(name, properties);
                if (result != null && !result.getString().isEmpty()) {
                    return result;
                }
            }
            String url = properties.get("open_url");
            if (url != null) {
                return parseClient(url).copy().withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
            }
        }
        return parse(I18n.get(name), FormatParser::defaultSubstitute);
    }

    public static String ellipsize(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        if (ellipsisWidth > maxWidth) {
            return font.plainSubstrByWidth(text, maxWidth);
        }
        return font.plainSubstrByWidth(text, maxWidth - ellipsisWidth) + ellipsis;
    }
}

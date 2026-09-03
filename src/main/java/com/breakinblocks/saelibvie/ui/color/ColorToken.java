package com.breakinblocks.saelibvie.ui.color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ColorToken(String name) {
    private static final Map<String, ColorToken> KNOWN = new LinkedHashMap<>();

    public static final ColorToken WINDOW_BG = register("window_bg");
    public static final ColorToken PANEL_BG = register("panel_bg");
    public static final ColorToken INSET_BG = register("inset_bg");
    public static final ColorToken HEADER_BG = register("header_bg");
    public static final ColorToken BORDER_OUT = register("border_out");
    public static final ColorToken BORDER_IN = register("border_in");
    public static final ColorToken BORDER_SOFT = register("border_soft");
    public static final ColorToken TEXT = register("text");
    public static final ColorToken TEXT_DIM = register("text_dim");
    public static final ColorToken TEXT_TITLE = register("text_title");
    public static final ColorToken TEXT_DISABLED = register("text_disabled");
    public static final ColorToken ACCENT = register("accent");
    public static final ColorToken ACCENT_DIM = register("accent_dim");
    public static final ColorToken POSITIVE = register("positive");
    public static final ColorToken WARNING = register("warning");
    public static final ColorToken NEGATIVE = register("negative");
    public static final ColorToken HOVER = register("hover");
    public static final ColorToken SELECTED = register("selected");
    public static final ColorToken FOCUS = register("focus");
    public static final ColorToken BUTTON_BG = register("button_bg");
    public static final ColorToken BUTTON_HOVER = register("button_hover");
    public static final ColorToken BUTTON_DISABLED = register("button_disabled");
    public static final ColorToken SCROLL_TRACK = register("scroll_track");
    public static final ColorToken SCROLL_THUMB = register("scroll_thumb");
    public static final ColorToken SLOT_BG = register("slot_bg");
    public static final ColorToken SLOT_BORDER = register("slot_border");
    public static final ColorToken TOOLTIP_BG = register("tooltip_bg");
    public static final ColorToken TOOLTIP_BORDER = register("tooltip_border");
    public static final ColorToken OVERLAY_DIM = register("overlay_dim");
    public static final ColorToken PROGRESS_BG = register("progress_bg");
    public static final ColorToken PROGRESS_FILL = register("progress_fill");
    public static final ColorToken PROGRESS_EDGE = register("progress_edge");
    public static final ColorToken GRAPH_LINE = register("graph_line");
    public static final ColorToken GRAPH_FILL = register("graph_fill");
    public static final ColorToken TITLE_BAR = register("title_bar");
    public static final ColorToken TITLE_BAR_ACTIVE = register("title_bar_active");
    public static final ColorToken RESIZE_GRIP = register("resize_grip");

    public static ColorToken register(String name) {
        synchronized (KNOWN) {
            return KNOWN.computeIfAbsent(name, ColorToken::new);
        }
    }

    public static ColorToken of(String name) {
        return register(name);
    }

    public static Collection<ColorToken> known() {
        synchronized (KNOWN) {
            return Collections.unmodifiableCollection(new ArrayList<>(KNOWN.values()));
        }
    }

    @Override
    public String toString() {
        return name;
    }
}

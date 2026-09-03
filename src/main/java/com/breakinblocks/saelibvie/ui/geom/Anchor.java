package com.breakinblocks.saelibvie.ui.geom;

public enum Anchor {
    TOP_LEFT(Align.START, Align.START),
    TOP_CENTER(Align.CENTER, Align.START),
    TOP_RIGHT(Align.END, Align.START),
    CENTER_LEFT(Align.START, Align.CENTER),
    CENTER(Align.CENTER, Align.CENTER),
    CENTER_RIGHT(Align.END, Align.CENTER),
    BOTTOM_LEFT(Align.START, Align.END),
    BOTTOM_CENTER(Align.CENTER, Align.END),
    BOTTOM_RIGHT(Align.END, Align.END);

    private final Align horizontal;
    private final Align vertical;

    Anchor(Align horizontal, Align vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public Align horizontal() {
        return horizontal;
    }

    public Align vertical() {
        return vertical;
    }

    public float fractionX() {
        return switch (horizontal) {
            case START -> 0f;
            case CENTER -> 0.5f;
            case END -> 1f;
        };
    }

    public float fractionY() {
        return switch (vertical) {
            case START -> 0f;
            case CENTER -> 0.5f;
            case END -> 1f;
        };
    }

    public Rect place(Rect container, int w, int h, int offsetX, int offsetY) {
        return container.align(w, h, this).offset(offsetX, offsetY);
    }

    public static Anchor fromName(String name, Anchor fallback) {
        for (Anchor anchor : values()) {
            if (anchor.name().equalsIgnoreCase(name)) {
                return anchor;
            }
        }
        return fallback;
    }
}

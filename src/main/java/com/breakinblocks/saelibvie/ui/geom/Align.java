package com.breakinblocks.saelibvie.ui.geom;

public enum Align {
    START,
    CENTER,
    END;

    public int offset(int available, int size) {
        return switch (this) {
            case START -> 0;
            case CENTER -> (available - size) / 2;
            case END -> available - size;
        };
    }

    public static Align fromName(String name, Align fallback) {
        for (Align align : values()) {
            if (align.name().equalsIgnoreCase(name)) {
                return align;
            }
        }
        return fallback;
    }
}

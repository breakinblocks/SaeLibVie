package com.breakinblocks.saelibvie.ui.geom;

public record Size(int w, int h) {
    public static final Size ZERO = new Size(0, 0);

    public static Size of(int w, int h) {
        return new Size(w, h);
    }

    public Size plus(int dw, int dh) {
        return new Size(w + dw, h + dh);
    }

    public Size max(Size other) {
        return new Size(Math.max(w, other.w), Math.max(h, other.h));
    }

    public int along(Axis axis) {
        return axis.isHorizontal() ? w : h;
    }

    public int across(Axis axis) {
        return axis.isHorizontal() ? h : w;
    }
}

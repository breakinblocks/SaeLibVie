package com.breakinblocks.saelibvie.ui.geom;

public enum Axis {
    HORIZONTAL,
    VERTICAL;

    public Axis cross() {
        return this == HORIZONTAL ? VERTICAL : HORIZONTAL;
    }

    public boolean isHorizontal() {
        return this == HORIZONTAL;
    }
}

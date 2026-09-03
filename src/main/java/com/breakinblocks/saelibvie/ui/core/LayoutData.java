package com.breakinblocks.saelibvie.ui.core;

import com.breakinblocks.saelibvie.ui.geom.Align;
import com.breakinblocks.saelibvie.ui.geom.Anchor;
import com.breakinblocks.saelibvie.ui.geom.Insets;

public final class LayoutData {
    private Anchor anchor = Anchor.TOP_LEFT;
    private int offsetX;
    private int offsetY;
    private float relativeX = Float.NaN;
    private float relativeY = Float.NaN;
    private float relativeW = Float.NaN;
    private float relativeH = Float.NaN;
    private int fixedW = -1;
    private int fixedH = -1;
    private float weight;
    private boolean fill;
    private Align crossAlign = Align.START;
    private Insets margin = Insets.NONE;
    private int column = -1;
    private int row = -1;
    private int columnSpan = 1;
    private int rowSpan = 1;
    private boolean lineBreak;
    private boolean explicit;

    public static LayoutData create() {
        return new LayoutData();
    }

    public static LayoutData anchored(Anchor anchor) {
        return new LayoutData().anchor(anchor);
    }

    public static LayoutData anchored(Anchor anchor, int offsetX, int offsetY) {
        return new LayoutData().anchor(anchor).offset(offsetX, offsetY);
    }

    public static LayoutData weighted(float weight) {
        return new LayoutData().weight(weight);
    }

    public static LayoutData filled() {
        return new LayoutData().fill(true);
    }

    public static LayoutData cell(int column, int row) {
        return new LayoutData().at(column, row);
    }

    public LayoutData anchor(Anchor anchor) {
        this.explicit = true;
        this.anchor = anchor;
        return this;
    }

    public LayoutData offset(int x, int y) {
        this.explicit = true;
        this.offsetX = x;
        this.offsetY = y;
        return this;
    }

    public LayoutData relativePos(float x, float y) {
        this.explicit = true;
        this.relativeX = x;
        this.relativeY = y;
        return this;
    }

    public LayoutData relativeSize(float w, float h) {
        this.explicit = true;
        this.relativeW = w;
        this.relativeH = h;
        return this;
    }

    public LayoutData size(int w, int h) {
        this.explicit = true;
        this.fixedW = w;
        this.fixedH = h;
        return this;
    }

    public LayoutData width(int w) {
        this.explicit = true;
        this.fixedW = w;
        return this;
    }

    public LayoutData height(int h) {
        this.explicit = true;
        this.fixedH = h;
        return this;
    }

    public LayoutData weight(float weight) {
        this.explicit = true;
        this.weight = weight;
        return this;
    }

    public LayoutData fill(boolean fill) {
        this.explicit = true;
        this.fill = fill;
        return this;
    }

    public LayoutData crossAlign(Align align) {
        this.explicit = true;
        this.crossAlign = align;
        return this;
    }

    public LayoutData margin(Insets margin) {
        this.explicit = true;
        this.margin = margin;
        return this;
    }

    public LayoutData margin(int all) {
        this.explicit = true;
        this.margin = Insets.all(all);
        return this;
    }

    public LayoutData at(int column, int row) {
        this.explicit = true;
        this.column = column;
        this.row = row;
        return this;
    }

    public LayoutData span(int columns, int rows) {
        this.explicit = true;
        this.columnSpan = Math.max(1, columns);
        this.rowSpan = Math.max(1, rows);
        return this;
    }

    public LayoutData lineBreak(boolean lineBreak) {
        this.explicit = true;
        this.lineBreak = lineBreak;
        return this;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public Anchor anchor() {
        return anchor;
    }

    public int offsetX() {
        return offsetX;
    }

    public int offsetY() {
        return offsetY;
    }

    public boolean hasRelativePos() {
        return !Float.isNaN(relativeX) && !Float.isNaN(relativeY);
    }

    public float relativeX() {
        return relativeX;
    }

    public float relativeY() {
        return relativeY;
    }

    public boolean hasRelativeW() {
        return !Float.isNaN(relativeW);
    }

    public boolean hasRelativeH() {
        return !Float.isNaN(relativeH);
    }

    public float relativeW() {
        return relativeW;
    }

    public float relativeH() {
        return relativeH;
    }

    public int fixedW() {
        return fixedW;
    }

    public int fixedH() {
        return fixedH;
    }

    public float weight() {
        return weight;
    }

    public boolean fill() {
        return fill;
    }

    public Align crossAlign() {
        return crossAlign;
    }

    public Insets margin() {
        return margin;
    }

    public int column() {
        return column;
    }

    public int row() {
        return row;
    }

    public int columnSpan() {
        return columnSpan;
    }

    public int rowSpan() {
        return rowSpan;
    }

    public boolean lineBreak() {
        return lineBreak;
    }
}

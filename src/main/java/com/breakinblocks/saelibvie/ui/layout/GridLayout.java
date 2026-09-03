package com.breakinblocks.saelibvie.ui.layout;

import com.breakinblocks.saelibvie.ui.core.Layout;
import com.breakinblocks.saelibvie.ui.core.LayoutData;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;

public final class GridLayout implements Layout {
    private final int columns;
    private final int rows;
    private final int cellW;
    private final int cellH;
    private int gapX;
    private int gapY;

    private GridLayout(int columns, int rows, int cellW, int cellH, int gapX, int gapY) {
        this.columns = Math.max(1, columns);
        this.rows = rows;
        this.cellW = cellW;
        this.cellH = cellH;
        this.gapX = gapX;
        this.gapY = gapY;
    }

    public static GridLayout fixed(int columns, int cellW, int cellH) {
        return new GridLayout(columns, 0, cellW, cellH, 0, 0);
    }

    public static GridLayout fixed(int columns, int cellW, int cellH, int gapX, int gapY) {
        return new GridLayout(columns, 0, cellW, cellH, gapX, gapY);
    }

    public static GridLayout split(int columns, int rows) {
        return new GridLayout(columns, Math.max(1, rows), -1, -1, 0, 0);
    }

    public static GridLayout split(int columns, int rows, int gapX, int gapY) {
        return new GridLayout(columns, Math.max(1, rows), -1, -1, gapX, gapY);
    }

    public static GridLayout slots(int columns) {
        return new GridLayout(columns, 0, 18, 18, 0, 0);
    }

    public GridLayout gap(int gapX, int gapY) {
        this.gapX = gapX;
        this.gapY = gapY;
        return this;
    }

    private boolean isSplit() {
        return cellW < 0;
    }

    private int cellWidth(Rect content) {
        if (!isSplit()) return cellW;
        return Math.max(0, (content.w() - gapX * (columns - 1)) / columns);
    }

    private int cellHeight(Rect content) {
        if (!isSplit()) return cellH;
        return Math.max(0, (content.h() - gapY * (rows - 1)) / rows);
    }

    @Override
    public void apply(Panel panel, Rect content) {
        int cw = cellWidth(content);
        int ch = cellHeight(content);
        int autoIndex = 0;
        for (Widget child : panel.children()) {
            if (!child.isVisible()) continue;
            LayoutData data = child.layoutData();
            int col;
            int row;
            if (data.column() >= 0 && data.row() >= 0) {
                col = data.column();
                row = data.row();
            } else {
                col = autoIndex % columns;
                row = autoIndex / columns;
                autoIndex += data.columnSpan();
            }
            int spanW = cw * data.columnSpan() + gapX * (data.columnSpan() - 1);
            int spanH = ch * data.rowSpan() + gapY * (data.rowSpan() - 1);
            int x = content.x() + col * (cw + gapX);
            int y = content.y() + row * (ch + gapY);
            Rect cell = new Rect(x, y, spanW, spanH).inset(data.margin());
            if (data.fill() || (data.fixedW() < 0 && data.fixedH() < 0 && child.preferredSize().w() <= 0)) {
                child.setBounds(cell);
            } else {
                Size size = Layout.childSize(child);
                int w = data.fixedW() >= 0 ? data.fixedW() : Math.min(size.w(), cell.w());
                int h = data.fixedH() >= 0 ? data.fixedH() : Math.min(size.h(), cell.h());
                child.setBounds(data.anchor().place(cell, w, h, data.offsetX(), data.offsetY()));
            }
        }
    }

    @Override
    public Size measure(Panel panel) {
        int count = 0;
        for (Widget child : panel.children()) {
            if (child.isVisible()) count += Math.max(1, child.layoutData().columnSpan());
        }
        if (count == 0) return Size.ZERO;
        int usedRows = isSplit() ? rows : (count + columns - 1) / columns;
        int cw = isSplit() ? 0 : cellW;
        int ch = isSplit() ? 0 : cellH;
        return new Size(columns * cw + gapX * (columns - 1), usedRows * ch + gapY * Math.max(0, usedRows - 1));
    }
}

package com.breakinblocks.saelibvie.ui.layout;

import com.breakinblocks.saelibvie.ui.core.Layout;
import com.breakinblocks.saelibvie.ui.core.LayoutData;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Align;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;

import java.util.ArrayList;
import java.util.List;

public final class FlowLayout implements Layout {
    private int gapX;
    private int gapY;
    private Align lineAlign = Align.START;

    private FlowLayout(int gapX, int gapY) {
        this.gapX = gapX;
        this.gapY = gapY;
    }

    public static FlowLayout create() {
        return new FlowLayout(2, 2);
    }

    public static FlowLayout create(int gapX, int gapY) {
        return new FlowLayout(gapX, gapY);
    }

    public FlowLayout gap(int gapX, int gapY) {
        this.gapX = gapX;
        this.gapY = gapY;
        return this;
    }

    public FlowLayout lineAlign(Align align) {
        this.lineAlign = align;
        return this;
    }

    @Override
    public void apply(Panel panel, Rect content) {
        List<Widget> line = new ArrayList<>();
        int lineWidth = 0;
        int lineHeight = 0;
        int y = content.y();
        for (Widget child : panel.children()) {
            if (!child.isVisible()) continue;
            LayoutData data = child.layoutData();
            Size size = Layout.childSize(child);
            int w = size.w() + data.margin().horizontal();
            int h = size.h() + data.margin().vertical();
            boolean overflow = !line.isEmpty() && (lineWidth + gapX + w > content.w() || child.layoutData().lineBreak());
            if (overflow) {
                placeLine(line, content, y, lineWidth, lineHeight);
                y += lineHeight + gapY;
                line.clear();
                lineWidth = 0;
                lineHeight = 0;
            }
            if (!line.isEmpty()) lineWidth += gapX;
            lineWidth += w;
            lineHeight = Math.max(lineHeight, h);
            line.add(child);
        }
        if (!line.isEmpty()) {
            placeLine(line, content, y, lineWidth, lineHeight);
        }
    }

    private void placeLine(List<Widget> line, Rect content, int y, int lineWidth, int lineHeight) {
        int x = content.x() + lineAlign.offset(content.w(), lineWidth);
        for (Widget child : line) {
            LayoutData data = child.layoutData();
            Size size = Layout.childSize(child);
            int h = data.fill() ? lineHeight - data.margin().vertical() : size.h();
            int offsetY = data.crossAlign().offset(lineHeight - data.margin().vertical(), h);
            child.setBounds(new Rect(x + data.margin().left(), y + data.margin().top() + offsetY, size.w(), h));
            x += size.w() + data.margin().horizontal() + gapX;
        }
    }

    @Override
    public Size measure(Panel panel) {
        int width = 0;
        int height = 0;
        int count = 0;
        for (Widget child : panel.children()) {
            if (!child.isVisible()) continue;
            Size size = Layout.childSize(child);
            LayoutData data = child.layoutData();
            width += size.w() + data.margin().horizontal();
            height = Math.max(height, size.h() + data.margin().vertical());
            count++;
        }
        if (count > 1) width += gapX * (count - 1);
        return new Size(width, height);
    }
}

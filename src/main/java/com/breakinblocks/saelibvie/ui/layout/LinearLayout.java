package com.breakinblocks.saelibvie.ui.layout;

import com.breakinblocks.saelibvie.ui.core.Layout;
import com.breakinblocks.saelibvie.ui.core.LayoutData;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Align;
import com.breakinblocks.saelibvie.ui.geom.Axis;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;

import java.util.ArrayList;
import java.util.List;

public final class LinearLayout implements Layout {
    private final Axis axis;
    private int gap;
    private Align mainAlign = Align.START;
    private Align crossAlign = Align.START;
    private boolean fillCross;

    private LinearLayout(Axis axis, int gap) {
        this.axis = axis;
        this.gap = gap;
    }

    public static LinearLayout vertical() {
        return new LinearLayout(Axis.VERTICAL, 2);
    }

    public static LinearLayout vertical(int gap) {
        return new LinearLayout(Axis.VERTICAL, gap);
    }

    public static LinearLayout horizontal() {
        return new LinearLayout(Axis.HORIZONTAL, 2);
    }

    public static LinearLayout horizontal(int gap) {
        return new LinearLayout(Axis.HORIZONTAL, gap);
    }

    public LinearLayout gap(int gap) {
        this.gap = gap;
        return this;
    }

    public LinearLayout mainAlign(Align align) {
        this.mainAlign = align;
        return this;
    }

    public LinearLayout crossAlign(Align align) {
        this.crossAlign = align;
        return this;
    }

    public LinearLayout fillCross(boolean fill) {
        this.fillCross = fill;
        return this;
    }

    public Axis axis() {
        return axis;
    }

    @Override
    public void apply(Panel panel, Rect content) {
        List<Widget> visible = new ArrayList<>();
        for (Widget child : panel.children()) {
            if (child.isVisible()) visible.add(child);
        }
        if (visible.isEmpty()) return;

        int available = content.w();
        int crossAvailable = content.h();
        if (axis == Axis.VERTICAL) {
            available = content.h();
            crossAvailable = content.w();
        }

        int fixedTotal = 0;
        float weightTotal = 0f;
        int[] mainSizes = new int[visible.size()];
        for (int i = 0; i < visible.size(); i++) {
            Widget child = visible.get(i);
            LayoutData data = child.layoutData();
            int marginMain = axis == Axis.HORIZONTAL ? data.margin().horizontal() : data.margin().vertical();
            if (data.weight() > 0f) {
                weightTotal += data.weight();
                mainSizes[i] = 0;
            } else {
                int crossMargin = axis == Axis.HORIZONTAL ? data.margin().vertical() : data.margin().horizontal();
                boolean fill = fillCross || data.fill();
                int crossSize = fill ? crossAvailable - crossMargin : Layout.childSize(child).across(axis);
                mainSizes[i] = Layout.childMain(child, axis, crossSize);
            }
            fixedTotal += mainSizes[i] + marginMain;
        }
        int gaps = gap * (visible.size() - 1);
        int leftover = Math.max(0, available - fixedTotal - gaps);
        if (weightTotal > 0f) {
            int distributed = 0;
            for (int i = 0; i < visible.size(); i++) {
                LayoutData data = visible.get(i).layoutData();
                if (data.weight() > 0f) {
                    int share = Math.round(leftover * (data.weight() / weightTotal));
                    mainSizes[i] = share;
                    distributed += share;
                }
            }
            leftover -= distributed;
            if (leftover != 0) {
                for (int i = visible.size() - 1; i >= 0; i--) {
                    if (visible.get(i).layoutData().weight() > 0f) {
                        mainSizes[i] += leftover;
                        break;
                    }
                }
            }
            leftover = 0;
        }

        int cursor = mainAlign.offset(available, available - leftover);
        for (int i = 0; i < visible.size(); i++) {
            Widget child = visible.get(i);
            LayoutData data = child.layoutData();
            Size size = Layout.childSize(child);
            int crossSize = size.across(axis);
            int crossMargin = axis == Axis.HORIZONTAL ? data.margin().vertical() : data.margin().horizontal();
            boolean fill = fillCross || data.fill();
            if (fill) {
                crossSize = crossAvailable - crossMargin;
            }
            Align align = data.fill() ? Align.START : data.crossAlign() != Align.START ? data.crossAlign() : crossAlign;
            int crossOffset = align.offset(crossAvailable - crossMargin, crossSize);
            if (axis == Axis.HORIZONTAL) {
                int x = content.x() + cursor + data.margin().left();
                int y = content.y() + data.margin().top() + crossOffset;
                child.setBounds(new Rect(x, y, mainSizes[i], crossSize));
                cursor += mainSizes[i] + data.margin().horizontal() + gap;
            } else {
                int x = content.x() + data.margin().left() + crossOffset;
                int y = content.y() + cursor + data.margin().top();
                child.setBounds(new Rect(x, y, crossSize, mainSizes[i]));
                cursor += mainSizes[i] + data.margin().vertical() + gap;
            }
        }
    }

    @Override
    public Size measure(Panel panel) {
        int main = 0;
        int cross = 0;
        int count = 0;
        for (Widget child : panel.children()) {
            if (!child.isVisible()) continue;
            LayoutData data = child.layoutData();
            Size size = Layout.childSize(child);
            int marginMain = axis == Axis.HORIZONTAL ? data.margin().horizontal() : data.margin().vertical();
            int marginCross = axis == Axis.HORIZONTAL ? data.margin().vertical() : data.margin().horizontal();
            main += size.along(axis) + marginMain;
            cross = Math.max(cross, size.across(axis) + marginCross);
            count++;
        }
        if (count > 1) main += gap * (count - 1);
        return axis == Axis.HORIZONTAL ? new Size(main, cross) : new Size(cross, main);
    }
}

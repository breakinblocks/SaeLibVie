package com.breakinblocks.saelibvie.ui.core;

import com.breakinblocks.saelibvie.ui.geom.Axis;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;

public interface Layout {
    void apply(Panel panel, Rect content);

    default Size measure(Panel panel) {
        Rect union = Rect.EMPTY;
        for (Widget child : panel.children()) {
            if (!child.isVisible()) continue;
            Rect r = child.bounds();
            if (r.isEmpty()) {
                Size p = child.preferredSize();
                r = r.sized(p.w(), p.h());
            }
            union = union.union(r);
        }
        return new Size(union.right(), union.bottom());
    }

    static int childMain(Widget child, Axis axis, int crossSize) {
        LayoutData data = child.layoutData();
        int fixed = axis == Axis.HORIZONTAL ? data.fixedW() : data.fixedH();
        if (fixed >= 0) return fixed;
        int measured = child.measureAlong(axis, crossSize);
        return measured > 0 ? measured : childSize(child).along(axis);
    }

    static Size childSize(Widget child) {
        LayoutData data = child.layoutData();
        Size preferred = child.preferredSize();
        int w = data.fixedW() >= 0 ? data.fixedW() : preferred.w() > 0 ? preferred.w() : child.width();
        int h = data.fixedH() >= 0 ? data.fixedH() : preferred.h() > 0 ? preferred.h() : child.height();
        return new Size(w, h);
    }
}

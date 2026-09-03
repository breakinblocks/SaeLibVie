package com.breakinblocks.saelibvie.ui.layout;

import com.breakinblocks.saelibvie.ui.core.Layout;
import com.breakinblocks.saelibvie.ui.core.LayoutData;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;

public final class AnchorLayout implements Layout {
    public static final AnchorLayout INSTANCE = new AnchorLayout();

    private AnchorLayout() {
    }

    @Override
    public void apply(Panel panel, Rect content) {
        for (Widget child : panel.children()) {
            if (!child.isVisible()) continue;
            LayoutData data = child.layoutData();
            if (!data.isExplicit()) continue;
            Rect area = content.inset(data.margin());
            Size size = Layout.childSize(child);
            int w = data.hasRelativeW() ? Math.round(area.w() * data.relativeW()) : size.w();
            int h = data.hasRelativeH() ? Math.round(area.h() * data.relativeH()) : size.h();
            if (data.fill()) {
                w = area.w();
                h = area.h();
            }
            Rect placed;
            if (data.hasRelativePos()) {
                int x = area.x() + Math.round(area.w() * data.relativeX()) + data.offsetX();
                int y = area.y() + Math.round(area.h() * data.relativeY()) + data.offsetY();
                placed = new Rect(x - Math.round(w * data.anchor().fractionX()), y - Math.round(h * data.anchor().fractionY()), w, h);
            } else {
                placed = data.anchor().place(area, w, h, data.offsetX(), data.offsetY());
            }
            child.setBounds(placed);
        }
    }
}

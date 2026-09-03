package com.breakinblocks.saelibvie.ui.layout;

import com.breakinblocks.saelibvie.ui.core.Layout;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.geom.Rect;

public final class AbsoluteLayout implements Layout {
    public static final AbsoluteLayout INSTANCE = new AbsoluteLayout();

    private AbsoluteLayout() {
    }

    @Override
    public void apply(Panel panel, Rect content) {
        AnchorLayout.INSTANCE.apply(panel, content);
    }
}

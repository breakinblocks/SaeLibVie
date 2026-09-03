package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.core.LayoutData;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;

public class Spacer extends Widget {
    public Spacer(int w, int h) {
        size(w, h);
    }

    public static Spacer flexible() {
        Spacer spacer = new Spacer(0, 0);
        spacer.layout(LayoutData.weighted(1f));
        return spacer;
    }

    public static Spacer flexible(float weight) {
        Spacer spacer = new Spacer(0, 0);
        spacer.layout(LayoutData.weighted(weight));
        return spacer;
    }

    @Override
    protected void paint(UiGraphics g) {
    }
}

package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Axis;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;

public class Separator extends Widget {
    private final Axis axis;

    public Separator(Axis axis) {
        this.axis = axis;
    }

    public static Separator horizontal() {
        return new Separator(Axis.HORIZONTAL);
    }

    public static Separator vertical() {
        return new Separator(Axis.VERTICAL);
    }

    @Override
    protected Size measure() {
        if (width() > 0 && height() > 0) return new Size(width(), height());
        return axis == Axis.HORIZONTAL ? new Size(Math.max(width(), 1), 3) : new Size(3, Math.max(height(), 1));
    }

    @Override
    protected void paint(UiGraphics g) {
        Painter.separator(g, localRect(), axis);
    }
}

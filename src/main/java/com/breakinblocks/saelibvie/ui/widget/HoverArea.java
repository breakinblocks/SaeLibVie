package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class HoverArea extends Widget {
    @Nullable
    private Consumer<Integer> onClick;
    private boolean highlight;

    public HoverArea(Rect bounds) {
        super(bounds);
    }

    public HoverArea onClick(Runnable action) {
        this.onClick = button -> action.run();
        return this;
    }

    public HoverArea onClick(Consumer<Integer> action) {
        this.onClick = action;
        return this;
    }

    public HoverArea highlight(boolean highlight) {
        this.highlight = highlight;
        return this;
    }

    @Override
    protected void paint(UiGraphics g) {
        if (highlight && isHovered()) {
            Painter.hoverTint(g, localRect());
        }
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (onClick == null) return false;
        onClick.accept(button);
        return true;
    }
}

package com.breakinblocks.saelibvie.ui.behavior;

import com.breakinblocks.saelibvie.ui.core.Behavior;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ResizeBehavior implements Behavior {
    private int grip = 6;
    private int minW = 40;
    private int minH = 30;
    private int maxW = Integer.MAX_VALUE;
    private int maxH = Integer.MAX_VALUE;
    private boolean edges = true;
    private boolean cornerOnly;
    private boolean drawGrip = true;
    private boolean keepAspect;
    @Nullable
    private BooleanSupplier enabled;
    @Nullable
    private Consumer<Widget> onResize;
    @Nullable
    private Consumer<Widget> onEnd;

    private boolean resizing;
    private boolean left;
    private boolean right;
    private boolean top;
    private boolean bottom;
    private double anchorX;
    private double anchorY;
    private Rect startBounds = Rect.EMPTY;

    public static ResizeBehavior create() {
        return new ResizeBehavior();
    }

    public ResizeBehavior grip(int grip) {
        this.grip = Math.max(1, grip);
        return this;
    }

    public ResizeBehavior min(int w, int h) {
        this.minW = w;
        this.minH = h;
        return this;
    }

    public ResizeBehavior max(int w, int h) {
        this.maxW = w;
        this.maxH = h;
        return this;
    }

    public ResizeBehavior edges(boolean allEdges) {
        this.edges = allEdges;
        return this;
    }

    public ResizeBehavior cornerOnly(boolean cornerOnly) {
        this.cornerOnly = cornerOnly;
        return this;
    }

    public ResizeBehavior drawGrip(boolean draw) {
        this.drawGrip = draw;
        return this;
    }

    public ResizeBehavior keepAspect(boolean keep) {
        this.keepAspect = keep;
        return this;
    }

    public ResizeBehavior enabledWhen(BooleanSupplier condition) {
        this.enabled = condition;
        return this;
    }

    public ResizeBehavior onResize(Consumer<Widget> callback) {
        this.onResize = callback;
        return this;
    }

    public ResizeBehavior onEnd(Consumer<Widget> callback) {
        this.onEnd = callback;
        return this;
    }

    public boolean isResizing() {
        return resizing;
    }

    @Override
    public boolean isActive(Widget widget) {
        return resizing;
    }

    private boolean isEnabled() {
        return enabled == null || enabled.getAsBoolean();
    }

    public boolean hitsGrip(Widget widget, double lx, double ly) {
        int w = widget.width();
        int h = widget.height();
        boolean nearRight = lx >= w - grip && lx < w;
        boolean nearBottom = ly >= h - grip && ly < h;
        boolean nearLeft = lx >= 0 && lx < grip;
        boolean nearTop = ly >= 0 && ly < grip;
        if (cornerOnly) {
            return nearRight && nearBottom;
        }
        if (!edges) {
            return nearRight && nearBottom;
        }
        return nearRight || nearBottom || nearLeft || nearTop;
    }

    @Override
    public boolean mouseClicked(Widget widget, double lx, double ly, int button) {
        if (button != 0 || !isEnabled()) return false;
        int w = widget.width();
        int h = widget.height();
        boolean nearRight = lx >= w - grip && lx < w;
        boolean nearBottom = ly >= h - grip && ly < h;
        boolean nearLeft = lx >= 0 && lx < grip;
        boolean nearTop = ly >= 0 && ly < grip;
        if (cornerOnly || !edges) {
            if (!(nearRight && nearBottom)) return false;
            nearLeft = false;
            nearTop = false;
        }
        if (!(nearRight || nearBottom || nearLeft || nearTop)) return false;
        resizing = true;
        right = nearRight;
        bottom = nearBottom;
        left = nearLeft && !nearRight;
        top = nearTop && !nearBottom;
        anchorX = lx;
        anchorY = ly;
        startBounds = widget.bounds();
        if (widget.parent() != null) {
            widget.parent().bringToFront(widget);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(Widget widget, double lx, double ly, int button, double dx, double dy) {
        if (!resizing) return false;
        double deltaX = lx - anchorX;
        double deltaY = ly - anchorY;
        Rect current = widget.bounds();
        int x = current.x();
        int y = current.y();
        int w = current.w();
        int h = current.h();
        if (right) {
            w = clampW((int) Math.round(w + deltaX));
        } else if (left) {
            int newW = clampW((int) Math.round(w - deltaX));
            x += w - newW;
            w = newW;
        }
        if (bottom) {
            h = clampH((int) Math.round(h + deltaY));
        } else if (top) {
            int newH = clampH((int) Math.round(h - deltaY));
            y += h - newH;
            h = newH;
        }
        if (keepAspect && startBounds.h() > 0) {
            float aspect = (float) startBounds.w() / startBounds.h();
            if (right || left) {
                h = clampH(Math.round(w / aspect));
            } else {
                w = clampW(Math.round(h * aspect));
            }
        }
        widget.setBounds(new Rect(x, y, w, h));
        if (onResize != null) onResize.accept(widget);
        return true;
    }

    @Override
    public boolean mouseReleased(Widget widget, double lx, double ly, int button) {
        if (!resizing) return false;
        resizing = false;
        if (onEnd != null) onEnd.accept(widget);
        return true;
    }

    @Override
    public void paintOverlay(Widget widget, UiGraphics g) {
        if (!drawGrip || !isEnabled()) return;
        int size = Math.min(grip, Math.min(widget.width(), widget.height()));
        Painter.resizeGrip(g, new Rect(widget.width() - size - 1, widget.height() - size - 1, size, size));
    }

    private int clampW(int w) {
        return Math.max(minW, Math.min(maxW, w));
    }

    private int clampH(int h) {
        return Math.max(minH, Math.min(maxH, h));
    }
}

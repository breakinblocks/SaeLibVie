package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.core.CursorType;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Axis;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import net.minecraft.util.Mth;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class ScrollBar extends Widget {
    private final Axis axis;
    private IntSupplier contentSize = () -> 0;
    private IntSupplier viewSize = () -> 0;
    private IntSupplier offset = () -> 0;
    private IntConsumer setOffset = v -> {
    };
    private boolean dragging;
    private double dragAnchor;
    private int dragStartOffset;
    private boolean autoHide = true;

    public ScrollBar(Axis axis) {
        this.axis = axis;
    }

    public ScrollBar bind(IntSupplier contentSize, IntSupplier viewSize, IntSupplier offset, IntConsumer setOffset) {
        this.contentSize = contentSize;
        this.viewSize = viewSize;
        this.offset = offset;
        this.setOffset = setOffset;
        return this;
    }

    public ScrollBar autoHide(boolean hide) {
        this.autoHide = hide;
        return this;
    }

    public int maxOffset() {
        return Math.max(0, contentSize.getAsInt() - viewSize.getAsInt());
    }

    public boolean isScrollable() {
        return maxOffset() > 0;
    }

    @Override
    public CursorType cursor() {
        if (!dragging) return null;
        return axis == Axis.VERTICAL ? CursorType.VRESIZE : CursorType.HRESIZE;
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && (!autoHide || isScrollable());
    }

    private float position() {
        int max = maxOffset();
        return max <= 0 ? 0f : (float) offset.getAsInt() / max;
    }

    private float thumbFraction() {
        int content = contentSize.getAsInt();
        return content <= 0 ? 1f : (float) viewSize.getAsInt() / content;
    }

    @Override
    protected void paint(UiGraphics g) {
        Painter.scrollbar(g, localRect(), position(), thumbFraction(), axis, isHovered() || dragging);
    }

    private Rect thumb() {
        return Painter.thumbRect(localRect(), position(), thumbFraction(), axis);
    }

    private int trackLength() {
        return axis == Axis.VERTICAL ? height() : width();
    }

    private int thumbLength() {
        Rect t = thumb();
        return axis == Axis.VERTICAL ? t.h() : t.w();
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (button != 0 || !isScrollable()) return false;
        Rect t = thumb();
        double along = axis == Axis.VERTICAL ? ly : lx;
        int thumbStart = axis == Axis.VERTICAL ? t.y() : t.x();
        if (t.contains(lx, ly)) {
            dragging = true;
            dragAnchor = along;
            dragStartOffset = offset.getAsInt();
            return true;
        }
        int travel = trackLength() - thumbLength();
        if (travel <= 0) return true;
        double target = (along - thumbLength() / 2.0) / travel;
        setOffset.accept(Mth.clamp((int) Math.round(target * maxOffset()), 0, maxOffset()));
        dragging = true;
        dragAnchor = along;
        dragStartOffset = offset.getAsInt();
        return true;
    }

    @Override
    protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
        if (!dragging) return false;
        double along = axis == Axis.VERTICAL ? ly : lx;
        int travel = trackLength() - thumbLength();
        if (travel <= 0) return true;
        double delta = (along - dragAnchor) / travel * maxOffset();
        setOffset.accept(Mth.clamp((int) Math.round(dragStartOffset + delta), 0, maxOffset()));
        return true;
    }

    @Override
    protected boolean onMouseReleased(double lx, double ly, int button) {
        if (!dragging) return false;
        dragging = false;
        return true;
    }

    @Override
    protected boolean onMouseScrolled(double lx, double ly, double scrollX, double scrollY) {
        if (!isScrollable()) return false;
        int step = Math.max(1, viewSize.getAsInt() / 8);
        setOffset.accept(Mth.clamp(offset.getAsInt() - (int) Math.signum(scrollY) * step, 0, maxOffset()));
        return true;
    }
}

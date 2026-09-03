package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.core.CursorType;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.geom.Axis;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import net.minecraft.util.Mth;

public class ScrollPanel extends Panel {
    public enum ReservePolicy {
        ALWAYS,
        WHEN_SCROLLABLE,
        NEVER
    }

    private final Axis axis;
    private final ScrollBar bar;
    private int scrollStep = 14;
    private boolean showBar = true;
    private int explicitContent = -1;
    private ReservePolicy reservePolicy = ReservePolicy.ALWAYS;
    private boolean reservedLastPass;
    private boolean measuring;

    public ScrollPanel() {
        this(Axis.VERTICAL);
    }

    public ScrollPanel(Axis axis) {
        this.axis = axis;
        this.bar = new ScrollBar(axis).bind(this::contentLength, this::viewLength, this::offset, this::setOffset);
        adoptDetached(this.bar);
        clip(true);
    }

    public ScrollPanel(Rect bounds) {
        this();
        bounds(bounds);
    }

    public ScrollPanel scrollStep(int step) {
        this.scrollStep = Math.max(1, step);
        return this;
    }

    public ScrollPanel showBar(boolean show) {
        this.showBar = show;
        return this;
    }

    public ScrollPanel reserveBarSpace(ReservePolicy policy) {
        this.reservePolicy = policy;
        requestLayout();
        return this;
    }

    public ReservePolicy reservePolicy() {
        return reservePolicy;
    }

    public ScrollPanel contentLength(int length) {
        this.explicitContent = length;
        return this;
    }

    public Axis axis() {
        return axis;
    }

    public ScrollBar bar() {
        return bar;
    }

    public int contentLength() {
        if (explicitContent >= 0) return explicitContent;
        Size measured = layoutStrategy().measure(this);
        return Math.round((axis == Axis.VERTICAL ? measured.h() : measured.w()) * scale());
    }

    public int viewLength() {
        Rect content = contentRect();
        return axis == Axis.VERTICAL ? content.h() : content.w();
    }

    public int maxOffset() {
        return Math.max(0, contentLength() - viewLength());
    }

    public int offset() {
        return axis == Axis.VERTICAL ? scrollY : scrollX;
    }

    public void setOffset(int value) {
        int clamped = Mth.clamp(value, 0, maxOffset());
        if (axis == Axis.VERTICAL) {
            scrollY = clamped;
        } else {
            scrollX = clamped;
        }
    }

    public void scrollBy(int delta) {
        setOffset(offset() + delta);
    }

    public void scrollToTop() {
        setOffset(0);
    }

    public void scrollToBottom() {
        setOffset(maxOffset());
    }

    public void reveal(Rect childRect) {
        int start = axis == Axis.VERTICAL ? childRect.y() : childRect.x();
        int end = axis == Axis.VERTICAL ? childRect.bottom() : childRect.right();
        int view = viewLength();
        if (start < offset()) {
            setOffset(start);
        } else if (end > offset() + view) {
            setOffset(end - view);
        }
    }

    public boolean isScrollable() {
        return maxOffset() > 0;
    }

    private boolean barVisible() {
        return showBar && maxOffset() > 0;
    }

    public int barThickness() {
        UiRoot root = root();
        return root != null ? root.theme().scrollbarWidth() : 4;
    }

    private boolean reserveSpace() {
        if (!showBar) return false;
        return switch (reservePolicy) {
            case ALWAYS -> true;
            case NEVER -> false;
            case WHEN_SCROLLABLE -> reservedLastPass;
        };
    }

    @Override
    public Rect contentRect() {
        Rect base = super.contentRect();
        if (!reserveSpace()) return base;
        int thickness = barThickness() + 1;
        if (axis == Axis.VERTICAL) {
            return base.inset(0, 0, thickness, 0);
        }
        return base.inset(0, 0, 0, thickness);
    }

    private Rect barRect() {
        Rect base = super.contentRect();
        int thickness = barThickness();
        if (axis == Axis.VERTICAL) {
            return new Rect(base.right() - thickness, base.y(), thickness, base.h());
        }
        return new Rect(base.x(), base.bottom() - thickness, base.w(), thickness);
    }

    @Override
    public void layoutNow() {
        if (reservePolicy == ReservePolicy.WHEN_SCROLLABLE && showBar && !measuring) {
            measuring = true;
            try {
                reservedLastPass = false;
                super.layoutNow();
                boolean overflow = maxOffset() > 0;
                if (overflow) {
                    reservedLastPass = true;
                    super.layoutNow();
                }
            } finally {
                measuring = false;
            }
        } else {
            super.layoutNow();
        }
    }

    @Override
    protected void onLayout() {
        setOffset(offset());
    }

    @Override
    protected void paintForeground(UiGraphics g) {
        super.paintForeground(g);
        if (barVisible()) {
            bar.setBounds(barRect());
            bar.render(g);
        }
    }

    private boolean overBar(double lx, double ly) {
        return barVisible() && bar.bounds().contains(lx, ly);
    }

    @Override
    public boolean mouseClicked(double lx, double ly, int button) {
        if (!isInteractive()) return false;
        if (overBar(lx, ly)) {
            bar.setBounds(barRect());
            return bar.mouseClicked(lx - bar.x(), ly - bar.y(), button);
        }
        return super.mouseClicked(lx, ly, button);
    }

    @Override
    public boolean mouseDragged(double lx, double ly, int button, double dx, double dy) {
        if (bar.mouseDragged(lx - bar.x(), ly - bar.y(), button, dx, dy)) return true;
        return super.mouseDragged(lx, ly, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double lx, double ly, int button) {
        if (bar.mouseReleased(lx - bar.x(), ly - bar.y(), button)) return true;
        return super.mouseReleased(lx, ly, button);
    }

    @Override
    public void updateHover(double lx, double ly) {
        bar.setHovered(overBar(lx, ly));
        super.updateHover(lx, ly);
    }

    @Override
    public CursorType cursor() {
        CursorType barCursor = bar.cursor();
        if (barCursor != null) return barCursor;
        return super.cursor();
    }

    @Override
    protected boolean onMouseScrolled(double lx, double ly, double scrollX, double scrollY) {
        if (maxOffset() <= 0) return false;
        double amount = axis == Axis.VERTICAL ? scrollY : (scrollX != 0 ? scrollX : scrollY);
        if (amount == 0) return false;
        scrollBy((int) Math.round(-amount * scrollStep));
        return true;
    }
}

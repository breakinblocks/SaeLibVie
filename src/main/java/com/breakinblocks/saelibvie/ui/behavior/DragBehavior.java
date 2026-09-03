package com.breakinblocks.saelibvie.ui.behavior;

import com.breakinblocks.saelibvie.ui.core.Behavior;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class DragBehavior implements Behavior {
    private int button = 0;
    @Nullable
    private Function<Widget, Rect> handle;
    private boolean clampToParent = true;
    private int minVisible = 8;
    private int snap = 1;
    private boolean bringToFront = true;
    private boolean lockX;
    private boolean lockY;
    @Nullable
    private BooleanSupplier enabled;
    @Nullable
    private Consumer<Widget> onStart;
    @Nullable
    private Consumer<Widget> onDrag;
    @Nullable
    private Consumer<Widget> onEnd;

    private boolean dragging;
    private double anchorX;
    private double anchorY;

    public static DragBehavior create() {
        return new DragBehavior();
    }

    public DragBehavior button(int button) {
        this.button = button;
        return this;
    }

    public DragBehavior handle(Function<Widget, Rect> handle) {
        this.handle = handle;
        return this;
    }

    public DragBehavior handle(Rect localHandle) {
        this.handle = w -> localHandle;
        return this;
    }

    public DragBehavior clampToParent(boolean clamp) {
        this.clampToParent = clamp;
        return this;
    }

    public DragBehavior minVisible(int pixels) {
        this.minVisible = pixels;
        return this;
    }

    public DragBehavior snap(int grid) {
        this.snap = Math.max(1, grid);
        return this;
    }

    public DragBehavior bringToFront(boolean value) {
        this.bringToFront = value;
        return this;
    }

    public DragBehavior lockAxis(boolean lockX, boolean lockY) {
        this.lockX = lockX;
        this.lockY = lockY;
        return this;
    }

    public DragBehavior enabledWhen(BooleanSupplier condition) {
        this.enabled = condition;
        return this;
    }

    public DragBehavior onStart(Consumer<Widget> callback) {
        this.onStart = callback;
        return this;
    }

    public DragBehavior onDrag(Consumer<Widget> callback) {
        this.onDrag = callback;
        return this;
    }

    public DragBehavior onEnd(Consumer<Widget> callback) {
        this.onEnd = callback;
        return this;
    }

    public boolean isDragging() {
        return dragging;
    }

    @Override
    public boolean isActive(Widget widget) {
        return dragging;
    }

    private boolean isEnabled() {
        return enabled == null || enabled.getAsBoolean();
    }

    @Override
    public boolean mouseClicked(Widget widget, double lx, double ly, int button) {
        if (button != this.button || !isEnabled()) return false;
        if (handle != null && !handle.apply(widget).contains(lx, ly)) return false;
        dragging = true;
        anchorX = lx;
        anchorY = ly;
        if (bringToFront && widget.parent() != null) {
            widget.parent().bringToFront(widget);
        }
        if (onStart != null) onStart.accept(widget);
        return true;
    }

    @Override
    public boolean mouseDragged(Widget widget, double lx, double ly, int button, double dx, double dy) {
        if (!dragging) return false;
        int newX = widget.x() + (lockX ? 0 : (int) Math.round(lx - anchorX));
        int newY = widget.y() + (lockY ? 0 : (int) Math.round(ly - anchorY));
        if (snap > 1) {
            newX = Math.round((float) newX / snap) * snap;
            newY = Math.round((float) newY / snap) * snap;
        }
        Rect target = widget.bounds().at(newX, newY);
        if (clampToParent) {
            target = clamp(widget, target);
        }
        widget.setBounds(target);
        if (onDrag != null) onDrag.accept(widget);
        return true;
    }

    @Override
    public boolean mouseReleased(Widget widget, double lx, double ly, int button) {
        if (!dragging) return false;
        dragging = false;
        if (onEnd != null) onEnd.accept(widget);
        return true;
    }

    private Rect clamp(Widget widget, Rect target) {
        Panel parent = widget.parent();
        if (parent == null) return target;
        Size content = parent.contentSize();
        Rect area;
        if (parent instanceof UiRoot root) {
            area = root.screenRectLocal().offset(-root.contentOriginX(), -root.contentOriginY());
        } else {
            area = new Rect(0, 0, content.w(), content.h());
        }
        int minX = area.x() - target.w() + minVisible;
        int maxX = area.right() - minVisible;
        int minY = area.y();
        int maxY = area.bottom() - minVisible;
        if (target.w() <= area.w()) {
            minX = area.x();
            maxX = area.right() - target.w();
        }
        if (target.h() <= area.h()) {
            maxY = area.bottom() - target.h();
        }
        int x = Math.max(minX, Math.min(target.x(), maxX));
        int y = Math.max(minY, Math.min(target.y(), maxY));
        return target.at(x, y);
    }
}

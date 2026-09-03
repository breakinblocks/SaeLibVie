package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.behavior.DragBehavior;
import com.breakinblocks.saelibvie.ui.behavior.ResizeBehavior;
import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.CursorType;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.UiSounds;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Window extends Panel {
    private Supplier<Component> title;
    private final DragBehavior drag;
    @Nullable
    private ResizeBehavior resize;
    private boolean closable = true;
    private boolean collapsible;
    private boolean collapsed;
    private int expandedHeight;
    @Nullable
    private Runnable onClose;
    private boolean closeButtonPressed;

    public Window(Component title) {
        this(() -> title);
    }

    public Window(Supplier<Component> title) {
        this.title = title;
        chrome(Chrome.WINDOW);
        padding(3);
        drag = DragBehavior.create().handle(w -> titleBarRect());
        behavior(drag);
    }

    public Window(Component title, Rect bounds) {
        this(title);
        bounds(bounds);
    }

    public Window title(Component title) {
        this.title = () -> title;
        return this;
    }

    public Window title(Supplier<Component> title) {
        this.title = title;
        return this;
    }

    public Component currentTitle() {
        return title.get();
    }

    public Window closable(boolean closable) {
        this.closable = closable;
        return this;
    }

    public Window collapsible(boolean collapsible) {
        this.collapsible = collapsible;
        return this;
    }

    public Window draggable(boolean draggable) {
        drag.enabledWhen(() -> draggable);
        return this;
    }

    public Window resizable(boolean resizable) {
        if (resizable && resize == null) {
            resize = ResizeBehavior.create().cornerOnly(false).min(60, titleBarHeight() + 20).onResize(w -> requestLayout());
            behavior(resize);
        } else if (!resizable && resize != null) {
            removeBehavior(resize);
            resize = null;
        }
        return this;
    }

    public Window resizable(int minW, int minH, int maxW, int maxH) {
        resizable(true);
        if (resize != null) {
            resize.min(minW, minH).max(maxW, maxH);
        }
        return this;
    }

    public Window onClose(Runnable onClose) {
        this.onClose = onClose;
        return this;
    }

    public DragBehavior dragBehavior() {
        return drag;
    }

    @Nullable
    public ResizeBehavior resizeBehavior() {
        return resize;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        if (this.collapsed == collapsed) return;
        this.collapsed = collapsed;
        if (collapsed) {
            expandedHeight = height();
            setSize(width(), titleBarHeight() + chromeInset() * 2 + padding().vertical());
        } else {
            setSize(width(), Math.max(expandedHeight, titleBarHeight() + 20));
        }
        for (Widget child : children) {
            child.setVisible(!collapsed);
        }
    }

    public void close() {
        if (onClose != null) onClose.run();
        UiRoot root = root();
        if (root != null && root.layers().stream().anyMatch(layer -> layer.widget() == this)) {
            root.removeLayer(this);
            return;
        }
        if (parent() != null) {
            parent().remove(this);
        }
    }

    public void bringToFront() {
        if (parent() != null) parent().bringToFront(this);
    }

    public boolean isFrontmost() {
        Panel p = parent();
        if (p == null) return true;
        var siblings = p.children();
        return siblings.isEmpty() || siblings.get(siblings.size() - 1) == this;
    }

    protected int titleBarHeight() {
        UiRoot root = root();
        return root != null ? root.theme().titleBarHeight() : 12;
    }

    @Override
    protected CursorType ownCursor() {
        UiRoot root = root();
        if (root == null) return null;
        double mx = root.lastMouseX() - rootX();
        double my = root.lastMouseY() - rootY();
        if (resize != null && resize.hitsGrip(this, mx, my)) {
            return CursorType.HRESIZE;
        }
        if (titleBarRect().contains(mx, my)) {
            return CursorType.HAND;
        }
        return null;
    }

    public Rect titleBarRect() {
        int inset = chromeInset();
        return new Rect(inset, inset, width() - inset * 2, titleBarHeight());
    }

    private Rect closeButtonRect() {
        Rect bar = titleBarRect();
        int size = bar.h() - 2;
        return new Rect(bar.right() - size - 1, bar.y() + 1, size, size);
    }

    private Rect collapseButtonRect() {
        Rect bar = titleBarRect();
        int size = bar.h() - 2;
        int offset = closable ? size + 2 : 1;
        return new Rect(bar.right() - size - offset, bar.y() + 1, size, size);
    }

    @Override
    public Rect contentRect() {
        int inset = chromeInset();
        Rect body = localRect().inset(inset).splitTop(titleBarHeight());
        return body.inset(padding());
    }

    @Override
    protected void paintBackground(UiGraphics g) {
        super.paintBackground(g);
        Rect bar = titleBarRect();
        Painter.titleBar(g, bar, title.get(), isFrontmost());
        double mx = g.localMouseX();
        double my = g.localMouseY();
        if (closable) {
            Rect close = closeButtonRect();
            boolean hovered = isHovered() && close.contains(mx, my);
            int color = hovered ? g.color(ColorToken.NEGATIVE) : g.color(ColorToken.TEXT_DIM);
            g.centeredText("x", close.centerX() + 1, close.y() + (close.h() - 8) / 2, color, false);
        }
        if (collapsible) {
            Rect collapse = collapseButtonRect();
            boolean hovered = isHovered() && collapse.contains(mx, my);
            int color = hovered ? g.color(ColorToken.ACCENT) : g.color(ColorToken.TEXT_DIM);
            g.centeredText(collapsed ? "+" : "-", collapse.centerX() + 1, collapse.y() + (collapse.h() - 8) / 2, color, false);
        }
    }

    @Override
    protected void paintChildren(UiGraphics g) {
        if (collapsed) return;
        super.paintChildren(g);
    }

    @Override
    public boolean mouseClicked(double lx, double ly, int button) {
        if (!isInteractive()) return false;
        if (button == 0) {
            if (closable && closeButtonRect().contains(lx, ly)) {
                closeButtonPressed = true;
                bringToFront();
                return true;
            }
            if (collapsible && collapseButtonRect().contains(lx, ly)) {
                UiSounds.click();
                setCollapsed(!collapsed);
                bringToFront();
                return true;
            }
        }
        boolean handled = super.mouseClicked(lx, ly, button);
        bringToFront();
        return true;
    }

    @Override
    public boolean mouseReleased(double lx, double ly, int button) {
        if (closeButtonPressed) {
            closeButtonPressed = false;
            if (closeButtonRect().contains(lx, ly)) {
                UiSounds.click();
                close();
            }
            return true;
        }
        return super.mouseReleased(lx, ly, button);
    }
}

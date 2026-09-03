package com.breakinblocks.saelibvie.ui.core;

import com.breakinblocks.saelibvie.ui.geom.Insets;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.layout.AbsoluteLayout;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Panel extends Widget {
    public enum Chrome {
        NONE,
        WINDOW,
        PANEL,
        INSET
    }

    protected final List<Widget> children = new ArrayList<>();
    private Layout layout = AbsoluteLayout.INSTANCE;
    private Insets padding = Insets.NONE;
    private float scale = 1f;
    private boolean clip;
    protected int scrollX;
    protected int scrollY;
    private boolean layoutDirty = true;
    @Nullable
    private Widget captured;
    @Nullable
    private Widget hoveredChild;
    private Chrome chrome = Chrome.NONE;
    @Nullable
    private Supplier<Component> header;
    @Nullable
    private BiConsumer<Panel, UiGraphics> background;
    @Nullable
    private BiConsumer<Panel, UiGraphics> foreground;

    public Panel() {
    }

    public Panel(Rect bounds) {
        super(bounds);
    }

    public <T extends Panel> T layout(Layout layout) {
        this.layout = layout;
        requestLayout();
        return self();
    }

    public Layout layoutStrategy() {
        return layout;
    }

    public <T extends Panel> T padding(Insets padding) {
        this.padding = padding;
        requestLayout();
        return self();
    }

    public <T extends Panel> T padding(int all) {
        return padding(Insets.all(all));
    }

    public Insets padding() {
        return padding;
    }

    public <T extends Panel> T scale(float scale) {
        this.scale = Math.max(0.05f, scale);
        return self();
    }

    public float scale() {
        return scale;
    }

    public <T extends Panel> T clip(boolean clip) {
        this.clip = clip;
        return self();
    }

    public boolean clips() {
        return clip;
    }

    public <T extends Panel> T chrome(Chrome chrome) {
        this.chrome = chrome;
        requestLayout();
        return self();
    }

    public Chrome chrome() {
        return chrome;
    }

    public <T extends Panel> T header(Component header) {
        this.header = () -> header;
        requestLayout();
        return self();
    }

    public <T extends Panel> T header(Supplier<Component> header) {
        this.header = header;
        requestLayout();
        return self();
    }

    @Nullable
    public Component headerText() {
        return header == null ? null : header.get();
    }

    public <T extends Panel> T background(BiConsumer<Panel, UiGraphics> painter) {
        this.background = painter;
        return self();
    }

    public <T extends Panel> T foreground(BiConsumer<Panel, UiGraphics> painter) {
        this.foreground = painter;
        return self();
    }

    public <T extends Widget> T add(T child) {
        if (child.parent != null) {
            child.parent.remove(child);
        }
        children.add(child);
        child.parent = this;
        child.onAdded(this);
        requestLayout();
        return child;
    }

    public <T extends Widget> T add(T child, LayoutData data) {
        child.layout(data);
        return add(child);
    }

    public <T extends Panel> T with(Widget... widgets) {
        for (Widget widget : widgets) {
            add(widget);
        }
        return self();
    }

    public void adoptDetached(Widget child) {
        child.parent = this;
    }

    public void remove(Widget child) {
        if (children.remove(child)) {
            child.parent = null;
            child.setHovered(false);
            if (captured == child) captured = null;
            UiRoot root = root();
            if (root != null) root.onWidgetRemoved(child);
            child.onRemoved(this);
            requestLayout();
        }
    }

    public void clear() {
        for (Widget child : new ArrayList<>(children)) {
            remove(child);
        }
    }

    public List<Widget> children() {
        return Collections.unmodifiableList(children);
    }

    public void bringToFront(Widget child) {
        if (children.remove(child)) {
            children.add(child);
        }
    }

    public void sendToBack(Widget child) {
        if (children.remove(child)) {
            children.add(0, child);
        }
    }

    public int headerHeight(UiGraphics g) {
        return header == null ? 0 : g.theme().headerHeight() + 1;
    }

    protected int headerHeightEstimate() {
        if (header == null) return 0;
        UiRoot root = root();
        return (root != null ? root.theme().headerHeight() : 11) + 1;
    }

    protected int chromeInset() {
        return switch (chrome) {
            case NONE -> 0;
            case WINDOW -> 2;
            case PANEL, INSET -> 1;
        };
    }

    public Rect contentRect() {
        int inset = chromeInset();
        return localRect().inset(inset).inset(padding).splitTop(headerHeightEstimate());
    }

    public int contentOriginX() {
        return contentRect().x() - scrollX;
    }

    public int contentOriginY() {
        return contentRect().y() - scrollY;
    }

    public double toChildX(double lx) {
        return (lx - contentOriginX()) / scale;
    }

    public double toChildY(double ly) {
        return (ly - contentOriginY()) / scale;
    }

    public int childToLocalX(int cx) {
        return Math.round(cx * scale) + contentOriginX();
    }

    public int childToLocalY(int cy) {
        return Math.round(cy * scale) + contentOriginY();
    }

    public Size contentSize() {
        Rect content = contentRect();
        return new Size(Math.round(content.w() / scale), Math.round(content.h() / scale));
    }

    public void requestLayout() {
        layoutDirty = true;
    }

    public boolean isLayoutDirty() {
        return layoutDirty;
    }

    public void layoutNow() {
        Size content = contentSize();
        layout.apply(this, new Rect(0, 0, content.w(), content.h()));
        layoutDirty = false;
        for (Widget child : children) {
            if (child instanceof Panel panel) {
                panel.layoutNow();
            }
        }
        onLayout();
    }

    protected void onLayout() {
    }

    public void layoutIfNeeded() {
        if (layoutDirty) {
            layoutNow();
            return;
        }
        for (Widget child : children) {
            if (child instanceof Panel panel) {
                panel.layoutIfNeeded();
            }
        }
    }

    @Override
    protected void onResized() {
        requestLayout();
    }

    @Override
    protected Size measure() {
        Size inner = layout.measure(this);
        int inset = chromeInset();
        return new Size(Math.round(inner.w() * scale) + padding.horizontal() + inset * 2,
                Math.round(inner.h() * scale) + padding.vertical() + inset * 2 + headerHeightEstimate());
    }

    public void packToContent() {
        Size preferred = preferredSize();
        setSize(preferred.w(), preferred.h());
    }

    @Override
    public void tick() {
        super.tick();
        for (Widget child : new ArrayList<>(children)) {
            child.tick();
        }
    }

    @Override
    protected void beforeRender(UiGraphics g) {
        layoutIfNeeded();
    }

    @Override
    protected void paint(UiGraphics g) {
        paintBackground(g);
        paintChildren(g);
        paintForeground(g);
    }

    protected void paintBackground(UiGraphics g) {
        Rect r = localRect();
        switch (chrome) {
            case WINDOW -> Painter.window(g, r);
            case PANEL -> Painter.panel(g, r);
            case INSET -> Painter.inset(g, r);
            case NONE -> {
            }
        }
        if (background != null) {
            background.accept(this, g);
        }
        if (header != null) {
            Painter.panelHeader(g, r.inset(chrome == Chrome.WINDOW ? 1 : 0), header.get());
        }
    }

    protected void paintChildren(UiGraphics g) {
        if (clip) {
            g.pushScissor(contentRect());
        }
        g.pushTransform(contentOriginX(), contentOriginY(), scale);
        for (Widget child : children) {
            child.render(g);
        }
        g.popTransform();
        if (clip) {
            g.popScissor();
        }
    }

    protected void paintForeground(UiGraphics g) {
        if (foreground != null) {
            foreground.accept(this, g);
        }
    }

    @Nullable
    public Widget childAt(double lx, double ly) {
        if (clip && !contentRect().contains(lx, ly)) {
            return null;
        }
        double cx = toChildX(lx);
        double cy = toChildY(ly);
        for (int i = children.size() - 1; i >= 0; i--) {
            Widget child = children.get(i);
            if (child.isVisible() && child.contains(cx - child.x(), cy - child.y())) {
                return child;
            }
        }
        return null;
    }

    public void updateHover(double lx, double ly) {
        Widget hit = isInteractive() && contains(lx, ly) ? childAt(lx, ly) : null;
        if (captured != null && captured.isVisible()) {
            hit = captured;
        }
        hoveredChild = hit;
        double cx = toChildX(lx);
        double cy = toChildY(ly);
        for (Widget child : children) {
            boolean hovered = child == hit;
            child.setHovered(hovered);
            if (child instanceof Panel panel) {
                if (hovered) {
                    panel.updateHover(cx - child.x(), cy - child.y());
                } else {
                    panel.clearHover();
                }
            } else if (hovered) {
                child.mouseMoved(cx - child.x(), cy - child.y());
            }
        }
    }

    public void clearHover() {
        hoveredChild = null;
        for (Widget child : children) {
            child.setHovered(false);
            if (child instanceof Panel panel) {
                panel.clearHover();
            }
        }
    }

    @Override
    protected boolean dispatchMouseClicked(double lx, double ly, int button) {
        Widget child = childAt(lx, ly);
        if (child == null) return false;
        double cx = toChildX(lx) - child.x();
        double cy = toChildY(ly) - child.y();
        if (child.mouseClicked(cx, cy, button)) {
            captured = child;
            return true;
        }
        return false;
    }

    @Override
    protected boolean dispatchMouseReleased(double lx, double ly, int button) {
        Widget target = captured != null ? captured : childAt(lx, ly);
        captured = null;
        if (target == null) return false;
        double cx = toChildX(lx) - target.x();
        double cy = toChildY(ly) - target.y();
        return target.mouseReleased(cx, cy, button);
    }

    @Override
    protected boolean dispatchMouseDragged(double lx, double ly, int button, double dx, double dy) {
        if (captured == null) return false;
        double cx = toChildX(lx) - captured.x();
        double cy = toChildY(ly) - captured.y();
        return captured.mouseDragged(cx, cy, button, dx / scale, dy / scale);
    }

    @Override
    protected boolean dispatchMouseScrolled(double lx, double ly, double scrollX, double scrollY) {
        Widget child = childAt(lx, ly);
        if (child == null) return false;
        double cx = toChildX(lx) - child.x();
        double cy = toChildY(ly) - child.y();
        return child.mouseScrolled(cx, cy, scrollX, scrollY);
    }

    @Nullable
    public Widget captured() {
        return captured;
    }

    public void releaseCapture() {
        captured = null;
    }

    @Nullable
    public Widget hoveredChild() {
        return hoveredChild;
    }

    @Override
    @Nullable
    public CursorType cursor() {
        if (hoveredChild != null && hoveredChild.isVisible()) {
            CursorType child = hoveredChild.cursor();
            if (child != null) return child;
        }
        return ownCursor();
    }

    @Nullable
    protected CursorType ownCursor() {
        return null;
    }

    @Override
    public Optional<PositionedIngredient> ingredientUnderMouse() {
        if (hoveredChild != null && hoveredChild.isVisible()) {
            return hoveredChild.ingredientUnderMouse();
        }
        return Optional.empty();
    }

    public Widget deepestHit(double lx, double ly) {
        Widget child = childAt(lx, ly);
        if (child == null) return this;
        double cx = toChildX(lx) - child.x();
        double cy = toChildY(ly) - child.y();
        if (child instanceof Panel panel) {
            return panel.deepestHit(cx, cy);
        }
        return child;
    }

    @Override
    protected boolean dispatchMouseDoubleClicked(double lx, double ly, int button) {
        Widget child = childAt(lx, ly);
        if (child == null) return false;
        double cx = toChildX(lx) - child.x();
        double cy = toChildY(ly) - child.y();
        return child.mouseDoubleClicked(cx, cy, button);
    }

    @Override
    public void collectFocusable(List<Widget> out) {
        if (!isInteractive()) return;
        super.collectFocusable(out);
        for (Widget child : children) {
            child.collectFocusable(out);
        }
    }

    @Override
    @Nullable
    public Widget find(String id) {
        Widget self = super.find(id);
        if (self != null) return self;
        for (Widget child : children) {
            Widget found = child.find(id);
            if (found != null) return found;
        }
        return null;
    }

    @Override
    public void forEachDescendant(Consumer<Widget> visitor) {
        visitor.accept(this);
        for (Widget child : children) {
            child.forEachDescendant(visitor);
        }
    }
}

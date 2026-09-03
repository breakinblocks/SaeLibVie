package com.breakinblocks.saelibvie.ui.core;

import com.breakinblocks.saelibvie.ui.color.Theme;
import com.breakinblocks.saelibvie.ui.color.Themes;
import com.breakinblocks.saelibvie.ui.geom.Anchor;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.widget.ContextMenu;
import com.breakinblocks.saelibvie.ui.widget.MenuItem;
import com.breakinblocks.saelibvie.ui.widget.TextArea;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class UiRoot extends Panel {
    public static final long DOUBLE_CLICK_MILLIS = 300L;
    public static final int MOUSE_POPUP_OFFSET = 3;

    public record Layer(Widget widget, boolean modal, boolean scrim, boolean closeOnOutsideClick, boolean closeOnEscape,
                        @Nullable Runnable onClose, @Nullable EditSession session, boolean contextMenu) {
    }

    public record GhostTarget(Widget widget, Rect screenRect, GhostIngredientTarget target) {
    }

    private Theme theme;
    @Nullable
    private Widget focused;
    private final List<Layer> layers = new ArrayList<>();
    private int screenWidth;
    private int screenHeight;
    private boolean focusRequested;
    @Nullable
    private Runnable closeRequest;
    @Nullable
    private UiGraphics currentGraphics;
    @Nullable
    private Widget layerCaptured;
    private double lastMouseX;
    private double lastMouseY;
    private long lastPressTime;
    private int lastPressButton = -1;
    @Nullable
    private Widget lastPressWidget;
    @Nullable
    private ContextMenu contextMenu;

    public UiRoot() {
        this(Themes.getDefault());
    }

    public UiRoot(Theme theme) {
        this.theme = theme;
    }

    public Theme theme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public int screenWidth() {
        return screenWidth;
    }

    public int screenHeight() {
        return screenHeight;
    }

    public Rect screenRectLocal() {
        return new Rect(-x(), -y(), screenWidth, screenHeight);
    }

    public void onCloseRequest(Runnable handler) {
        this.closeRequest = handler;
    }

    public void requestClose() {
        if (closeRequest != null) {
            closeRequest.run();
        }
    }

    @Nullable
    public UiGraphics currentGraphics() {
        return currentGraphics;
    }

    public double lastMouseX() {
        return lastMouseX;
    }

    public double lastMouseY() {
        return lastMouseY;
    }

    @Override
    public boolean contains(double lx, double ly) {
        return true;
    }

    @Nullable
    public Widget focused() {
        return focused;
    }

    public void setFocus(@Nullable Widget widget) {
        focusRequested = true;
        if (focused == widget) return;
        Widget old = focused;
        focused = widget;
        if (old != null) old.onFocusChanged(false);
        if (widget != null) widget.onFocusChanged(true);
    }

    public void clearFocus() {
        setFocus(null);
        focusRequested = false;
    }

    void onWidgetRemoved(Widget widget) {
        if (focused != null && isDescendantOf(focused, widget)) {
            focused.onFocusChanged(false);
            focused = null;
        }
        if (lastPressWidget != null && isDescendantOf(lastPressWidget, widget)) {
            lastPressWidget = null;
        }
    }

    public void pushLayer(Widget widget) {
        pushLayer(widget, LayerOptions.modal());
    }

    public void pushLayer(Widget widget, boolean modal, boolean scrim, boolean closeOnOutsideClick, boolean closeOnEscape, @Nullable Runnable onClose) {
        pushLayer(widget, new LayerOptions().modal(modal).scrim(scrim).closeOnOutsideClick(closeOnOutsideClick).closeOnEscape(closeOnEscape).onClose(onClose));
    }

    public void pushLayer(Widget widget, LayerOptions options) {
        if (widget.parent != null) {
            widget.parent.remove(widget);
        }
        widget.parent = this;
        widget.onAdded(this);
        layers.add(new Layer(widget, options.modal, options.scrim, options.closeOnOutsideClick, options.closeOnEscape,
                options.onClose, options.session, options.contextMenu));
        if (widget instanceof Panel panel) {
            panel.requestLayout();
        }
        clearHover();
    }

    public void pushLayerAt(Widget widget, Widget anchor, int offsetX, int offsetY, LayerOptions options) {
        int x = anchor.rootX() - contentOriginX() + offsetX;
        int y = anchor.rootY() - contentOriginY() + offsetY;
        widget.setPos(x, y);
        clampWithMargin(widget, options.margin);
        pushLayer(widget, options);
    }

    public void pushLayerAtMouse(Widget widget, LayerOptions options) {
        int x = (int) Math.round(lastMouseX) - contentOriginX() - MOUSE_POPUP_OFFSET;
        int y = (int) Math.round(lastMouseY) - contentOriginY() - MOUSE_POPUP_OFFSET;
        widget.setPos(x, y);
        clampWithMargin(widget, options.margin);
        pushLayer(widget, options);
    }

    public void clampWithMargin(Widget widget, int margin) {
        Rect screen = screenRectLocal().offset(-contentOriginX(), -contentOriginY()).inset(margin);
        if (screen.w() < widget.width() || screen.h() < widget.height()) {
            screen = screenRectLocal().offset(-contentOriginX(), -contentOriginY());
        }
        widget.setBounds(widget.bounds().clampInside(screen));
    }

    public void popLayer() {
        if (layers.isEmpty()) return;
        Layer layer = layers.remove(layers.size() - 1);
        closeLayer(layer);
    }

    public void removeLayer(Widget widget) {
        for (int i = layers.size() - 1; i >= 0; i--) {
            if (layers.get(i).widget() == widget) {
                closeLayer(layers.remove(i));
                return;
            }
        }
    }

    private void closeLayer(Layer layer) {
        Widget widget = layer.widget();
        if (layer.session() != null && !layer.session().isFinished()) {
            layer.session().cancel();
        }
        onWidgetRemoved(widget);
        widget.setHovered(false);
        if (widget instanceof Panel panel) {
            panel.clearHover();
        }
        widget.parent = null;
        widget.onRemoved(this);
        if (layer.contextMenu() && contextMenu != null && contextMenu.ownsLayer(widget)) {
            contextMenu.onLayerClosed(widget);
        }
        if (layer.onClose() != null) {
            layer.onClose().run();
        }
    }

    public boolean hasLayers() {
        return !layers.isEmpty();
    }

    @Nullable
    public Layer topLayer() {
        return layers.isEmpty() ? null : layers.get(layers.size() - 1);
    }

    @Nullable
    public Layer layerOf(Widget widget) {
        for (Layer layer : layers) {
            if (layer.widget() == widget) return layer;
        }
        return null;
    }

    public List<Layer> layers() {
        return List.copyOf(layers);
    }

    public void centerOnScreen(Widget widget) {
        Rect screen = screenRectLocal();
        Rect placed = screen.align(widget.width(), widget.height(), Anchor.CENTER);
        widget.setPos(placed.x() - contentOriginX(), placed.y() - contentOriginY());
    }

    public void clampToScreen(Widget widget) {
        Rect screen = screenRectLocal().offset(-contentOriginX(), -contentOriginY());
        widget.setBounds(widget.bounds().clampInside(screen));
    }

    private boolean modalActive() {
        Layer top = topLayer();
        return top != null && top.modal();
    }

    public ContextMenu openContextMenu(List<MenuItem> items) {
        closeContextMenu();
        ContextMenu menu = new ContextMenu(this, items);
        contextMenu = menu;
        menu.openAtMouse();
        return menu;
    }

    public ContextMenu openContextMenu(List<MenuItem> items, Widget anchor) {
        closeContextMenu();
        ContextMenu menu = new ContextMenu(this, items);
        contextMenu = menu;
        menu.openBelow(anchor);
        return menu;
    }

    public void closeContextMenu() {
        ContextMenu menu = contextMenu;
        if (menu == null) return;
        contextMenu = null;
        for (int i = layers.size() - 1; i >= 0; i--) {
            if (layers.get(i).contextMenu()) {
                closeLayer(layers.remove(i));
            }
        }
        menu.onClosed();
    }

    void contextMenuLayerRemoved() {
        if (contextMenu != null && layers.stream().noneMatch(Layer::contextMenu)) {
            ContextMenu menu = contextMenu;
            contextMenu = null;
            menu.onClosed();
        }
    }

    @Nullable
    public ContextMenu contextMenu() {
        return contextMenu;
    }

    public void renderFrame(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        UiGraphics g = beginFrame(gui, mouseX, mouseY, partialTick);
        endFrame(g);
    }

    public UiGraphics beginFrame(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        UiGraphics g = new UiGraphics(gui, theme, mouseX, mouseY, partialTick);
        currentGraphics = g;
        layoutIfNeeded();
        for (Layer layer : layers) {
            if (layer.widget() instanceof Panel panel) {
                panel.layoutIfNeeded();
            }
        }
        updateHover(mouseX - x(), mouseY - y());
        render(g);
        return g;
    }

    public void renderLayersAndTooltip(UiGraphics g) {
        renderLayers(g);
        g.flushTooltip();
        currentGraphics = null;
    }

    public void endFrame(UiGraphics g) {
        renderLayersAndTooltip(g);
    }

    private void renderLayers(UiGraphics g) {
        if (layers.isEmpty()) return;
        g.pushTranslate(x(), y());
        int z = Painter.Z_OVERLAY;
        for (Layer layer : new ArrayList<>(layers)) {
            g.pushZ(z);
            if (layer.scrim()) {
                Painter.scrim(g, screenRectLocal());
            }
            g.pushTranslate(contentOriginX(), contentOriginY());
            layer.widget().render(g);
            g.popTransform();
            g.popZ();
            z += 50;
        }
        g.popTransform();
    }

    @Override
    public void updateHover(double lx, double ly) {
        lastMouseX = lx;
        lastMouseY = ly;
        Layer top = topLayer();
        if (top != null) {
            Widget widget = top.widget();
            double cx = toChildX(lx) - widget.x();
            double cy = toChildY(ly) - widget.y();
            boolean over = widget.isVisible() && widget.contains(cx, cy);
            widget.setHovered(over);
            if (widget instanceof Panel panel) {
                if (over) panel.updateHover(cx, cy);
                else panel.clearHover();
            } else if (over) {
                widget.mouseMoved(cx, cy);
            }
            if (top.modal()) {
                clearHover();
                return;
            }
        }
        super.updateHover(lx, ly);
    }

    private boolean topLayerHovered() {
        Layer top = topLayer();
        return top != null && top.widget().isHovered();
    }

    @Nullable
    public CursorType requestedCursor() {
        if (topLayerHovered()) {
            return topLayer().widget().cursor();
        }
        if (modalActive()) return null;
        return cursor();
    }

    @Override
    public Optional<PositionedIngredient> ingredientUnderMouse() {
        if (topLayerHovered()) {
            return topLayer().widget().ingredientUnderMouse();
        }
        if (modalActive()) return Optional.empty();
        return super.ingredientUnderMouse();
    }

    public List<GhostTarget> ghostTargets() {
        List<GhostTarget> targets = new ArrayList<>();
        Consumer<Widget> visitor = w -> {
            if (w instanceof GhostIngredientTarget target && w.isInteractive()) {
                targets.add(new GhostTarget(w, w.screenRect(), target));
            }
        };
        forEachDescendant(visitor);
        for (Layer layer : layers) {
            layer.widget().forEachDescendant(visitor);
        }
        return targets;
    }

    @Override
    public void tick() {
        super.tick();
        for (Layer layer : new ArrayList<>(layers)) {
            layer.widget().tick();
        }
    }

    @Nullable
    private Widget resolveHit(double lx, double ly) {
        Layer top = topLayer();
        if (top != null) {
            Widget widget = top.widget();
            double cx = toChildX(lx) - widget.x();
            double cy = toChildY(ly) - widget.y();
            if (widget.isVisible() && widget.contains(cx, cy)) {
                return widget instanceof Panel panel ? panel.deepestHit(cx, cy) : widget;
            }
            if (top.modal()) return null;
        }
        Widget deepest = deepestHit(lx, ly);
        return deepest == this ? null : deepest;
    }

    private boolean layerMouseClicked(double lx, double ly, int button) {
        Layer top = topLayer();
        if (top == null) return false;
        Widget widget = top.widget();
        double cx = toChildX(lx) - widget.x();
        double cy = toChildY(ly) - widget.y();
        if (widget.isVisible() && widget.contains(cx, cy)) {
            if (widget.mouseClicked(cx, cy, button)) {
                layerCaptured = widget;
                return true;
            }
            return top.modal();
        }
        if (top.closeOnOutsideClick()) {
            if (top.contextMenu()) {
                closeContextMenu();
            } else {
                popLayer();
            }
            return true;
        }
        return top.modal();
    }

    public boolean handleMouseClicked(double screenX, double screenY, int button) {
        focusRequested = false;
        double lx = screenX - x();
        double ly = screenY - y();
        lastMouseX = lx;
        lastMouseY = ly;
        Widget hit = resolveHit(lx, ly);
        long now = Util.getMillis();
        if (hit != null && hit == lastPressWidget && button == lastPressButton && now - lastPressTime <= DOUBLE_CLICK_MILLIS) {
            double hx = screenX - hit.screenX();
            double hy = screenY - hit.screenY();
            lastPressTime = 0L;
            lastPressWidget = null;
            lastPressButton = -1;
            if (hit.mouseDoubleClicked(hx, hy, button)) {
                return true;
            }
        } else {
            lastPressTime = now;
            lastPressWidget = hit;
            lastPressButton = button;
        }
        boolean handled = layerMouseClicked(lx, ly, button);
        if (!handled && !modalActive()) {
            handled = mouseClicked(lx, ly, button);
        }
        if (!focusRequested) {
            Widget old = focused;
            focused = null;
            if (old != null) old.onFocusChanged(false);
        }
        return handled;
    }

    public boolean handleMouseReleased(double screenX, double screenY, int button) {
        double lx = screenX - x();
        double ly = screenY - y();
        if (layerCaptured != null) {
            Widget widget = layerCaptured;
            layerCaptured = null;
            double cx = toChildX(lx) - widget.x();
            double cy = toChildY(ly) - widget.y();
            widget.mouseReleased(cx, cy, button);
            return true;
        }
        Layer top = topLayer();
        if (top != null) {
            Widget widget = top.widget();
            double cx = toChildX(lx) - widget.x();
            double cy = toChildY(ly) - widget.y();
            if (widget.isVisible() && widget.contains(cx, cy) && widget.mouseReleased(cx, cy, button)) {
                return true;
            }
            if (top.modal()) return true;
        }
        return mouseReleased(lx, ly, button);
    }

    public boolean handleMouseDragged(double screenX, double screenY, int button, double dx, double dy) {
        double lx = screenX - x();
        double ly = screenY - y();
        lastMouseX = lx;
        lastMouseY = ly;
        if (layerCaptured != null) {
            Widget widget = layerCaptured;
            double cx = toChildX(lx) - widget.x();
            double cy = toChildY(ly) - widget.y();
            return widget.mouseDragged(cx, cy, button, dx / scale(), dy / scale());
        }
        if (modalActive()) return true;
        return mouseDragged(lx, ly, button, dx, dy);
    }

    public boolean handleMouseScrolled(double screenX, double screenY, double scrollX, double scrollY) {
        double lx = screenX - x();
        double ly = screenY - y();
        Layer top = topLayer();
        if (top != null) {
            Widget widget = top.widget();
            double cx = toChildX(lx) - widget.x();
            double cy = toChildY(ly) - widget.y();
            if (widget.isVisible() && widget.contains(cx, cy) && widget.mouseScrolled(cx, cy, scrollX, scrollY)) {
                return true;
            }
            if (top.modal()) return true;
        }
        if (focused != null && focused.isInteractive() && focused instanceof ScrollTarget target && target.acceptsScrollWhileFocused()) {
            if (focused.mouseScrolled(0, 0, scrollX, scrollY)) return true;
        }
        return mouseScrolled(lx, ly, scrollX, scrollY);
    }

    public interface ScrollTarget {
        default boolean acceptsScrollWhileFocused() {
            return true;
        }
    }

    public void handleMouseMoved(double screenX, double screenY) {
        updateHover(screenX - x(), screenY - y());
    }

    private static boolean isEnter(int key) {
        return key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER;
    }

    public boolean handleKeyPressed(int key, int scanCode, int modifiers) {
        if (focused != null) {
            Widget w = focused;
            while (w != null && w != this) {
                if (w.keyPressed(key, scanCode, modifiers)) return true;
                w = w.parent;
            }
        }
        if (isEnter(key) && Screen.hasShiftDown() && !(focused instanceof TextArea)) {
            AcceptsShiftEnter acceptor = findShiftEnterAcceptor();
            if (acceptor != null && acceptor.acceptsOnShiftEnter()) {
                acceptor.onShiftEnter();
                return true;
            }
        }
        Layer top = topLayer();
        if (top != null) {
            if (focused == null || !isDescendantOf(focused, top.widget())) {
                if (top.widget().keyPressed(key, scanCode, modifiers)) return true;
            }
            if (key == InputConstants.KEY_ESCAPE && top.closeOnEscape()) {
                if (top.contextMenu()) {
                    closeContextMenu();
                } else {
                    popLayer();
                }
                return true;
            }
            if (top.modal() && key != InputConstants.KEY_ESCAPE) {
                return key == InputConstants.KEY_TAB && cycleFocus(!hasShift(modifiers));
            }
        }
        if (key == InputConstants.KEY_TAB) {
            return cycleFocus(!hasShift(modifiers));
        }
        return onKeyPressed(key, scanCode, modifiers);
    }

    @Nullable
    private AcceptsShiftEnter findShiftEnterAcceptor() {
        Layer top = topLayer();
        if (top != null) {
            AcceptsShiftEnter found = findShiftEnterIn(top.widget());
            if (found != null || top.modal()) return found;
        }
        return findShiftEnterIn(this);
    }

    @Nullable
    private static AcceptsShiftEnter findShiftEnterIn(Widget widget) {
        AcceptsShiftEnter[] result = new AcceptsShiftEnter[1];
        widget.forEachDescendant(w -> {
            if (result[0] == null && w instanceof AcceptsShiftEnter acceptor && w.isVisible()) {
                result[0] = acceptor;
            }
        });
        return result[0];
    }

    private static boolean hasShift(int modifiers) {
        return (modifiers & 1) != 0;
    }

    public boolean handleKeyReleased(int key, int scanCode, int modifiers) {
        if (focused != null) {
            Widget w = focused;
            while (w != null && w != this) {
                if (w.keyReleased(key, scanCode, modifiers)) return true;
                w = w.parent;
            }
        }
        return false;
    }

    public boolean handleCharTyped(char character, int modifiers) {
        if (focused != null) {
            Widget w = focused;
            while (w != null && w != this) {
                if (w.charTyped(character, modifiers)) return true;
                w = w.parent;
            }
        }
        return false;
    }

    static boolean isDescendantOf(Widget widget, Widget ancestor) {
        Widget w = widget;
        while (w != null) {
            if (w == ancestor) return true;
            w = w.parent;
        }
        return false;
    }

    public boolean cycleFocus(boolean forward) {
        List<Widget> candidates = new ArrayList<>();
        Layer top = topLayer();
        if (top != null && top.modal()) {
            top.widget().collectFocusable(candidates);
        } else {
            collectFocusable(candidates);
            if (top != null) top.widget().collectFocusable(candidates);
        }
        if (candidates.isEmpty()) return false;
        int index = focused == null ? -1 : candidates.indexOf(focused);
        int next;
        if (index < 0) {
            next = forward ? 0 : candidates.size() - 1;
        } else {
            next = Math.floorMod(index + (forward ? 1 : -1), candidates.size());
        }
        setFocus(candidates.get(next));
        return true;
    }

    public boolean handleEscape() {
        Layer top = topLayer();
        if (top != null && top.closeOnEscape()) {
            if (top.contextMenu()) {
                closeContextMenu();
            } else {
                popLayer();
            }
            return true;
        }
        return false;
    }

    @Override
    public void collectFocusable(List<Widget> out) {
        for (Widget child : children) {
            child.collectFocusable(out);
        }
    }

    public void closeAllLayers() {
        closeContextMenu();
        while (!layers.isEmpty()) {
            popLayer();
        }
    }
}

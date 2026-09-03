package com.breakinblocks.saelibvie.ui.core;

import com.breakinblocks.saelibvie.ui.color.Theme;
import com.breakinblocks.saelibvie.ui.geom.Axis;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Widget {
    private Rect bounds = Rect.EMPTY;
    @Nullable
    Panel parent;
    private boolean visible = true;
    private boolean enabled = true;
    private boolean focusable;
    private float alpha = 1f;
    @Nullable
    private String id;
    private LayoutData layoutData = LayoutData.create();
    @Nullable
    private BooleanSupplier visibleWhen;
    @Nullable
    private BooleanSupplier enabledWhen;
    @Nullable
    private Supplier<List<Component>> tooltipLines;
    @Nullable
    private Consumer<UiGraphics> tooltipRenderer;
    @Nullable
    private Theme theme;
    @Nullable
    private Size preferred;
    private boolean hovered;
    private final List<Behavior> behaviors = new ArrayList<>(2);
    @Nullable
    private Object userData;

    protected Widget() {
    }

    protected Widget(Rect bounds) {
        this.bounds = bounds;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Widget> T self() {
        return (T) this;
    }

    public Rect bounds() {
        return bounds;
    }

    public Rect localRect() {
        return new Rect(0, 0, bounds.w(), bounds.h());
    }

    public int x() {
        return bounds.x();
    }

    public int y() {
        return bounds.y();
    }

    public int width() {
        return bounds.w();
    }

    public int height() {
        return bounds.h();
    }

    public <T extends Widget> T bounds(Rect rect) {
        setBounds(rect);
        return self();
    }

    public <T extends Widget> T bounds(int x, int y, int w, int h) {
        return bounds(new Rect(x, y, w, h));
    }

    public <T extends Widget> T pos(int x, int y) {
        setBounds(bounds.at(x, y));
        return self();
    }

    public <T extends Widget> T size(int w, int h) {
        setBounds(bounds.sized(w, h));
        return self();
    }

    public void setBounds(Rect rect) {
        if (rect.equals(bounds)) return;
        boolean resized = rect.w() != bounds.w() || rect.h() != bounds.h();
        bounds = rect;
        if (resized) {
            onResized();
        }
    }

    public void setPos(int x, int y) {
        setBounds(bounds.at(x, y));
    }

    public void setSize(int w, int h) {
        setBounds(bounds.sized(w, h));
    }

    protected void onResized() {
    }

    public <T extends Widget> T id(String id) {
        this.id = id;
        return self();
    }

    @Nullable
    public String id() {
        return id;
    }

    public <T extends Widget> T layout(LayoutData data) {
        this.layoutData = data;
        return self();
    }

    public LayoutData layoutData() {
        return layoutData;
    }

    public <T extends Widget> T preferredSize(int w, int h) {
        this.preferred = new Size(w, h);
        return self();
    }

    public Size preferredSize() {
        if (preferred != null) return preferred;
        return measure();
    }

    protected Size measure() {
        return new Size(bounds.w(), bounds.h());
    }

    public int measureAlong(Axis axis, int crossSize) {
        return 0;
    }

    public <T extends Widget> T visible(boolean visible) {
        this.visible = visible;
        return self();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public <T extends Widget> T visibleWhen(BooleanSupplier condition) {
        this.visibleWhen = condition;
        return self();
    }

    public boolean isVisible() {
        return visible && (visibleWhen == null || visibleWhen.getAsBoolean());
    }

    public <T extends Widget> T enabled(boolean enabled) {
        this.enabled = enabled;
        return self();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public <T extends Widget> T enabledWhen(BooleanSupplier condition) {
        this.enabledWhen = condition;
        return self();
    }

    public boolean isEnabled() {
        return enabled && (enabledWhen == null || enabledWhen.getAsBoolean());
    }

    public boolean isInteractive() {
        return isVisible() && isEnabled();
    }

    public <T extends Widget> T alpha(float alpha) {
        this.alpha = Math.max(0f, Math.min(1f, alpha));
        return self();
    }

    public float alpha() {
        return alpha;
    }

    public <T extends Widget> T theme(@Nullable Theme theme) {
        this.theme = theme;
        return self();
    }

    @Nullable
    public Theme theme() {
        return theme;
    }

    public <T extends Widget> T tooltip(Component line) {
        List<Component> lines = List.of(line);
        this.tooltipLines = () -> lines;
        return self();
    }

    public <T extends Widget> T tooltip(List<Component> lines) {
        List<Component> copy = List.copyOf(lines);
        this.tooltipLines = () -> copy;
        return self();
    }

    public <T extends Widget> T tooltip(Supplier<List<Component>> lines) {
        this.tooltipLines = lines;
        return self();
    }

    public <T extends Widget> T tooltipRenderer(Consumer<UiGraphics> renderer) {
        this.tooltipRenderer = renderer;
        return self();
    }

    public <T extends Widget> T clearTooltip() {
        this.tooltipLines = null;
        this.tooltipRenderer = null;
        return self();
    }

    public boolean hasTooltip() {
        return tooltipLines != null || tooltipRenderer != null;
    }

    public <T extends Widget> T focusable(boolean focusable) {
        this.focusable = focusable;
        return self();
    }

    public boolean isFocusable() {
        return focusable;
    }

    public <T extends Widget> T behavior(Behavior behavior) {
        behaviors.add(behavior);
        return self();
    }

    public void removeBehavior(Behavior behavior) {
        behaviors.remove(behavior);
    }

    public List<Behavior> behaviors() {
        return Collections.unmodifiableList(behaviors);
    }

    public <T extends Widget> T userData(@Nullable Object data) {
        this.userData = data;
        return self();
    }

    @Nullable
    public Object userData() {
        return userData;
    }

    @Nullable
    public Panel parent() {
        return parent;
    }

    @Nullable
    public UiRoot root() {
        Widget w = this;
        while (w != null) {
            if (w instanceof UiRoot root) return root;
            w = w.parent;
        }
        return null;
    }

    public boolean isHovered() {
        return hovered;
    }

    public void setHovered(boolean hovered) {
        if (this.hovered == hovered) return;
        this.hovered = hovered;
        if (hovered) {
            onMouseEnter();
        } else {
            onMouseExit();
        }
    }

    protected void onMouseEnter() {
    }

    protected void onMouseExit() {
    }

    public boolean isFocused() {
        UiRoot root = root();
        return root != null && root.focused() == this;
    }

    public void requestFocus() {
        UiRoot root = root();
        if (root != null) {
            root.setFocus(this);
        }
    }

    public void onFocusChanged(boolean focused) {
    }

    public void invalidateLayout() {
        if (parent != null) {
            parent.requestLayout();
        }
    }

    public boolean contains(double lx, double ly) {
        return localRect().contains(lx, ly);
    }

    public int rootX() {
        int x = bounds.x();
        Panel p = parent;
        while (p != null) {
            x = p.childToLocalX(x);
            if (p instanceof UiRoot) break;
            x += p.x();
            p = p.parent;
        }
        return x;
    }

    public int rootY() {
        int y = bounds.y();
        Panel p = parent;
        while (p != null) {
            y = p.childToLocalY(y);
            if (p instanceof UiRoot) break;
            y += p.y();
            p = p.parent;
        }
        return y;
    }

    public int screenX() {
        UiRoot root = root();
        return rootX() + (root == null ? 0 : root.x());
    }

    public int screenY() {
        UiRoot root = root();
        return rootY() + (root == null ? 0 : root.y());
    }

    public Rect screenRect() {
        return new Rect(screenX(), screenY(), width(), height());
    }

    @Nullable
    public CursorType cursor() {
        return null;
    }

    public Optional<PositionedIngredient> ingredientUnderMouse() {
        return Optional.empty();
    }

    protected boolean dispatchMouseDoubleClicked(double lx, double ly, int button) {
        return false;
    }

    public boolean mouseDoubleClicked(double lx, double ly, int button) {
        if (!isInteractive()) return false;
        if (dispatchMouseDoubleClicked(lx, ly, button)) return true;
        for (Behavior behavior : behaviors) {
            if (behavior.mouseDoubleClicked(this, lx, ly, button)) return true;
        }
        return onMouseDoubleClicked(lx, ly, button);
    }

    protected boolean onMouseDoubleClicked(double lx, double ly, int button) {
        return false;
    }

    public void tick() {
        for (Behavior behavior : behaviors) {
            behavior.tick(this);
        }
        onTick();
    }

    protected void onTick() {
    }

    public final void render(UiGraphics g) {
        if (!isVisible()) return;
        beforeRender(g);
        g.pushTranslate(bounds.x(), bounds.y());
        if (theme != null) g.pushTheme(theme);
        if (alpha < 1f) g.pushAlpha(alpha);
        paint(g);
        for (Behavior behavior : behaviors) {
            behavior.paintOverlay(this, g);
        }
        if (hovered && !g.hasTooltip() && isEnabledForTooltip()) {
            if (tooltipRenderer != null) {
                tooltipRenderer.accept(g);
            } else if (tooltipLines != null) {
                List<Component> lines = tooltipLines.get();
                if (lines != null && !lines.isEmpty()) {
                    g.tooltip(lines);
                }
            }
        }
        if (alpha < 1f) g.popAlpha();
        if (theme != null) g.popTheme();
        g.popTransform();
    }

    protected boolean isEnabledForTooltip() {
        return true;
    }

    protected void beforeRender(UiGraphics g) {
    }

    protected abstract void paint(UiGraphics g);

    protected boolean dispatchMouseClicked(double lx, double ly, int button) {
        return false;
    }

    protected boolean dispatchMouseReleased(double lx, double ly, int button) {
        return false;
    }

    protected boolean dispatchMouseDragged(double lx, double ly, int button, double dx, double dy) {
        return false;
    }

    protected boolean dispatchMouseScrolled(double lx, double ly, double scrollX, double scrollY) {
        return false;
    }

    public boolean mouseClicked(double lx, double ly, int button) {
        if (!isInteractive()) return false;
        if (dispatchMouseClicked(lx, ly, button)) return true;
        for (Behavior behavior : behaviors) {
            if (behavior.mouseClicked(this, lx, ly, button)) return true;
        }
        return onMouseClicked(lx, ly, button);
    }

    protected boolean onMouseClicked(double lx, double ly, int button) {
        return false;
    }

    public boolean mouseReleased(double lx, double ly, int button) {
        if (!isVisible()) return false;
        if (dispatchMouseReleased(lx, ly, button)) return true;
        for (Behavior behavior : behaviors) {
            if (behavior.mouseReleased(this, lx, ly, button)) return true;
        }
        return onMouseReleased(lx, ly, button);
    }

    protected boolean onMouseReleased(double lx, double ly, int button) {
        return false;
    }

    public boolean mouseDragged(double lx, double ly, int button, double dx, double dy) {
        if (!isVisible()) return false;
        if (dispatchMouseDragged(lx, ly, button, dx, dy)) return true;
        for (Behavior behavior : behaviors) {
            if (behavior.mouseDragged(this, lx, ly, button, dx, dy)) return true;
        }
        return onMouseDragged(lx, ly, button, dx, dy);
    }

    protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
        return false;
    }

    public boolean mouseScrolled(double lx, double ly, double scrollX, double scrollY) {
        if (!isInteractive()) return false;
        if (dispatchMouseScrolled(lx, ly, scrollX, scrollY)) return true;
        for (Behavior behavior : behaviors) {
            if (behavior.mouseScrolled(this, lx, ly, scrollX, scrollY)) return true;
        }
        return onMouseScrolled(lx, ly, scrollX, scrollY);
    }

    protected boolean onMouseScrolled(double lx, double ly, double scrollX, double scrollY) {
        return false;
    }

    public void mouseMoved(double lx, double ly) {
        onMouseMoved(lx, ly);
    }

    protected void onMouseMoved(double lx, double ly) {
    }

    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (!isInteractive()) return false;
        for (Behavior behavior : behaviors) {
            if (behavior.keyPressed(this, key, scanCode, modifiers)) return true;
        }
        return onKeyPressed(key, scanCode, modifiers);
    }

    protected boolean onKeyPressed(int key, int scanCode, int modifiers) {
        return false;
    }

    public boolean keyReleased(int key, int scanCode, int modifiers) {
        if (!isInteractive()) return false;
        return onKeyReleased(key, scanCode, modifiers);
    }

    protected boolean onKeyReleased(int key, int scanCode, int modifiers) {
        return false;
    }

    public boolean charTyped(char character, int modifiers) {
        if (!isInteractive()) return false;
        return onCharTyped(character, modifiers);
    }

    protected boolean onCharTyped(char character, int modifiers) {
        return false;
    }

    public void onAdded(Panel newParent) {
    }

    public void onRemoved(Panel oldParent) {
    }

    public void collectFocusable(List<Widget> out) {
        if (isInteractive() && focusable) {
            out.add(this);
        }
    }

    @Nullable
    public Widget find(String id) {
        return id.equals(this.id) ? this : null;
    }

    public void forEachDescendant(Consumer<Widget> visitor) {
        visitor.accept(this);
    }
}

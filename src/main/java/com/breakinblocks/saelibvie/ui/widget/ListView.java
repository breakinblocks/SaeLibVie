package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Axis;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.UiSounds;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ListView<T> extends Widget {
    @FunctionalInterface
    public interface RowRenderer<T> {
        void render(UiGraphics g, T item, int index, Rect row, boolean hovered, boolean selected);
    }

    private Supplier<List<T>> source;
    @Nullable
    private Predicate<T> filter;
    @Nullable
    private List<T> filteredCache;
    @Nullable
    private List<T> filteredFrom;
    private RowRenderer<T> renderer;
    private int rowHeight = 12;
    private int rowGap;
    private int scroll;
    private int selected = -1;
    @Nullable
    private Consumer<T> onSelect;
    @Nullable
    private BiConsumer<T, Integer> onClick;
    @Nullable
    private BiConsumer<T, Integer> onDoubleClick;
    @Nullable
    private Function<T, List<Component>> rowTooltip;
    @Nullable
    private Supplier<Component> emptyText;
    private boolean drawBackground = true;
    private boolean selectable = true;
    private final ScrollBar bar = new ScrollBar(Axis.VERTICAL).bind(this::contentHeight, this::viewHeight, () -> scroll, this::setScroll);
    private int scrollStep = 14;

    public ListView(Supplier<List<T>> items, RowRenderer<T> renderer) {
        this.source = items;
        this.renderer = renderer;
        focusable(true);
    }

    public static <T> ListView<T> simple(Supplier<List<T>> items, Function<T, Component> labeler) {
        return new ListView<>(items, (g, item, index, row, hovered, selected) -> {
            int color = selected ? g.color(ColorToken.ACCENT) : hovered ? g.color(ColorToken.TEXT) : g.color(ColorToken.TEXT_DIM);
            g.text(labeler.apply(item), row.x() + 4, row.y() + (row.h() - 8) / 2, color);
        });
    }

    public ListView<T> items(Supplier<List<T>> items) {
        this.source = items;
        filteredCache = null;
        return this;
    }

    public ListView<T> renderer(RowRenderer<T> renderer) {
        this.renderer = renderer;
        return this;
    }

    public ListView<T> rowHeight(int height) {
        this.rowHeight = Math.max(1, height);
        return this;
    }

    public ListView<T> rowGap(int gap) {
        this.rowGap = gap;
        return this;
    }

    public ListView<T> onSelect(Consumer<T> onSelect) {
        this.onSelect = onSelect;
        return this;
    }

    public ListView<T> onClick(BiConsumer<T, Integer> onClick) {
        this.onClick = onClick;
        return this;
    }

    public ListView<T> onDoubleClick(BiConsumer<T, Integer> onDoubleClick) {
        this.onDoubleClick = onDoubleClick;
        return this;
    }

    public ListView<T> rowTooltip(Function<T, List<Component>> tooltip) {
        this.rowTooltip = tooltip;
        return this;
    }

    public ListView<T> emptyText(Component text) {
        this.emptyText = () -> text;
        return this;
    }

    public ListView<T> background(boolean draw) {
        this.drawBackground = draw;
        return this;
    }

    public ListView<T> selectable(boolean selectable) {
        this.selectable = selectable;
        return this;
    }

    public ListView<T> scrollStep(int step) {
        this.scrollStep = step;
        return this;
    }

    public ListView<T> filter(@Nullable Predicate<T> filter) {
        this.filter = filter;
        this.filteredCache = null;
        setScroll(0);
        return this;
    }

    private List<T> filteredItems() {
        List<T> base = source.get();
        if (filter == null) return base;
        if (filteredCache != null && filteredFrom == base && filteredCache.size() <= base.size()) {
            return filteredCache;
        }
        List<T> out = new ArrayList<>();
        for (T item : base) {
            if (filter.test(item)) out.add(item);
        }
        filteredFrom = base;
        filteredCache = out;
        return out;
    }

    public void invalidateFilter() {
        filteredCache = null;
    }

    public List<T> currentItems() {
        return filteredItems();
    }

    public int selectedIndex() {
        return selected;
    }

    @Nullable
    public T selectedItem() {
        List<T> list = currentItems();
        return selected >= 0 && selected < list.size() ? list.get(selected) : null;
    }

    public void select(int index) {
        List<T> list = currentItems();
        if (index < 0 || index >= list.size()) {
            selected = -1;
            return;
        }
        selected = index;
        revealSelection();
        if (onSelect != null) onSelect.accept(list.get(index));
    }

    public void selectItem(T item) {
        int index = currentItems().indexOf(item);
        selected = index;
        if (index >= 0) revealSelection();
    }

    public void clearSelection() {
        selected = -1;
    }

    private int rowPitch() {
        return rowHeight + rowGap;
    }

    public int contentHeight() {
        int count = currentItems().size();
        return count == 0 ? 0 : count * rowPitch() - rowGap + 2;
    }

    private Rect listRect() {
        Rect r = localRect();
        int barW = bar.isScrollable() ? 5 : 0;
        return r.inset(1, 1, 1 + barW, 1);
    }

    public int viewHeight() {
        return localRect().h() - 2;
    }

    public int maxScroll() {
        return Math.max(0, contentHeight() - viewHeight());
    }

    public void setScroll(int value) {
        scroll = Mth.clamp(value, 0, maxScroll());
    }

    public void revealSelection() {
        if (selected < 0) return;
        int top = selected * rowPitch();
        int bottom = top + rowHeight;
        if (top < scroll) {
            setScroll(top);
        } else if (bottom > scroll + viewHeight()) {
            setScroll(bottom - viewHeight());
        } else {
            setScroll(scroll);
        }
    }

    public int indexAt(double lx, double ly) {
        Rect list = listRect();
        if (!list.contains(lx, ly)) return -1;
        int localY = (int) (ly - list.y() + scroll - 1);
        int index = localY / rowPitch();
        if (localY % rowPitch() >= rowHeight) return -1;
        return index >= 0 && index < currentItems().size() ? index : -1;
    }

    private Rect barRect() {
        Rect r = localRect();
        return new Rect(r.right() - 5, r.y() + 1, 4, r.h() - 2);
    }

    @Override
    protected void paint(UiGraphics g) {
        Rect r = localRect();
        if (drawBackground) {
            Painter.inset(g, r);
        }
        List<T> list = currentItems();
        setScroll(scroll);
        Rect listArea = listRect();
        if (list.isEmpty()) {
            if (emptyText != null) {
                g.centeredText(emptyText.get(), r.centerX(), r.centerY() - 4, g.color(ColorToken.TEXT_DIM));
            }
        } else {
            g.pushScissor(listArea);
            int hovered = isHovered() ? indexAt(g.localMouseX(), g.localMouseY()) : -1;
            int first = Math.max(0, scroll / rowPitch());
            int last = Math.min(list.size() - 1, (scroll + listArea.h()) / rowPitch() + 1);
            for (int i = first; i <= last; i++) {
                int rowY = listArea.y() + 1 + i * rowPitch() - scroll;
                Rect row = new Rect(listArea.x(), rowY, listArea.w(), rowHeight);
                boolean isSelected = selectable && i == selected;
                boolean isHovered = i == hovered;
                if (isSelected) {
                    Painter.selectionTint(g, row);
                } else if (isHovered) {
                    Painter.hoverTint(g, row);
                }
                renderer.render(g, list.get(i), i, row, isHovered, isSelected);
            }
            g.popScissor();
            if (hovered >= 0 && rowTooltip != null && !g.hasTooltip()) {
                List<Component> lines = rowTooltip.apply(list.get(hovered));
                if (lines != null && !lines.isEmpty()) g.tooltip(lines);
            }
        }
        if (bar.isScrollable()) {
            bar.setBounds(barRect());
            bar.setHovered(isHovered() && bar.bounds().contains(g.localMouseX(), g.localMouseY()));
            bar.render(g);
        }
        if (isFocused()) {
            Painter.focusRing(g, r);
        }
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (bar.isScrollable() && barRect().contains(lx, ly)) {
            bar.setBounds(barRect());
            return bar.mouseClicked(lx - bar.x(), ly - bar.y(), button);
        }
        requestFocus();
        int index = indexAt(lx, ly);
        if (index < 0) return true;
        T item = currentItems().get(index);
        if (button == 0 && selectable) {
            selected = index;
            UiSounds.click();
            if (onSelect != null) onSelect.accept(item);
        }
        if (onClick != null) onClick.accept(item, button);
        return true;
    }

    @Override
    protected boolean onMouseDoubleClicked(double lx, double ly, int button) {
        if (onDoubleClick == null) return false;
        int index = indexAt(lx, ly);
        if (index < 0) return false;
        onDoubleClick.accept(currentItems().get(index), button);
        return true;
    }

    @Override
    protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
        return bar.mouseDragged(lx - bar.x(), ly - bar.y(), button, dx, dy);
    }

    @Override
    protected boolean onMouseReleased(double lx, double ly, int button) {
        return bar.mouseReleased(lx - bar.x(), ly - bar.y(), button);
    }

    @Override
    protected boolean onMouseScrolled(double lx, double ly, double scrollX, double scrollY) {
        if (maxScroll() <= 0) return false;
        setScroll(scroll - (int) Math.round(scrollY * scrollStep));
        return true;
    }

    @Override
    protected boolean onKeyPressed(int key, int scanCode, int modifiers) {
        if (!selectable) return false;
        int count = currentItems().size();
        if (count == 0) return false;
        if (key == InputConstants.KEY_DOWN) {
            select(Math.min(count - 1, selected + 1));
            return true;
        }
        if (key == InputConstants.KEY_UP) {
            select(Math.max(0, selected - 1));
            return true;
        }
        if (key == InputConstants.KEY_HOME) {
            select(0);
            return true;
        }
        if (key == InputConstants.KEY_END) {
            select(count - 1);
            return true;
        }
        return false;
    }
}

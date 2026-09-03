package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.LayoutData;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.geom.Align;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.layout.AnchorLayout;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Dropdown<T> extends Button {
    private final Supplier<List<T>> options;
    private final Function<T, Component> labeler;
    private Supplier<T> selected;
    @Nullable
    private T localSelected;
    @Nullable
    private Consumer<T> onSelect;
    private int maxVisibleRows = 8;
    private int rowHeight = 12;
    @Nullable
    private Panel popup;

    public Dropdown(List<T> options, Function<T, Component> labeler) {
        this(() -> options, labeler);
    }

    public Dropdown(Supplier<List<T>> options, Function<T, Component> labeler) {
        this.options = options;
        this.labeler = labeler;
        List<T> initial = options.get();
        this.localSelected = initial.isEmpty() ? null : initial.get(0);
        this.selected = () -> localSelected;
        label(() -> {
            T value = selected.get();
            return value == null ? Component.empty() : labeler.apply(value);
        });
        textAlign(Align.START);
        onPress(b -> toggle());
    }

    public Dropdown<T> bind(Supplier<T> selected, Consumer<T> onSelect) {
        this.selected = selected;
        this.onSelect = onSelect;
        return this;
    }

    public Dropdown<T> onSelect(Consumer<T> onSelect) {
        this.onSelect = onSelect;
        return this;
    }

    public Dropdown<T> maxVisibleRows(int rows) {
        this.maxVisibleRows = Math.max(1, rows);
        return this;
    }

    public Dropdown<T> rowHeight(int height) {
        this.rowHeight = height;
        return this;
    }

    public Dropdown<T> initial(T value) {
        this.localSelected = value;
        return this;
    }

    @Nullable
    public T value() {
        return selected.get();
    }

    public void select(T option) {
        localSelected = option;
        if (onSelect != null) onSelect.accept(option);
    }

    public boolean isOpen() {
        return popup != null;
    }

    public void toggle() {
        if (popup != null) {
            close();
        } else {
            open();
        }
    }

    public void open() {
        UiRoot root = root();
        if (root == null) return;
        List<T> list = options.get();
        int rows = Math.min(maxVisibleRows, Math.max(1, list.size()));
        int h = rows * rowHeight + 4;
        ListView<T> view = ListView.simple(options, labeler)
                .rowHeight(rowHeight)
                .onSelect(item -> {
                    select(item);
                    close();
                });
        view.selectItem(selected.get());
        Panel panel = new Panel().chrome(Panel.Chrome.PANEL).padding(1);
        panel.layout(AnchorLayout.INSTANCE);
        panel.add(view, LayoutData.filled());
        int rootX = rootX() - root.contentOriginX();
        int rootY = rootY() - root.contentOriginY();
        int y = rootY + height();
        if (y + h > root.screenHeight() - root.y()) {
            y = rootY - h;
        }
        panel.setBounds(new Rect(rootX, y, width(), h));
        popup = panel;
        root.pushLayer(panel, false, false, true, true, () -> popup = null);
    }

    public void close() {
        UiRoot root = root();
        if (root != null && popup != null) {
            Panel current = popup;
            popup = null;
            root.removeLayer(current);
        }
    }

    @Override
    protected void paintContent(UiGraphics g, Rect r, Painter.ButtonState state) {
        super.paintContent(g, r.inset(0, 0, 10, 0), state);
        int color = Painter.buttonTextColor(g, state);
        g.text(isOpen() ? "^" : "v", r.right() - 8, r.y() + (r.h() - 8) / 2, color, false);
        if (isOpen()) {
            g.outline(r, g.color(ColorToken.FOCUS));
        }
    }

    @Override
    public void onRemoved(Panel oldParent) {
        close();
    }
}

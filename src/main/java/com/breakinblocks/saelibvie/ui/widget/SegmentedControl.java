package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.UiSounds;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class SegmentedControl<T> extends Widget {
    private final List<T> options;
    private final Function<T, Component> labeler;
    private Supplier<T> selected;
    @Nullable
    private T localSelected;
    @Nullable
    private Consumer<T> onSelect;
    @Nullable
    private ToIntFunction<T> accentColor;
    @Nullable
    private Function<T, List<Component>> optionTooltip;
    private int gap = 1;
    private int columns;

    public SegmentedControl(List<T> options, Function<T, Component> labeler) {
        this.options = List.copyOf(options);
        this.labeler = labeler;
        this.localSelected = this.options.isEmpty() ? null : this.options.get(0);
        this.selected = () -> localSelected;
        this.columns = this.options.size();
    }

    public static <E extends Enum<E>> SegmentedControl<E> ofEnum(Class<E> type, Function<E, Component> labeler) {
        return new SegmentedControl<>(List.of(type.getEnumConstants()), labeler);
    }

    public SegmentedControl<T> bind(Supplier<T> selected, Consumer<T> onSelect) {
        this.selected = selected;
        this.onSelect = onSelect;
        return this;
    }

    public SegmentedControl<T> onSelect(Consumer<T> onSelect) {
        this.onSelect = onSelect;
        return this;
    }

    public SegmentedControl<T> accentColor(ToIntFunction<T> accentColor) {
        this.accentColor = accentColor;
        return this;
    }

    public SegmentedControl<T> optionTooltip(Function<T, List<Component>> tooltip) {
        this.optionTooltip = tooltip;
        return this;
    }

    public SegmentedControl<T> gap(int gap) {
        this.gap = gap;
        return this;
    }

    public SegmentedControl<T> columns(int columns) {
        this.columns = Math.max(1, columns);
        return this;
    }

    public SegmentedControl<T> initial(T value) {
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

    private int rows() {
        return (options.size() + columns - 1) / columns;
    }

    @Override
    protected Size measure() {
        return new Size(width() > 0 ? width() : options.size() * 40, height() > 0 ? height() : rows() * 16 + (rows() - 1) * gap);
    }

    private Rect segmentRect(int index) {
        int rows = rows();
        int col = index % columns;
        int row = index / columns;
        int segW = (width() - gap * (columns - 1)) / columns;
        int segH = (height() - gap * (rows - 1)) / rows;
        return new Rect(col * (segW + gap), row * (segH + gap), segW, segH);
    }

    @Override
    protected void paint(UiGraphics g) {
        T current = selected.get();
        double mx = g.localMouseX();
        double my = g.localMouseY();
        int hoveredIndex = -1;
        for (int i = 0; i < options.size(); i++) {
            T option = options.get(i);
            Rect r = segmentRect(i);
            boolean isSelected = option.equals(current);
            boolean hovered = isHovered() && r.contains(mx, my);
            if (hovered) hoveredIndex = i;
            Painter.ButtonState state = new Painter.ButtonState(isEnabled(), hovered, false, isSelected, false);
            Painter.button(g, r, state);
            int accent = accentColor != null ? accentColor.applyAsInt(option) : g.color(ColorToken.ACCENT);
            if (isSelected) {
                g.fill(r.x() + 1, r.y() + 1, r.right() - 1, r.y() + 3, accent);
            }
            int color = isSelected ? accent : Painter.buttonTextColor(g, state);
            String text = g.fit(labeler.apply(option), r.w() - 4);
            g.centeredText(text, r.centerX(), r.y() + (r.h() - 8) / 2 + (isSelected ? 1 : 0), color, g.theme().textShadow());
        }
        if (hoveredIndex >= 0 && optionTooltip != null && !g.hasTooltip()) {
            List<Component> lines = optionTooltip.apply(options.get(hoveredIndex));
            if (lines != null && !lines.isEmpty()) g.tooltip(lines);
        }
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (button != 0) return false;
        for (int i = 0; i < options.size(); i++) {
            if (segmentRect(i).contains(lx, ly)) {
                UiSounds.click();
                select(options.get(i));
                return true;
            }
        }
        return false;
    }
}

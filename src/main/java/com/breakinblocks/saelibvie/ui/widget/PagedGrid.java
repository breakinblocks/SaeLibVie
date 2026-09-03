package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.item.FluidTextures;
import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.PositionedIngredient;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Axis;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PagedGrid<T> extends Widget {
    @FunctionalInterface
    public interface CellRenderer<T> {
        void render(UiGraphics g, T item, int index, Rect cell, boolean hovered);
    }

    private Supplier<List<T>> items;
    private final CellRenderer<T> renderer;
    private int columns;
    private int cellSize = 18;
    private int scrollRow;
    private int selected = -1;
    @Nullable
    private BiConsumer<T, Integer> onClick;
    @Nullable
    private BiConsumer<T, Integer> onDoubleClick;
    @Nullable
    private Function<T, List<Component>> cellTooltip;
    @Nullable
    private Function<T, Object> ingredientOf;
    private boolean drawCells = true;
    private int hoveredIndex = -1;
    private final ScrollBar bar = new ScrollBar(Axis.VERTICAL).bind(this::totalRows, this::visibleRows, () -> scrollRow, this::setScrollRow);

    public PagedGrid(Supplier<List<T>> items, int columns, CellRenderer<T> renderer) {
        this.items = items;
        this.columns = Math.max(1, columns);
        this.renderer = renderer;
    }

    public static PagedGrid<ItemStack> items(Supplier<List<ItemStack>> stacks, int columns) {
        PagedGrid<ItemStack> grid = new PagedGrid<>(stacks, columns, (g, stack, index, cell, hovered) -> {
            g.itemWithDecorations(stack, cell.x() + 1, cell.y() + 1);
        });
        grid.cellTooltip(stack -> Screen.getTooltipFromItem(Minecraft.getInstance(), stack));
        grid.ingredientOf(stack -> stack.isEmpty() ? null : stack);
        return grid;
    }

    public static void drawFluidCell(UiGraphics g, FluidStack stack, Rect cell) {
        if (stack.isEmpty()) return;
        TextureAtlasSprite sprite = FluidTextures.stillSprite(stack);
        if (sprite == null) return;
        int tint = FluidTextures.color(stack);
        if ((tint >>> 24) == 0) tint |= 0xFF000000;
        g.atlasSprite(sprite, cell.x() + 1, cell.y() + 1, cell.w() - 2, cell.h() - 2, tint);
    }

    public PagedGrid<T> items(Supplier<List<T>> items) {
        this.items = items;
        return this;
    }

    public PagedGrid<T> cellSize(int size) {
        this.cellSize = size;
        return this;
    }

    public PagedGrid<T> columns(int columns) {
        this.columns = Math.max(1, columns);
        return this;
    }

    public PagedGrid<T> onClick(BiConsumer<T, Integer> onClick) {
        this.onClick = onClick;
        return this;
    }

    public PagedGrid<T> onDoubleClick(BiConsumer<T, Integer> onDoubleClick) {
        this.onDoubleClick = onDoubleClick;
        return this;
    }

    public PagedGrid<T> cellTooltip(Function<T, List<Component>> tooltip) {
        this.cellTooltip = tooltip;
        return this;
    }

    public PagedGrid<T> ingredientOf(Function<T, @Nullable Object> function) {
        this.ingredientOf = function;
        return this;
    }

    public PagedGrid<T> drawCells(boolean draw) {
        this.drawCells = draw;
        return this;
    }

    public int selectedIndex() {
        return selected;
    }

    public void select(int index) {
        selected = index >= 0 && index < items.get().size() ? index : -1;
    }

    public void selectItem(@Nullable T item) {
        selected = item == null ? -1 : items.get().indexOf(item);
    }

    @Nullable
    public T selectedItem() {
        List<T> list = items.get();
        return selected >= 0 && selected < list.size() ? list.get(selected) : null;
    }

    public int totalRows() {
        int count = items.get().size();
        return (count + columns - 1) / columns;
    }

    public int visibleRows() {
        return Math.max(1, height() / cellSize);
    }

    public int maxScrollRow() {
        return Math.max(0, totalRows() - visibleRows());
    }

    public void setScrollRow(int row) {
        scrollRow = Mth.clamp(row, 0, maxScrollRow());
    }

    public int firstVisibleIndex() {
        return scrollRow * columns;
    }

    public int lastVisibleIndex() {
        return Math.min(items.get().size(), (scrollRow + visibleRows()) * columns);
    }

    public int gridWidth() {
        return columns * cellSize;
    }

    private Rect barRect() {
        return new Rect(gridWidth() + 2, 0, 4, visibleRows() * cellSize);
    }

    private boolean barVisible() {
        return maxScrollRow() > 0 && width() >= gridWidth() + 6;
    }

    public int indexAt(double lx, double ly) {
        if (lx < 0 || ly < 0 || lx >= gridWidth() || ly >= visibleRows() * cellSize) return -1;
        int col = (int) (lx / cellSize);
        int row = (int) (ly / cellSize) + scrollRow;
        int index = row * columns + col;
        return index < items.get().size() ? index : -1;
    }

    public Rect cellRect(int index) {
        int col = index % columns;
        int row = index / columns - scrollRow;
        return new Rect(col * cellSize, row * cellSize, cellSize, cellSize);
    }

    @Override
    public Optional<PositionedIngredient> ingredientUnderMouse() {
        if (ingredientOf == null || hoveredIndex < 0) return Optional.empty();
        List<T> list = items.get();
        if (hoveredIndex >= list.size()) return Optional.empty();
        Object ingredient = ingredientOf.apply(list.get(hoveredIndex));
        if (ingredient == null) return Optional.empty();
        Rect cell = cellRect(hoveredIndex);
        return Optional.of(new PositionedIngredient(ingredient, new Rect(screenX() + cell.x(), screenY() + cell.y(), cell.w(), cell.h()), false));
    }

    @Override
    protected void paint(UiGraphics g) {
        setScrollRow(scrollRow);
        List<T> list = items.get();
        hoveredIndex = isHovered() ? indexAt(g.localMouseX(), g.localMouseY()) : -1;
        int first = firstVisibleIndex();
        int last = lastVisibleIndex();
        int rows = visibleRows();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int index = (scrollRow + row) * columns + col;
                Rect cell = new Rect(col * cellSize, row * cellSize, cellSize, cellSize);
                if (drawCells) {
                    Painter.slot(g, cell.x(), cell.y(), cellSize);
                }
                if (index >= first && index < last) {
                    boolean isHovered = index == hoveredIndex;
                    renderer.render(g, list.get(index), index, cell, isHovered);
                    if (index == selected) {
                        g.pushZ(200);
                        Painter.selectionTint(g, cell.inset(1));
                        g.outline(cell.inset(1), g.color(ColorToken.ACCENT));
                        g.popZ();
                    } else if (isHovered) {
                        g.pushZ(200);
                        Painter.hoverTint(g, cell.inset(1));
                        g.popZ();
                    }
                }
            }
        }
        if (hoveredIndex >= 0 && cellTooltip != null && !g.hasTooltip()) {
            List<Component> lines = cellTooltip.apply(list.get(hoveredIndex));
            if (lines != null && !lines.isEmpty()) g.tooltip(lines);
        }
        if (barVisible()) {
            bar.setBounds(barRect());
            bar.setHovered(isHovered() && bar.bounds().contains(g.localMouseX(), g.localMouseY()));
            bar.render(g);
        }
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (barVisible() && barRect().grow(1).contains(lx, ly)) {
            bar.setBounds(barRect());
            return bar.mouseClicked(lx - bar.x(), ly - bar.y(), button);
        }
        int index = indexAt(lx, ly);
        if (index < 0) return false;
        if (onClick != null) onClick.accept(items.get().get(index), button);
        return true;
    }

    @Override
    protected boolean onMouseDoubleClicked(double lx, double ly, int button) {
        if (onDoubleClick == null) return false;
        int index = indexAt(lx, ly);
        if (index < 0) return false;
        onDoubleClick.accept(items.get().get(index), button);
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
        if (maxScrollRow() <= 0) return false;
        setScrollRow(scrollRow - (int) Math.signum(scrollY));
        return true;
    }
}

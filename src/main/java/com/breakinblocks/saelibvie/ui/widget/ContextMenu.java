package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.color.Theme;
import com.breakinblocks.saelibvie.ui.core.CursorType;
import com.breakinblocks.saelibvie.ui.core.LayerOptions;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import com.breakinblocks.saelibvie.ui.util.UiSounds;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public final class ContextMenu {
    public static final int ICON_COLUMN = 12;
    public static final int PADDING = 3;
    public static final int COLUMN_GAP = 5;

    private final UiRoot root;
    private final List<MenuItem> items;
    private final List<MenuPanel> panels = new ArrayList<>();
    private int maxRows = Integer.MAX_VALUE;
    private boolean closed;

    public ContextMenu(UiRoot root, List<MenuItem> items) {
        this.root = root;
        this.items = List.copyOf(items);
    }

    public ContextMenu maxRows(int rows) {
        this.maxRows = Math.max(1, rows);
        return this;
    }

    public List<MenuItem> items() {
        return items;
    }

    public boolean isOpen() {
        return !closed && !panels.isEmpty();
    }

    public void openAtMouse() {
        MenuPanel panel = new MenuPanel(this, items, null);
        panels.add(panel);
        root.pushLayerAtMouse(panel, options());
    }

    public void openBelow(Widget anchor) {
        MenuPanel panel = new MenuPanel(this, items, null);
        panels.add(panel);
        root.pushLayerAt(panel, anchor, 0, anchor.height(), options());
    }

    private LayerOptions options() {
        return LayerOptions.modal().scrim(false).closeOnOutsideClick(true).closeOnEscape(true).contextMenu(true);
    }

    void openSubMenu(MenuPanel parent, MenuItem item, Rect rowRect) {
        while (panels.size() > panels.indexOf(parent) + 1) {
            MenuPanel top = panels.remove(panels.size() - 1);
            root.removeLayer(top);
        }
        MenuPanel sub = new MenuPanel(this, item.subItems(), parent);
        sub.measureMenu();
        int x = parent.x() + rowRect.right();
        int y = parent.y() + rowRect.y() - 1;
        int screenRight = root.screenWidth() - root.x() - root.contentOriginX();
        int screenBottom = root.screenHeight() - root.y() - root.contentOriginY();
        if (x + sub.width() > screenRight) {
            x = parent.x() + rowRect.x() - sub.width();
        }
        if (y + sub.height() > screenBottom) {
            y = Math.max(0, screenBottom - sub.height());
        }
        sub.setPos(x, y);
        panels.add(sub);
        root.pushLayer(sub, options());
    }

    public boolean ownsLayer(Widget widget) {
        return panels.contains(widget);
    }

    public void onLayerClosed(Widget widget) {
        panels.remove(widget);
    }

    public void onClosed() {
        closed = true;
        panels.clear();
    }

    public void close() {
        root.closeContextMenu();
    }

    void runItem(MenuItem item, int button) {
        if (!item.isEnabled() || item.action() == null) return;
        if (item.confirmQuestion() != null) {
            Component question = item.confirmQuestion();
            close();
            Confirm.ask(root, question, Component.empty(), yes -> {
                if (yes) item.action().accept(button);
            });
            return;
        }
        if (!item.keepsOpen()) {
            close();
        }
        item.action().accept(button);
    }

    static final class MenuPanel extends Panel {
        private final ContextMenu menu;
        private final List<MenuItem> entries;
        @Nullable
        private final MenuPanel parentPanel;
        private final List<Rect> rowRects = new ArrayList<>();
        private final List<Integer> rowItems = new ArrayList<>();
        private final List<Rect> columnSeparators = new ArrayList<>();
        private boolean hasIcons;
        private int rowHeight = 12;

        MenuPanel(ContextMenu menu, List<MenuItem> entries, @Nullable MenuPanel parentPanel) {
            this.menu = menu;
            this.entries = entries;
            this.parentPanel = parentPanel;
            chrome(Chrome.PANEL);
            measureMenu();
        }

        @Override
        protected CursorType ownCursor() {
            int hovered = hoveredRow();
            return hovered >= 0 && entries.get(rowItems.get(hovered)).isClickable() && entries.get(rowItems.get(hovered)).isEnabled() ? CursorType.HAND : null;
        }

        private int theme(ToIntFunction<Theme> f, int fallback) {
            UiRoot root = root();
            return root != null ? f.applyAsInt(root.theme()) : fallback;
        }

        void measureMenu() {
            var font = TextUtil.font();
            rowHeight = Math.max(10, theme(t -> t.rowHeight(), 12));
            hasIcons = entries.stream().anyMatch(MenuItem::hasIcon);
            int textInset = PADDING + (hasIcons ? ICON_COLUMN : 0);
            int widest = 0;
            for (MenuItem item : entries) {
                if (item.kind() == MenuItem.Kind.SEPARATOR) continue;
                int w = font.width(item.text()) + (item.kind() == MenuItem.Kind.SUBMENU ? 8 : 0);
                widest = Math.max(widest, w);
            }
            int columnWidth = textInset + widest + PADDING;
            int screenHeight = menu.root.screenHeight() > 0 ? menu.root.screenHeight() : 240;
            int rowsPerColumn = Math.max(1, Math.min(menu.maxRows, (screenHeight - 2 * PADDING - 4) / rowHeight));
            int totalRows = 0;
            List<Integer> heights = new ArrayList<>();
            for (MenuItem item : entries) {
                int h = item.kind() == MenuItem.Kind.SEPARATOR ? Math.max(3, rowHeight / 3) : rowHeight;
                heights.add(h);
                totalRows++;
            }
            int columns = Math.max(1, (totalRows + rowsPerColumn - 1) / rowsPerColumn);
            rowRects.clear();
            rowItems.clear();
            columnSeparators.clear();
            int col = 0;
            int rowInCol = 0;
            int y = PADDING + 1;
            int maxColHeight = 0;
            for (int i = 0; i < entries.size(); i++) {
                if (rowInCol >= rowsPerColumn) {
                    maxColHeight = Math.max(maxColHeight, y);
                    col++;
                    rowInCol = 0;
                    y = PADDING + 1;
                }
                int x = PADDING + 1 + col * (columnWidth + COLUMN_GAP);
                rowRects.add(new Rect(x, y, columnWidth, heights.get(i)));
                rowItems.add(i);
                y += heights.get(i);
                rowInCol++;
            }
            maxColHeight = Math.max(maxColHeight, y);
            for (int c = 1; c < columns; c++) {
                int sx = PADDING + 1 + c * (columnWidth + COLUMN_GAP) - COLUMN_GAP / 2 - 1;
                columnSeparators.add(new Rect(sx, PADDING + 1, 1, maxColHeight - PADDING - 1));
            }
            int width = PADDING * 2 + 2 + columns * columnWidth + (columns - 1) * COLUMN_GAP;
            setSize(width, maxColHeight + PADDING + 1);
        }

        private int hoveredRow() {
            UiRoot root = root();
            if (root == null || !isHovered()) return -1;
            UiGraphics g = root.currentGraphics();
            double mx;
            double my;
            if (g != null) {
                mx = g.localMouseX();
                my = g.localMouseY();
            } else {
                return -1;
            }
            for (int i = 0; i < rowRects.size(); i++) {
                if (rowRects.get(i).contains(mx, my)) return i;
            }
            return -1;
        }

        @Override
        protected void paintBackground(UiGraphics g) {
            super.paintBackground(g);
            int hovered = -1;
            double mx = g.localMouseX();
            double my = g.localMouseY();
            for (int i = 0; i < rowRects.size(); i++) {
                if (isHovered() && rowRects.get(i).contains(mx, my)) hovered = i;
            }
            for (int i = 0; i < rowRects.size(); i++) {
                Rect r = rowRects.get(i);
                MenuItem item = entries.get(rowItems.get(i));
                switch (item.kind()) {
                    case SEPARATOR -> g.fill(r.x() + 2, r.centerY(), r.right() - 2, r.centerY() + 1, g.color(ColorToken.BORDER_SOFT));
                    case TITLE -> g.text(item.text(), r.x() + PADDING, r.y() + (r.h() - 8) / 2, g.color(ColorToken.TEXT_TITLE));
                    default -> {
                        boolean enabled = item.isEnabled();
                        if (i == hovered && enabled) {
                            Painter.hoverTint(g, r);
                        }
                        int textX = r.x() + PADDING + (hasIcons ? ICON_COLUMN : 0);
                        if (item.spriteIcon() != null) {
                            g.sprite(item.spriteIcon(), r.x() + PADDING, r.y() + (r.h() - 8) / 2, 8, 8);
                        } else if (item.itemIcon() != null && !item.itemIcon().isEmpty()) {
                            g.pushTransform(r.x() + PADDING, r.y() + (r.h() - 8) / 2, 0.5f);
                            g.item(item.itemIcon(), 0, 0);
                            g.popTransform();
                        }
                        int color = enabled ? g.color(ColorToken.TEXT) : g.color(ColorToken.TEXT_DISABLED);
                        g.text(item.text(), textX, r.y() + (r.h() - 8) / 2, color);
                        if (item.kind() == MenuItem.Kind.SUBMENU) {
                            g.text(">", r.right() - PADDING - 5, r.y() + (r.h() - 8) / 2, color, false);
                        }
                        if (i == hovered && item.tooltip() != null && !item.tooltip().isEmpty()) {
                            g.tooltip(item.tooltip());
                        }
                    }
                }
            }
            for (Rect sep : columnSeparators) {
                g.fill(sep, g.color(ColorToken.BORDER_SOFT));
            }
        }

        @Override
        protected boolean onMouseClicked(double lx, double ly, int button) {
            for (int i = 0; i < rowRects.size(); i++) {
                Rect r = rowRects.get(i);
                if (!r.contains(lx, ly)) continue;
                MenuItem item = entries.get(rowItems.get(i));
                if (!item.isClickable() || !item.isEnabled()) return true;
                UiSounds.click();
                if (item.kind() == MenuItem.Kind.SUBMENU) {
                    menu.openSubMenu(this, item, r);
                } else {
                    menu.runItem(item, button);
                }
                return true;
            }
            return true;
        }

        @Override
        public void updateHover(double lx, double ly) {
            super.updateHover(lx, ly);
            if (isHovered()) {
                onMouseMoved(lx, ly);
            }
        }

        @Override
        protected void onMouseMoved(double lx, double ly) {
            for (int i = 0; i < rowRects.size(); i++) {
                Rect r = rowRects.get(i);
                if (!r.contains(lx, ly)) continue;
                MenuItem item = entries.get(rowItems.get(i));
                if (item.kind() == MenuItem.Kind.SUBMENU && item.isEnabled()) {
                    int index = menu.panels.indexOf(this);
                    boolean alreadyOpen = index >= 0 && menu.panels.size() > index + 1 && menu.panels.get(index + 1).entries == item.subItems();
                    if (!alreadyOpen) {
                        menu.openSubMenu(this, item, r);
                    }
                }
                return;
            }
        }
    }
}

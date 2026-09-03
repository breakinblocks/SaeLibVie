package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.LayoutData;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.layout.AnchorLayout;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.UiSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TabPanel extends Panel {
    public record Tab(Supplier<Component> title, Widget content, @Nullable BooleanSupplier available) {
        public boolean isAvailable() {
            return available == null || available.getAsBoolean();
        }
    }

    private final List<Tab> tabs = new ArrayList<>();
    private int selected;
    private int stripHeight = 14;
    private int tabPadding = 6;
    @Nullable
    private Consumer<Integer> onChange;

    public TabPanel() {
        layout(AnchorLayout.INSTANCE);
    }

    public TabPanel(Rect bounds) {
        this();
        bounds(bounds);
    }

    public TabPanel stripHeight(int height) {
        this.stripHeight = height;
        requestLayout();
        return this;
    }

    public TabPanel onChange(Consumer<Integer> onChange) {
        this.onChange = onChange;
        return this;
    }

    public TabPanel addTab(Component title, Widget content) {
        return addTab(() -> title, content, null);
    }

    public TabPanel addTab(Supplier<Component> title, Widget content, @Nullable BooleanSupplier available) {
        Tab tab = new Tab(title, content, available);
        tabs.add(tab);
        int index = tabs.size() - 1;
        content.layout(LayoutData.filled());
        content.visibleWhen(() -> selected == index && tab.isAvailable());
        add(content);
        return this;
    }

    public int selectedIndex() {
        return selected;
    }

    public void select(int index) {
        if (index < 0 || index >= tabs.size() || !tabs.get(index).isAvailable()) return;
        if (selected != index) {
            selected = index;
            requestLayout();
            if (onChange != null) onChange.accept(index);
        }
    }

    public void selectNext(int delta) {
        if (tabs.isEmpty()) return;
        int index = selected;
        for (int i = 0; i < tabs.size(); i++) {
            index = Math.floorMod(index + delta, tabs.size());
            if (tabs.get(index).isAvailable()) {
                select(index);
                return;
            }
        }
    }

    public List<Tab> tabs() {
        return List.copyOf(tabs);
    }

    @Override
    public Rect contentRect() {
        return super.contentRect().splitTop(stripHeight);
    }

    private List<Rect> tabRects() {
        List<Rect> rects = new ArrayList<>();
        Rect strip = super.contentRect().topPart(stripHeight);
        Font font = Minecraft.getInstance().font;
        int x = strip.x();
        for (Tab tab : tabs) {
            if (!tab.isAvailable()) {
                rects.add(Rect.EMPTY);
                continue;
            }
            int w = font.width(tab.title().get()) + tabPadding * 2;
            rects.add(new Rect(x, strip.y(), w, stripHeight));
            x += w + 1;
        }
        return rects;
    }

    @Override
    protected void paintBackground(UiGraphics g) {
        super.paintBackground(g);
        List<Rect> rects = tabRects();
        double mx = g.localMouseX();
        double my = g.localMouseY();
        for (int i = 0; i < tabs.size(); i++) {
            Rect r = rects.get(i);
            if (r.isEmpty()) continue;
            boolean isSelected = i == selected;
            boolean hovered = isHovered() && r.contains(mx, my);
            Painter.button(g, r, new Painter.ButtonState(true, hovered, false, isSelected, false));
            int color = Painter.buttonTextColor(g, new Painter.ButtonState(true, hovered, false, isSelected, false));
            g.text(tabs.get(i).title().get(), r.x() + tabPadding, r.y() + (r.h() - 8) / 2, color);
        }
        Rect content = contentRect();
        g.fill(content.x(), content.y() - 1, content.right(), content.y(), g.color(ColorToken.BORDER_SOFT));
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (button != 0) return false;
        List<Rect> rects = tabRects();
        for (int i = 0; i < rects.size(); i++) {
            if (rects.get(i).contains(lx, ly)) {
                UiSounds.click();
                select(i);
                return true;
            }
        }
        return false;
    }
}

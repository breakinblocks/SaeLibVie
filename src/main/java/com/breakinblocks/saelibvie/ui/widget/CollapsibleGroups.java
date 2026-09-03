package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.LayoutData;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.layout.LinearLayout;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import com.breakinblocks.saelibvie.ui.util.UiSounds;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CollapsibleGroups extends Panel {
    public static final int HEADER_HEIGHT = 14;
    public static final int SPACER = 4;

    public record Group(String key, Component header, @Nullable Supplier<List<Component>> tooltip, boolean defaultCollapsed, List<Widget> rows) {
        public static Group of(String key, Component header, List<Widget> rows) {
            return new Group(key, header, null, false, rows);
        }
    }

    private final List<Group> groups = new ArrayList<>();
    private final Map<String, Boolean> collapsed = new HashMap<>();
    private final List<Header> headers = new ArrayList<>();
    private int rowGap;

    public CollapsibleGroups() {
        layout(LinearLayout.vertical(0));
    }

    public CollapsibleGroups rowGap(int gap) {
        this.rowGap = gap;
        return this;
    }

    public CollapsibleGroups setGroups(List<Group> newGroups) {
        groups.clear();
        groups.addAll(newGroups);
        rebuild();
        return this;
    }

    public List<Group> groups() {
        return groups;
    }

    public int groupCount() {
        return groups.size();
    }

    public boolean isCollapsed(String key) {
        Boolean value = collapsed.get(key);
        if (value != null) return value;
        for (Group group : groups) {
            if (group.key().equals(key)) return group.defaultCollapsed();
        }
        return false;
    }

    public void setCollapsed(String key, boolean value) {
        collapsed.put(key, value);
        applyVisibility();
    }

    public void toggle(String key) {
        setCollapsed(key, !isCollapsed(key));
    }

    public void rebuild() {
        clear();
        headers.clear();
        boolean showHeaders = groups.size() > 1;
        boolean first = true;
        for (Group group : groups) {
            if (showHeaders) {
                if (!first) {
                    add(new Spacer(1, SPACER), LayoutData.filled().height(SPACER));
                }
                Header header = new Header(group);
                headers.add(header);
                add(header, LayoutData.filled().height(HEADER_HEIGHT));
            }
            for (Widget row : group.rows()) {
                add(row, row.layoutData().isExplicit() ? row.layoutData() : LayoutData.filled().height(row.height()));
                if (rowGap > 0) {
                    add(new Spacer(1, rowGap), LayoutData.filled().height(rowGap));
                }
            }
            first = false;
        }
        applyVisibility();
        requestLayout();
    }

    private void applyVisibility() {
        boolean showHeaders = groups.size() > 1;
        for (Group group : groups) {
            boolean hidden = showHeaders && isCollapsed(group.key());
            for (Widget row : group.rows()) {
                row.setVisible(!hidden);
            }
        }
        requestLayout();
    }

    public void expandAll() {
        boolean any = false;
        for (Group group : groups) {
            if (isCollapsed(group.key())) any = true;
            collapsed.put(group.key(), false);
        }
        if (any) {
            applyVisibility();
            scrollToTop();
        }
    }

    public void collapseAll() {
        for (Group group : groups) {
            collapsed.put(group.key(), true);
        }
        applyVisibility();
        scrollToTop();
    }

    private void scrollToTop() {
        Panel p = parent();
        while (p != null && !(p instanceof ScrollPanel)) {
            p = p.parent();
        }
        if (p instanceof ScrollPanel scroll) {
            scroll.scrollToTop();
        }
    }

    public Button expandAllButton() {
        return new Button(Component.literal("+"), this::expandAll)
                .tooltip(List.of(Component.translatable("gui.expand_all"), TextUtil.hotkey("="), TextUtil.hotkey("+")));
    }

    public Button collapseAllButton() {
        return new Button(Component.literal("-"), this::collapseAll)
                .tooltip(List.of(Component.translatable("gui.collapse_all"), TextUtil.hotkey("-")));
    }

    public boolean handleHotkey(int key) {
        if (key == InputConstants.KEY_EQUALS || key == InputConstants.KEY_ADD) {
            expandAll();
            return true;
        }
        if (key == InputConstants.KEY_MINUS || key == GLFW.GLFW_KEY_KP_SUBTRACT) {
            collapseAll();
            return true;
        }
        return false;
    }

    private final class Header extends Widget {
        private final Group group;

        Header(Group group) {
            this.group = group;
            if (group.tooltip() != null) {
                tooltip(group.tooltip());
            }
        }

        @Override
        protected void paint(UiGraphics g) {
            Rect r = localRect();
            Painter.button(g, r, new Painter.ButtonState(true, isHovered(), false, false, false));
            boolean open = !isCollapsed(group.key());
            String marker = open ? "▼ " : "▶ ";
            int markerColor = open ? g.color(ColorToken.POSITIVE) : g.color(ColorToken.NEGATIVE);
            g.text(marker, 3, (r.h() - 8) / 2, markerColor, false);
            int markerWidth = g.textWidth(marker);
            g.text(g.fit(group.header(), r.w() - 6 - markerWidth), 3 + markerWidth, (r.h() - 8) / 2, g.color(ColorToken.TEXT_TITLE), g.theme().textShadow());
        }

        @Override
        protected boolean onMouseClicked(double lx, double ly, int button) {
            UiSounds.click();
            toggle(group.key());
            return true;
        }
    }
}

package com.breakinblocks.saelibvie.ui;

import com.breakinblocks.saelibvie.ui.color.Themes;
import com.breakinblocks.saelibvie.ui.core.LayoutData;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.geom.Anchor;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.layout.AnchorLayout;
import com.breakinblocks.saelibvie.ui.layout.LinearLayout;
import com.breakinblocks.saelibvie.ui.widget.Slider;
import com.breakinblocks.saelibvie.ui.widget.Spacer;
import com.breakinblocks.saelibvie.ui.widget.TabPanel;
import com.breakinblocks.saelibvie.ui.widget.Window;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SliderDispatchTest {
    private final double[] last = new double[1];
    private Window window;
    private TabPanel tabs;

    private UiRoot buildTree(Slider slider) {
        UiRoot root = new UiRoot(Themes.DARK);
        root.setBounds(new Rect(0, 0, 400, 300));
        root.setScreenSize(400, 300);
        root.layout(AnchorLayout.INSTANCE);

        window = new Window(Component.literal("Test"));
        window.setBounds(new Rect(30, 20, 300, 200));

        tabs = new TabPanel();
        Panel page = new Panel().layout(LinearLayout.vertical(4)).padding(4);
        page.add(new Spacer(10, 30), LayoutData.create().size(10, 30));
        page.add(slider, LayoutData.create().width(140).height(12));
        tabs.addTab(Component.literal("Tab"), page);
        window.add(tabs, LayoutData.filled());
        root.add(window);
        root.add(new Spacer(20, 20), LayoutData.anchored(Anchor.BOTTOM_RIGHT));
        root.layoutNow();
        return root;
    }

    @Test
    void anchorLayoutLeavesUnmanagedChildrenAlone() {
        buildTree(new Slider(0, 1));
        assertEquals(new Rect(30, 20, 300, 200), window.bounds());
        assertTrue(tabs.width() > 200 && tabs.height() > 100, "tabs should fill the window content, got " + tabs.bounds());
    }

    @Test
    void clickAndDragMovesNestedSlider() {
        Slider slider = new Slider(0.2, 1.0, 1.0, v -> last[0] = v).step(0.05);
        UiRoot root = buildTree(slider);

        int sx = slider.screenX();
        int sy = slider.screenY();
        assertTrue(sx > 30 && sy > 20, "slider should sit inside the window, got " + sx + "," + sy);

        double clickX = sx + 4;
        double clickY = sy + 6;
        assertTrue(root.handleMouseClicked(clickX, clickY, 0), "click should be consumed");
        assertTrue(slider.value() < 0.3, "clicking the left edge should move to the minimum, got " + slider.value());

        double dragX = sx + 70;
        assertTrue(root.handleMouseDragged(dragX, clickY, 0, dragX - clickX, 0), "drag should be routed to the captured slider");
        double mid = slider.value();
        assertTrue(mid > 0.5 && mid < 0.7, "dragging to the middle should land near 0.6, got " + mid);
        assertEquals(mid, last[0], 1e-9);

        assertTrue(root.handleMouseReleased(dragX, clickY, 0));
        double endX = sx + 139;
        root.handleMouseClicked(endX, clickY, 0);
        root.handleMouseReleased(endX, clickY, 0);
        assertEquals(1.0, slider.value(), 1e-9);
    }

    @Test
    void draggedWindowKeepsPositionAcrossLayoutPasses() {
        Slider slider = new Slider(0, 1);
        UiRoot root = buildTree(slider);
        int barY = window.screenY() + 5;
        int barX = window.screenX() + 40;
        assertTrue(root.handleMouseClicked(barX, barY, 0));
        assertTrue(root.handleMouseDragged(barX + 50, barY + 30, 0, 50, 30));
        root.handleMouseReleased(barX + 50, barY + 30, 0);
        assertEquals(new Rect(80, 50, 300, 200), window.bounds());
        root.requestLayout();
        root.layoutNow();
        assertEquals(new Rect(80, 50, 300, 200), window.bounds());
    }
}

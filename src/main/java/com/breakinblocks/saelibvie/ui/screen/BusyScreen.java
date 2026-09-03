package com.breakinblocks.saelibvie.ui.screen;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.LayoutData;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Anchor;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.layout.AnchorLayout;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.ClientTasks;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BusyScreen extends SaeScreen {
    private final List<Component> lines = new ArrayList<>();
    @Nullable
    private Runnable onFinish;
    private boolean finished;

    public BusyScreen(Component title) {
        super(title);
    }

    public BusyScreen(Component title, @Nullable Screen parent) {
        super(title, parent);
    }

    public BusyScreen onFinish(Runnable hook) {
        this.onFinish = hook;
        return this;
    }

    public synchronized void setLines(List<Component> newLines) {
        lines.clear();
        lines.addAll(newLines);
    }

    public synchronized void addLine(Component line) {
        lines.add(line);
    }

    public synchronized void clearLines() {
        lines.clear();
    }

    private synchronized List<Component> snapshot() {
        return new ArrayList<>(lines);
    }

    public void finish() {
        ClientTasks.later(() -> {
            if (finished) return;
            finished = true;
            if (isCurrent()) {
                close();
            }
            if (onFinish != null) onFinish.run();
        });
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    protected void build(UiRoot root) {
        root.layout(AnchorLayout.INSTANCE);
        Panel panel = new Panel().chrome(Panel.Chrome.WINDOW).padding(8);
        panel.setSize(Math.min(width - 20, 220), 60);
        panel.background((p, g) -> paintBody(g, p));
        root.add(panel, LayoutData.anchored(Anchor.CENTER));
    }

    private void paintBody(UiGraphics g, Widget panel) {
        int y = 2;
        g.centeredText(getTitle(), panel.width() / 2, y, g.color(ColorToken.TEXT_TITLE));
        y += 12;
        for (Component line : snapshot()) {
            g.text(line, 2, y, g.color(ColorToken.TEXT_DIM));
            y += 10;
        }
        Rect bar = new Rect(4, panel.height() - 12, panel.width() - 8, 6);
        Painter.inset(g, bar);
        int span = Math.max(8, bar.w() / 4);
        long t = Util.getMillis() / 8L;
        int travel = bar.w() - 2 - span;
        int phase = travel <= 0 ? 0 : (int) (t % (travel * 2L));
        int offset = phase > travel ? travel * 2 - phase : phase;
        g.fill(new Rect(bar.x() + 1 + offset, bar.y() + 1, span, bar.h() - 2), g.color(ColorToken.ACCENT));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void onEscape() {
    }

    public static BusyScreen open(Component title) {
        Minecraft mc = Minecraft.getInstance();
        BusyScreen screen = new BusyScreen(title, mc.screen);
        mc.setScreen(screen);
        return screen;
    }
}

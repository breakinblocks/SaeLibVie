package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import net.minecraft.network.chat.Component;

public class ConfirmButton extends Button {
    private final Component idleLabel;
    private final Component confirmLabel;
    private boolean armed;
    private int armedTicks;
    private int timeoutTicks = 60;
    private Runnable confirmed = () -> {
    };

    public ConfirmButton(Component idleLabel, Component confirmLabel, Runnable onConfirm) {
        super(idleLabel);
        this.idleLabel = idleLabel;
        this.confirmLabel = confirmLabel;
        this.confirmed = onConfirm;
        label(() -> armed ? confirmLabel : idleLabel);
        onPress(b -> {
            if (armed) {
                armed = false;
                confirmed.run();
            } else {
                armed = true;
                armedTicks = 0;
            }
        });
    }

    public ConfirmButton timeout(int ticks) {
        this.timeoutTicks = ticks;
        return this;
    }

    public boolean isArmed() {
        return armed;
    }

    public void disarm() {
        armed = false;
    }

    @Override
    protected void onTick() {
        if (armed && timeoutTicks > 0 && ++armedTicks >= timeoutTicks) {
            armed = false;
        }
    }

    @Override
    public void onFocusChanged(boolean focused) {
        if (!focused) armed = false;
    }

    @Override
    protected void paint(UiGraphics g) {
        super.paint(g);
        if (armed) {
            Rect r = localRect();
            g.outline(r, g.color(ColorToken.NEGATIVE));
        }
    }

    @Override
    protected void paintContent(UiGraphics g, Rect r, Painter.ButtonState state) {
        if (armed) {
            Component text = currentLabel();
            if (text != null) {
                g.centeredText(text, r.centerX(), r.y() + (r.h() - 8) / 2, g.color(ColorToken.NEGATIVE), g.theme().textShadow());
            }
            return;
        }
        super.paintContent(g, r, state);
    }
}

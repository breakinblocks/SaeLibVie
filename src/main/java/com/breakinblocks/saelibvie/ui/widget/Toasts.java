package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.color.Themes;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class Toasts {
    public static final long DEFAULT_DURATION = 5000L;

    private Toasts() {
    }

    public static void info(Component title, Component subtitle) {
        show(new SaeToast(title, subtitle, null, ItemStack.EMPTY, false));
    }

    public static void error(Component title, Component subtitle) {
        show(new SaeToast(title, subtitle, null, new ItemStack(Items.BARRIER), true));
    }

    public static void show(Toast toast) {
        Minecraft.getInstance().getToastManager().addToast(toast);
    }

    public static class SaeToast implements Toast {
        private final Component title;
        private final Component subtitle;
        @Nullable
        private final Identifier sprite;
        private final ItemStack icon;
        private final boolean error;
        private long duration = DEFAULT_DURATION;
        private Visibility wantedVisibility = Visibility.SHOW;

        public SaeToast(Component title, Component subtitle, @Nullable Identifier sprite, ItemStack icon, boolean error) {
            this.title = title;
            this.subtitle = subtitle;
            this.sprite = sprite;
            this.icon = icon;
            this.error = error;
        }

        public SaeToast duration(long millis) {
            this.duration = millis;
            return this;
        }

        public boolean important() {
            return error;
        }

        @Override
        public Visibility getWantedVisibility() {
            return wantedVisibility;
        }

        @Override
        public void update(ToastManager manager, long fullyVisibleForMs) {
            double scaled = duration * manager.getNotificationDisplayTimeMultiplier();
            wantedVisibility = fullyVisibleForMs >= scaled ? Visibility.HIDE : Visibility.SHOW;
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
            UiGraphics g = new UiGraphics(graphics, Themes.getDefault(), -1, -1, 0f);
            Rect r = new Rect(0, 0, width(), height());
            Painter.panel(g, r);
            boolean hasIcon = sprite != null || !icon.isEmpty();
            int textX = hasIcon ? 26 : 6;
            if (sprite != null) {
                g.sprite(sprite, 6, (height() - 16) / 2, 16, 16);
            } else if (!icon.isEmpty()) {
                g.item(icon, 6, (height() - 16) / 2);
            }
            int titleColor = important() ? g.color(ColorToken.NEGATIVE) : g.color(ColorToken.ACCENT);
            g.text(title, textX, 6, titleColor);
            List<FormattedCharSequence> lines = g.wrap(subtitle, width() - textX - 4);
            int y = 17;
            for (int i = 0; i < Math.min(lines.size(), 2); i++) {
                g.text(lines.get(i), textX, y, g.color(ColorToken.TEXT), false);
                y += 10;
            }
        }
    }
}

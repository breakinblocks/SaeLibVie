package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.core.EditSession;
import com.breakinblocks.saelibvie.ui.core.LayerOptions;
import com.breakinblocks.saelibvie.ui.core.LayoutData;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.geom.Align;
import com.breakinblocks.saelibvie.ui.layout.LinearLayout;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public final class Confirm {
    private Confirm() {
    }

    public static void ask(UiRoot root, Component title, Component description, Runnable onYes) {
        ask(root, title, description, yes -> {
            if (yes) onYes.run();
        });
    }

    public static void ask(UiRoot root, Component title, Component description, Consumer<Boolean> answer) {
        Dialog dialog = new Dialog(root, title, description, answer);
        root.centerOnScreen(dialog);
        root.pushLayer(dialog, LayerOptions.modal().scrim(true).closeOnOutsideClick(false).closeOnEscape(true).session(dialog.session));
    }

    private static final class Dialog extends Panel {
        private final UiRoot host;
        private final EditSession session;

        Dialog(UiRoot host, Component title, Component description, Consumer<Boolean> answer) {
            this.host = host;
            this.session = new EditSession(result -> {
                answer.accept(result);
                host.requestLayout();
            });
            chrome(Chrome.WINDOW);
            padding(8);
            layout(LinearLayout.vertical(6));
            int width = Math.max(160, Math.min(host.screenWidth() - 40, TextUtil.font().width(title) + 40));
            add(new Label(title).title(), LayoutData.filled().height(10));
            TextBlock body = new TextBlock(description);
            int bodyHeight = description.getString().isEmpty() ? 0 : body.measureHeight(width - 16);
            if (bodyHeight > 0) {
                add(body, LayoutData.filled().height(bodyHeight));
            }
            Panel buttons = new Panel().layout(LinearLayout.horizontal(6).mainAlign(Align.END));
            buttons.add(new Button(Component.translatable("gui.yes"), () -> finish(true))
                    .tooltip(List.of(TextUtil.hotkey("Enter"), TextUtil.hotkey("Y"))).size(60, 14));
            buttons.add(new Button(Component.translatable("gui.no"), () -> finish(false))
                    .tooltip(List.of(TextUtil.hotkey("Esc"), TextUtil.hotkey("N"))).size(60, 14));
            add(buttons, LayoutData.filled().height(14));
            setSize(width, 8 * 2 + 10 + 6 + (bodyHeight > 0 ? bodyHeight + 6 : 0) + 14 + 4);
        }

        private void finish(boolean yes) {
            session.finish(yes);
            host.removeLayer(this);
        }

        @Override
        protected boolean onKeyPressed(int key, int scanCode, int modifiers) {
            if (key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER || key == InputConstants.KEY_Y) {
                finish(true);
                return true;
            }
            if (key == InputConstants.KEY_N) {
                finish(false);
                return true;
            }
            return false;
        }
    }
}

package com.breakinblocks.saelibvie.ui.widget;

import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class SearchBox extends TextField {
    private boolean focusOnShow = true;
    private boolean wasVisible;

    public SearchBox() {
        hint(Component.translatable("gui.search_box"));
        commitOnFocusLoss(false);
    }

    public SearchBox onSearch(Consumer<String> consumer) {
        responder(consumer);
        return this;
    }

    public SearchBox focusOnShow(boolean value) {
        this.focusOnShow = value;
        return this;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible && !wasVisible && focusOnShow) {
            requestFocus();
        }
        wasVisible = visible;
    }

    @Override
    protected boolean onEnter() {
        return true;
    }
}

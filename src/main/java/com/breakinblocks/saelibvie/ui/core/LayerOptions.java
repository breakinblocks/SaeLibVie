package com.breakinblocks.saelibvie.ui.core;

import org.jetbrains.annotations.Nullable;

public final class LayerOptions {
    boolean modal = true;
    boolean scrim = true;
    boolean closeOnOutsideClick = true;
    boolean closeOnEscape = true;
    @Nullable
    Runnable onClose;
    @Nullable
    EditSession session;
    boolean contextMenu;
    int margin = 10;

    public static LayerOptions modal() {
        return new LayerOptions();
    }

    public static LayerOptions popup() {
        return new LayerOptions().modal(false).scrim(false);
    }

    public LayerOptions modal(boolean value) {
        modal = value;
        return this;
    }

    public LayerOptions scrim(boolean value) {
        scrim = value;
        return this;
    }

    public LayerOptions closeOnOutsideClick(boolean value) {
        closeOnOutsideClick = value;
        return this;
    }

    public LayerOptions closeOnEscape(boolean value) {
        closeOnEscape = value;
        return this;
    }

    public LayerOptions onClose(@Nullable Runnable value) {
        onClose = value;
        return this;
    }

    public LayerOptions session(@Nullable EditSession value) {
        session = value;
        return this;
    }

    public LayerOptions contextMenu(boolean value) {
        contextMenu = value;
        return this;
    }

    public LayerOptions margin(int value) {
        margin = value;
        return this;
    }

    public int margin() {
        return margin;
    }

    @Nullable
    public EditSession session() {
        return session;
    }
}

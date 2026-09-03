package com.breakinblocks.saelibvie.ui.core;

public interface AcceptsShiftEnter {
    default boolean acceptsOnShiftEnter() {
        return true;
    }

    void onShiftEnter();
}

package com.breakinblocks.saelibvie.ui.core;

public interface GhostIngredientTarget {
    boolean acceptsGhost(Object ingredient);

    void acceptGhost(Object ingredient);
}

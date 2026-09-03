package com.breakinblocks.saelibvie.ui.core;

import com.breakinblocks.saelibvie.ui.geom.Rect;

import java.util.Optional;

public record PositionedIngredient(Object ingredient, Rect screenRect, boolean showTooltip) {
    public static PositionedIngredient of(Object ingredient, Widget widget) {
        return of(ingredient, widget, true);
    }

    public static PositionedIngredient of(Object ingredient, Widget widget, boolean showTooltip) {
        return new PositionedIngredient(ingredient, widget.screenRect(), showTooltip);
    }

    public static Optional<PositionedIngredient> optional(Object ingredient, Widget widget) {
        return Optional.of(of(ingredient, widget));
    }
}

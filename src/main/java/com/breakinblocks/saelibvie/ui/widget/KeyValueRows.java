package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class KeyValueRows extends Widget {
    public record Row(@Nullable Supplier<Component> label, @Nullable Supplier<Component> value, @Nullable IntSupplier color, int gapAfter) {
        public boolean isGap() {
            return label == null && value == null;
        }
    }

    private final List<Row> rows = new ArrayList<>();
    private int rowHeight = 9;
    private boolean uppercaseLabels;
    private boolean colonSuffix = true;
    private ColorToken labelToken = ColorToken.TEXT_DIM;
    private ColorToken valueToken = ColorToken.TEXT;

    public KeyValueRows() {
    }

    public KeyValueRows row(Component label, Supplier<Component> value) {
        rows.add(new Row(() -> label, value, null, 0));
        return this;
    }

    public KeyValueRows row(Component label, Supplier<Component> value, IntSupplier color) {
        rows.add(new Row(() -> label, value, color, 0));
        return this;
    }

    public KeyValueRows row(Supplier<Component> label, Supplier<Component> value, @Nullable IntSupplier color) {
        rows.add(new Row(label, value, color, 0));
        return this;
    }

    public KeyValueRows row(Component label, Component value) {
        return row(label, () -> value);
    }

    public KeyValueRows text(Supplier<Component> text) {
        rows.add(new Row(text, null, null, 0));
        return this;
    }

    public KeyValueRows gap(int pixels) {
        rows.add(new Row(null, null, null, pixels));
        return this;
    }

    public KeyValueRows clearRows() {
        rows.clear();
        return this;
    }

    public KeyValueRows rowHeight(int height) {
        this.rowHeight = height;
        return this;
    }

    public KeyValueRows uppercaseLabels(boolean uppercase) {
        this.uppercaseLabels = uppercase;
        return this;
    }

    public KeyValueRows colonSuffix(boolean suffix) {
        this.colonSuffix = suffix;
        return this;
    }

    public KeyValueRows labelToken(ColorToken token) {
        this.labelToken = token;
        return this;
    }

    public KeyValueRows valueToken(ColorToken token) {
        this.valueToken = token;
        return this;
    }

    public int rowCount() {
        return rows.size();
    }

    public int contentHeight() {
        int h = 0;
        for (Row row : rows) {
            h += row.isGap() ? row.gapAfter() : rowHeight;
        }
        return h;
    }

    @Override
    protected Size measure() {
        int w = width();
        if (w <= 0) {
            var font = TextUtil.font();
            for (Row row : rows) {
                if (row.isGap()) continue;
                int lw = row.label() != null ? font.width(row.label().get()) + 12 : 0;
                int vw = row.value() != null ? font.width(row.value().get()) : 0;
                w = Math.max(w, lw + vw);
            }
        }
        return new Size(w, height() > 0 ? height() : contentHeight());
    }

    @Override
    protected void paint(UiGraphics g) {
        int y = 0;
        boolean upper = uppercaseLabels || g.theme().uppercaseHeaders();
        for (Row row : rows) {
            if (row.isGap()) {
                y += row.gapAfter();
                continue;
            }
            if (row.label() != null) {
                Component label = row.label().get();
                if (row.value() == null) {
                    g.text(label, 0, y, row.color() != null ? row.color().getAsInt() : g.color(valueToken));
                } else {
                    String text = label.getString();
                    if (upper) text = text.toUpperCase(Locale.ROOT);
                    if (colonSuffix) text = text + ":";
                    g.text(text, 0, y, g.color(labelToken), g.theme().textShadow());
                }
            }
            if (row.value() != null) {
                Component value = row.value().get();
                int color = row.color() != null ? row.color().getAsInt() : g.color(valueToken);
                g.rightText(value, width(), y, color);
            }
            y += rowHeight;
        }
    }
}

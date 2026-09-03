package com.breakinblocks.saelibvie.ui.render;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.color.Colors;
import com.breakinblocks.saelibvie.ui.color.Skin;
import com.breakinblocks.saelibvie.ui.color.Theme;
import com.breakinblocks.saelibvie.ui.geom.Align;
import com.breakinblocks.saelibvie.ui.geom.Anchor;
import com.breakinblocks.saelibvie.ui.geom.Axis;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class Painter {
    public record ButtonState(boolean enabled, boolean hovered, boolean pressed, boolean selected, boolean focused) {
        public static final ButtonState IDLE = new ButtonState(true, false, false, false, false);
    }

    private Painter() {
    }

    private static boolean useSkin(UiGraphics g, String key) {
        Skin skin = g.theme().skin();
        return skin != null && skin.has(key);
    }

    private static Identifier skinSprite(UiGraphics g, String key) {
        Skin skin = g.theme().skin();
        return skin == null ? null : skin.sprite(key);
    }

    public static void window(UiGraphics g, Rect r) {
        if (r.isEmpty()) return;
        if (useSkin(g, Skin.WINDOW)) {
            g.sprite(skinSprite(g, Skin.WINDOW), r);
            return;
        }
        Theme theme = g.theme();
        if (theme.style() == Theme.Style.BEVEL) {
            g.fill(r, theme.color(ColorToken.WINDOW_BG));
            g.outline(r, theme.color(ColorToken.BORDER_OUT));
            g.bevel(r.inset(1), theme.color(ColorToken.BORDER_IN), theme.color(ColorToken.BORDER_SOFT));
            return;
        }
        g.outline(r, theme.color(ColorToken.BORDER_OUT));
        g.outline(r.inset(1), theme.color(ColorToken.BORDER_IN));
        g.fill(r.inset(2), theme.color(ColorToken.WINDOW_BG));
    }

    public static void panel(UiGraphics g, Rect r) {
        if (r.isEmpty()) return;
        if (useSkin(g, Skin.PANEL)) {
            g.sprite(skinSprite(g, Skin.PANEL), r);
            return;
        }
        Theme theme = g.theme();
        g.fill(r, theme.color(ColorToken.PANEL_BG));
        if (theme.style() == Theme.Style.BEVEL) {
            g.bevel(r, theme.color(ColorToken.BORDER_SOFT), theme.color(ColorToken.BORDER_IN));
        } else {
            g.outline(r, theme.color(ColorToken.BORDER_IN));
        }
    }

    public static void panelHeader(UiGraphics g, Rect panelRect, Component header) {
        Theme theme = g.theme();
        int h = theme.headerHeight();
        Rect band = panelRect.inset(1).topPart(h);
        if (useSkin(g, Skin.HEADER)) {
            g.sprite(skinSprite(g, Skin.HEADER), band);
        } else {
            g.fill(band, theme.color(ColorToken.HEADER_BG));
        }
        String text = g.headerText(header, band.w() - 6);
        g.text(text, band.x() + 3, band.y() + (h - 7) / 2, theme.color(ColorToken.TEXT_TITLE), theme.textShadow());
    }

    public static void inset(UiGraphics g, Rect r) {
        if (r.isEmpty()) return;
        if (useSkin(g, Skin.INSET)) {
            g.sprite(skinSprite(g, Skin.INSET), r);
            return;
        }
        Theme theme = g.theme();
        g.fill(r, theme.color(ColorToken.INSET_BG));
        if (theme.style() == Theme.Style.BEVEL) {
            g.bevel(r, theme.color(ColorToken.BORDER_SOFT), theme.color(ColorToken.BORDER_IN));
        } else {
            g.outline(r, theme.color(ColorToken.BORDER_SOFT));
        }
    }

    public static void slot(UiGraphics g, int x, int y) {
        slot(g, x, y, g.theme().slotSize());
    }

    public static void slot(UiGraphics g, int x, int y, int size) {
        Rect r = new Rect(x, y, size, size);
        if (useSkin(g, Skin.SLOT)) {
            g.sprite(skinSprite(g, Skin.SLOT), r);
            return;
        }
        Theme theme = g.theme();
        g.fill(r, theme.color(ColorToken.SLOT_BG));
        if (theme.style() == Theme.Style.BEVEL) {
            g.bevel(r, theme.color(ColorToken.SLOT_BORDER), theme.color(ColorToken.BORDER_IN));
        } else {
            g.outline(r, theme.color(ColorToken.SLOT_BORDER));
        }
    }

    public static void button(UiGraphics g, Rect r, ButtonState state) {
        if (r.isEmpty()) return;
        Theme theme = g.theme();
        String key = !state.enabled() ? Skin.BUTTON_DISABLED
                : state.selected() && useSkin(g, Skin.BUTTON_SELECTED) ? Skin.BUTTON_SELECTED
                : state.hovered() || state.focused() ? Skin.BUTTON_HOVER
                : Skin.BUTTON;
        if (useSkin(g, key)) {
            g.sprite(skinSprite(g, key), r);
            if (state.selected() && !useSkin(g, Skin.BUTTON_SELECTED)) {
                g.outline(r, theme.color(ColorToken.ACCENT));
            }
            return;
        }
        int background = !state.enabled() ? theme.color(ColorToken.BUTTON_DISABLED)
                : state.pressed() ? Colors.darken(theme.color(ColorToken.BUTTON_HOVER), 0.2f)
                : state.hovered() || state.focused() ? theme.color(ColorToken.BUTTON_HOVER)
                : theme.color(ColorToken.BUTTON_BG);
        g.fill(r, background);
        if (theme.style() == Theme.Style.BEVEL) {
            g.outline(r, theme.color(ColorToken.BORDER_OUT));
            Rect inner = r.inset(1);
            if (state.pressed()) {
                g.bevel(inner, theme.color(ColorToken.BORDER_SOFT), theme.color(ColorToken.BORDER_IN));
            } else {
                g.bevel(inner, theme.color(ColorToken.BORDER_IN), theme.color(ColorToken.BORDER_SOFT));
            }
            if (state.selected()) {
                g.outline(r, theme.color(ColorToken.ACCENT));
            }
        } else {
            g.outline(r, state.selected() ? theme.color(ColorToken.ACCENT) : theme.color(ColorToken.BORDER_SOFT));
        }
    }

    public static int buttonTextColor(UiGraphics g, ButtonState state) {
        Theme theme = g.theme();
        if (!state.enabled()) return theme.color(ColorToken.TEXT_DISABLED);
        if (state.selected()) return theme.color(ColorToken.ACCENT);
        if (theme.style() == Theme.Style.BEVEL) {
            return state.hovered() ? 0xFFFFFFA0 : 0xFFE0E0E0;
        }
        return theme.color(ColorToken.TEXT);
    }

    public static void scrollbar(UiGraphics g, Rect track, float position, float thumbFraction, Axis axis, boolean hovered) {
        if (track.isEmpty()) return;
        Theme theme = g.theme();
        if (useSkin(g, Skin.SCROLL_TRACK)) {
            g.sprite(skinSprite(g, Skin.SCROLL_TRACK), track);
        } else {
            g.fill(track, theme.color(ColorToken.SCROLL_TRACK));
        }
        Rect thumb = thumbRect(track, position, thumbFraction, axis);
        if (useSkin(g, Skin.SCROLL_THUMB)) {
            g.sprite(skinSprite(g, Skin.SCROLL_THUMB), thumb);
        } else {
            int color = theme.color(ColorToken.SCROLL_THUMB);
            g.fill(thumb, hovered ? Colors.lighten(color, 0.2f) : color);
        }
    }

    public static Rect thumbRect(Rect track, float position, float thumbFraction, Axis axis) {
        position = Mth.clamp(position, 0f, 1f);
        thumbFraction = Mth.clamp(thumbFraction, 0f, 1f);
        if (axis == Axis.VERTICAL) {
            int thumbH = Math.max(8, Math.round(track.h() * thumbFraction));
            thumbH = Math.min(thumbH, track.h());
            int thumbY = track.y() + Math.round((track.h() - thumbH) * position);
            return new Rect(track.x(), thumbY, track.w(), thumbH);
        }
        int thumbW = Math.max(8, Math.round(track.w() * thumbFraction));
        thumbW = Math.min(thumbW, track.w());
        int thumbX = track.x() + Math.round((track.w() - thumbW) * position);
        return new Rect(thumbX, track.y(), thumbW, track.h());
    }

    public static void titleBar(UiGraphics g, Rect r, Component title, boolean active) {
        Theme theme = g.theme();
        if (useSkin(g, Skin.TITLE_BAR)) {
            g.sprite(skinSprite(g, Skin.TITLE_BAR), r);
        } else {
            g.fill(r, theme.color(active ? ColorToken.TITLE_BAR_ACTIVE : ColorToken.TITLE_BAR));
            g.outline(r, theme.color(ColorToken.BORDER_SOFT));
        }
        String text = g.headerText(title, r.w() - 8);
        g.text(text, r.x() + 4, r.y() + (r.h() - 7) / 2, theme.color(ColorToken.TEXT_TITLE), theme.textShadow());
    }

    public static void checkbox(UiGraphics g, Rect r, boolean checked, boolean hovered, boolean enabled) {
        Theme theme = g.theme();
        String key = checked ? Skin.CHECKBOX_CHECKED : Skin.CHECKBOX;
        if (useSkin(g, key)) {
            g.sprite(skinSprite(g, key), r);
            return;
        }
        int size = Math.min(r.w(), r.h());
        Rect box = r.align(size, size, Anchor.CENTER_LEFT);
        inset(g, box);
        if (hovered && enabled) {
            g.fill(box.inset(1), theme.color(ColorToken.HOVER));
        }
        if (checked) {
            int color = enabled ? theme.color(ColorToken.ACCENT) : theme.color(ColorToken.TEXT_DISABLED);
            g.fill(box.inset(2), color);
        }
    }

    public static void textField(UiGraphics g, Rect r, boolean focused, boolean enabled) {
        Theme theme = g.theme();
        String key = focused ? Skin.TEXT_FIELD_FOCUSED : Skin.TEXT_FIELD;
        if (useSkin(g, key)) {
            g.sprite(skinSprite(g, key), r);
            return;
        }
        g.fill(r, theme.color(ColorToken.INSET_BG));
        int border = focused ? theme.color(ColorToken.FOCUS) : enabled ? theme.color(ColorToken.BORDER_SOFT) : theme.color(ColorToken.TEXT_DISABLED);
        g.outline(r, border);
    }

    public static void progress(UiGraphics g, Rect r, float fraction, Axis axis, boolean reverse) {
        Theme theme = g.theme();
        fraction = Mth.clamp(fraction, 0f, 1f);
        if (useSkin(g, Skin.PROGRESS_BG)) {
            g.sprite(skinSprite(g, Skin.PROGRESS_BG), r);
        } else {
            inset(g, r);
        }
        Rect inner = r.inset(1);
        if (inner.isEmpty()) return;
        Rect fill;
        Rect edge;
        if (axis == Axis.HORIZONTAL) {
            int w = Math.round(inner.w() * fraction);
            if (w <= 0) return;
            fill = reverse ? inner.rightPart(w) : inner.leftPart(w);
            edge = reverse ? fill.leftPart(1) : fill.rightPart(1);
        } else {
            int h = Math.round(inner.h() * fraction);
            if (h <= 0) return;
            fill = reverse ? inner.topPart(h) : inner.bottomPart(h);
            edge = reverse ? fill.bottomPart(1) : fill.topPart(1);
        }
        if (useSkin(g, Skin.PROGRESS_FILL)) {
            g.sprite(skinSprite(g, Skin.PROGRESS_FILL), fill);
            return;
        }
        g.fill(fill, theme.color(ColorToken.PROGRESS_FILL));
        g.fill(edge, theme.color(ColorToken.PROGRESS_EDGE));
    }

    public static void statBar(UiGraphics g, Rect r, float fraction, int fillColor) {
        Theme theme = g.theme();
        g.outline(r, theme.color(ColorToken.BORDER_SOFT));
        Rect inner = r.inset(1);
        g.fill(inner, theme.color(ColorToken.INSET_BG));
        int w = Math.round(inner.w() * Mth.clamp(fraction, 0f, 1f));
        if (w > 0) {
            g.fill(inner.leftPart(w), fillColor);
        }
    }

    public static void scrim(UiGraphics g, Rect r) {
        g.fill(r, g.theme().color(ColorToken.OVERLAY_DIM));
    }

    public static void focusRing(UiGraphics g, Rect r) {
        g.outline(r, g.theme().color(ColorToken.FOCUS));
    }

    public static void hoverTint(UiGraphics g, Rect r) {
        g.fill(r, g.theme().color(ColorToken.HOVER));
    }

    public static void selectionTint(UiGraphics g, Rect r) {
        g.fill(r, g.theme().color(ColorToken.SELECTED));
    }

    public static void resizeGrip(UiGraphics g, Rect r) {
        int color = g.theme().color(ColorToken.RESIZE_GRIP);
        int size = Math.min(r.w(), r.h());
        for (int i = 1; i < size; i += 2) {
            for (int k = 0; k <= i; k++) {
                int px = r.right() - 1 - i + k;
                int py = r.bottom() - 1 - k;
                g.fill(px, py, px + 1, py + 1, color);
            }
        }
    }

    public static void cornerAccents(UiGraphics g, Rect r, int length, int thickness, int color) {
        g.fill(r.x(), r.y(), r.x() + length, r.y() + thickness, color);
        g.fill(r.x(), r.y(), r.x() + thickness, r.y() + length, color);
        g.fill(r.right() - length, r.y(), r.right(), r.y() + thickness, color);
        g.fill(r.right() - thickness, r.y(), r.right(), r.y() + length, color);
        g.fill(r.x(), r.bottom() - thickness, r.x() + length, r.bottom(), color);
        g.fill(r.x(), r.bottom() - length, r.x() + thickness, r.bottom(), color);
        g.fill(r.right() - length, r.bottom() - thickness, r.right(), r.bottom(), color);
        g.fill(r.right() - thickness, r.bottom() - length, r.right(), r.bottom(), color);
    }

    public static void label(UiGraphics g, Rect r, Component text, int color, Align horizontal, Align vertical) {
        g.textIn(text, r, color, horizontal, vertical);
    }

    public static void separator(UiGraphics g, Rect r, Axis axis) {
        int color = g.theme().color(ColorToken.BORDER_SOFT);
        if (axis == Axis.HORIZONTAL) {
            g.fill(r.x(), r.centerY(), r.right(), r.centerY() + 1, color);
        } else {
            g.fill(r.centerX(), r.y(), r.centerX() + 1, r.bottom(), color);
        }
    }

    public static void vanillaChest(UiGraphics g, Identifier texture, int x, int y, int rows) {
        int topH = rows * 18 + 17;
        g.blit(texture, x, y, 0, 0, 176, topH, 256, 256);
        g.blit(texture, x, y + topH, 0, 126, 176, 96, 256, 256);
    }
}

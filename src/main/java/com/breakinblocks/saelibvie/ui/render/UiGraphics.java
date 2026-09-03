package com.breakinblocks.saelibvie.ui.render;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.color.Colors;
import com.breakinblocks.saelibvie.ui.color.Theme;
import com.breakinblocks.saelibvie.ui.geom.Align;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class UiGraphics {
    private final GuiGraphics gui;
    private final Font font;
    private Theme theme;
    private final int mouseX;
    private final int mouseY;
    private final float partialTick;

    private float offsetX;
    private float offsetY;
    private float scale = 1f;
    private float alpha = 1f;
    private final Deque<float[]> transformStack = new ArrayDeque<>();
    private final Deque<Float> alphaStack = new ArrayDeque<>();
    private final Deque<Theme> themeStack = new ArrayDeque<>();
    private int scissorDepth;

    @Nullable
    private TooltipRequest tooltip;

    public UiGraphics(GuiGraphics gui, Theme theme, int mouseX, int mouseY, float partialTick) {
        this.gui = gui;
        this.font = Minecraft.getInstance().font;
        this.theme = theme;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTick = partialTick;
        this.alpha = theme.opacity();
    }

    public GuiGraphics gui() {
        return gui;
    }

    public Font font() {
        return font;
    }

    public Theme theme() {
        return theme;
    }

    public void pushTheme(Theme newTheme) {
        themeStack.push(theme);
        theme = newTheme;
    }

    public void popTheme() {
        if (!themeStack.isEmpty()) {
            theme = themeStack.pop();
        }
    }

    public int color(ColorToken token) {
        return theme.color(token);
    }

    public float partialTick() {
        return partialTick;
    }

    public int mouseX() {
        return mouseX;
    }

    public int mouseY() {
        return mouseY;
    }

    public double localMouseX() {
        return (mouseX - offsetX) / scale;
    }

    public double localMouseY() {
        return (mouseY - offsetY) / scale;
    }

    public float alpha() {
        return alpha;
    }

    public float currentScale() {
        return scale;
    }

    public int toAbsX(double localX) {
        return Math.round(offsetX + (float) localX * scale);
    }

    public int toAbsY(double localY) {
        return Math.round(offsetY + (float) localY * scale);
    }

    public void pushTranslate(int dx, int dy) {
        pushTransform(dx, dy, 1f);
    }

    public void pushTransform(int dx, int dy, float s) {
        transformStack.push(new float[]{offsetX, offsetY, scale});
        gui.pose().pushPose();
        gui.pose().translate(dx, dy, 0);
        if (s != 1f) {
            gui.pose().scale(s, s, 1f);
        }
        offsetX += dx * scale;
        offsetY += dy * scale;
        scale *= s;
    }

    public void popTransform() {
        gui.pose().popPose();
        float[] saved = transformStack.pop();
        offsetX = saved[0];
        offsetY = saved[1];
        scale = saved[2];
    }

    public void pushZ(float z) {
        gui.pose().pushPose();
        gui.pose().translate(0, 0, z);
    }

    public void popZ() {
        gui.pose().popPose();
    }

    public void pushAlpha(float multiplier) {
        alphaStack.push(alpha);
        alpha = Math.max(0f, Math.min(1f, alpha * multiplier));
    }

    public void popAlpha() {
        if (!alphaStack.isEmpty()) {
            alpha = alphaStack.pop();
        }
    }

    public int apply(int color) {
        return Colors.multiplyAlpha(color, alpha);
    }

    public void pushScissor(Rect local) {
        int x1 = toAbsX(local.x());
        int y1 = toAbsY(local.y());
        int x2 = toAbsX(local.right());
        int y2 = toAbsY(local.bottom());
        gui.enableScissor(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2), Math.max(y1, y2));
        scissorDepth++;
    }

    public void popScissor() {
        if (scissorDepth > 0) {
            gui.disableScissor();
            scissorDepth--;
        }
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        if (Colors.alpha(color) == 0 || alpha <= 0f) return;
        gui.fill(x1, y1, x2, y2, apply(color));
    }

    public void fill(Rect r, int color) {
        fill(r.x(), r.y(), r.right(), r.bottom(), color);
    }

    public void fillGradient(int x1, int y1, int x2, int y2, int from, int to) {
        if (alpha <= 0f) return;
        gui.fillGradient(x1, y1, x2, y2, apply(from), apply(to));
    }

    public void fillGradient(Rect r, int from, int to) {
        fillGradient(r.x(), r.y(), r.right(), r.bottom(), from, to);
    }

    public void hLine(int x1, int x2, int y, int color) {
        fill(Math.min(x1, x2), y, Math.max(x1, x2) + 1, y + 1, color);
    }

    public void vLine(int x, int y1, int y2, int color) {
        fill(x, Math.min(y1, y2), x + 1, Math.max(y1, y2) + 1, color);
    }

    public void outline(int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) return;
        fill(x, y, x + w, y + 1, color);
        fill(x, y + h - 1, x + w, y + h, color);
        fill(x, y + 1, x + 1, y + h - 1, color);
        fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    public void outline(Rect r, int color) {
        outline(r.x(), r.y(), r.w(), r.h(), color);
    }

    public void bevel(Rect r, int light, int dark) {
        fill(r.x(), r.y(), r.right() - 1, r.y() + 1, light);
        fill(r.x(), r.y(), r.x() + 1, r.bottom() - 1, light);
        fill(r.x() + 1, r.bottom() - 1, r.right(), r.bottom(), dark);
        fill(r.right() - 1, r.y() + 1, r.right(), r.bottom(), dark);
    }

    public void box(Rect r, int background, int border) {
        fill(r, background);
        outline(r, border);
    }

    private void beginTexture() {
        if (alpha < 1f) {
            gui.setColor(1f, 1f, 1f, alpha);
        }
    }

    private void endTexture() {
        if (alpha < 1f) {
            gui.setColor(1f, 1f, 1f, 1f);
        }
    }

    public void blit(ResourceLocation texture, int x, int y, int u, int v, int w, int h) {
        blit(texture, x, y, u, v, w, h, 256, 256);
    }

    public void blit(ResourceLocation texture, int x, int y, float u, float v, int w, int h, int texW, int texH) {
        if (alpha <= 0f) return;
        beginTexture();
        gui.blit(texture, x, y, u, v, w, h, texW, texH);
        endTexture();
    }

    public void blitScaled(ResourceLocation texture, int x, int y, int w, int h, float u, float v, int uW, int vH, int texW, int texH) {
        if (alpha <= 0f) return;
        beginTexture();
        gui.blit(texture, x, y, w, h, u, v, uW, vH, texW, texH);
        endTexture();
    }

    public void sprite(ResourceLocation sprite, int x, int y, int w, int h) {
        if (alpha <= 0f) return;
        beginTexture();
        gui.blitSprite(sprite, x, y, w, h);
        endTexture();
    }

    public void sprite(ResourceLocation sprite, Rect r) {
        sprite(sprite, r.x(), r.y(), r.w(), r.h());
    }

    public void spriteRegion(ResourceLocation sprite, int spriteW, int spriteH, int uOff, int vOff, int x, int y, int w, int h) {
        if (alpha <= 0f) return;
        beginTexture();
        gui.blitSprite(sprite, spriteW, spriteH, uOff, vOff, x, y, w, h);
        endTexture();
    }

    public void atlasSprite(TextureAtlasSprite sprite, int x, int y, int w, int h, int tint) {
        if (alpha <= 0f) return;
        int color = apply(tint);
        gui.blit(x, y, 0, w, h, sprite, Colors.redF(color), Colors.greenF(color), Colors.blueF(color), Colors.alphaF(color));
    }

    public void tiledAtlasSprite(TextureAtlasSprite sprite, int x, int y, int w, int h, int tint) {
        int rowsLeft = h;
        int yCursor = y + h;
        while (rowsLeft > 0) {
            int rowH = Math.min(16, rowsLeft);
            yCursor -= rowH;
            int colsLeft = w;
            int xCursor = x;
            while (colsLeft > 0) {
                int colW = Math.min(16, colsLeft);
                atlasSprite(sprite, xCursor, yCursor, colW, rowH, tint);
                xCursor += colW;
                colsLeft -= colW;
            }
            rowsLeft -= rowH;
        }
    }

    public void item(ItemStack stack, int x, int y) {
        if (stack.isEmpty()) return;
        gui.renderItem(stack, x, y);
    }

    public void itemWithDecorations(ItemStack stack, int x, int y) {
        itemWithDecorations(stack, x, y, null);
    }

    public void itemWithDecorations(ItemStack stack, int x, int y, @Nullable String countText) {
        if (stack.isEmpty()) return;
        gui.renderItem(stack, x, y);
        gui.renderItemDecorations(font, stack, x, y, countText);
    }

    public int textWidth(Component text) {
        return font.width(text);
    }

    public int textWidth(String text) {
        return font.width(text);
    }

    public int lineHeight() {
        return font.lineHeight;
    }

    private boolean textVisible() {
        return alpha > 0.02f;
    }

    public void text(Component text, int x, int y, int color) {
        text(text, x, y, color, theme.textShadow());
    }

    public void text(Component text, int x, int y, int color, boolean shadow) {
        if (!textVisible()) return;
        gui.drawString(font, text, x, y, apply(color), shadow);
    }

    public void text(String text, int x, int y, int color, boolean shadow) {
        if (!textVisible()) return;
        gui.drawString(font, text, x, y, apply(color), shadow);
    }

    public void text(FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        if (!textVisible()) return;
        gui.drawString(font, text, x, y, apply(color), shadow);
    }

    public void centeredText(Component text, int centerX, int y, int color) {
        centeredText(text, centerX, y, color, theme.textShadow());
    }

    public void centeredText(Component text, int centerX, int y, int color, boolean shadow) {
        text(text, centerX - font.width(text) / 2, y, color, shadow);
    }

    public void centeredText(String text, int centerX, int y, int color, boolean shadow) {
        text(text, centerX - font.width(text) / 2, y, color, shadow);
    }

    public void rightText(Component text, int rightX, int y, int color) {
        rightText(text, rightX, y, color, theme.textShadow());
    }

    public void rightText(Component text, int rightX, int y, int color, boolean shadow) {
        text(text, rightX - font.width(text), y, color, shadow);
    }

    public void textIn(Component text, Rect r, int color, Align horizontal, Align vertical) {
        textIn(text, r, color, horizontal, vertical, theme.textShadow());
    }

    public void textIn(Component text, Rect r, int color, Align horizontal, Align vertical, boolean shadow) {
        int w = font.width(text);
        int x = r.x() + horizontal.offset(r.w(), w);
        int y = r.y() + vertical.offset(r.h(), font.lineHeight - 1);
        text(text, x, y, color, shadow);
    }

    public List<FormattedCharSequence> wrap(FormattedText text, int width) {
        return font.split(text, Math.max(1, width));
    }

    public int wrappedText(Component text, int x, int y, int width, int color) {
        return wrappedText(text, x, y, width, color, theme.textShadow());
    }

    public int wrappedText(Component text, int x, int y, int width, int color, boolean shadow) {
        List<FormattedCharSequence> lines = wrap(text, width);
        int cursor = y;
        for (FormattedCharSequence line : lines) {
            text(line, x, cursor, color, shadow);
            cursor += font.lineHeight;
        }
        return cursor - y;
    }

    public String fit(Component text, int maxWidth) {
        return fit(text.getString(), maxWidth);
    }

    public String fit(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        String trimmed = font.plainSubstrByWidth(text, Math.max(0, maxWidth - ellipsisWidth));
        return trimmed + ellipsis;
    }

    public String headerText(Component text, int maxWidth) {
        String raw = text.getString();
        if (theme.uppercaseHeaders()) {
            raw = raw.toUpperCase(Locale.ROOT);
        }
        return font.width(raw) <= maxWidth ? raw : font.plainSubstrByWidth(raw, Math.max(0, maxWidth));
    }

    public void tooltip(Component line) {
        tooltip(List.of(line));
    }

    public void tooltip(List<Component> lines) {
        if (lines.isEmpty()) return;
        tooltip = new TooltipRequest(new ArrayList<>(lines), Optional.empty(), null, null, mouseX, mouseY);
    }

    public void tooltip(List<Component> lines, Optional<TooltipComponent> extra) {
        if (lines.isEmpty() && extra.isEmpty()) return;
        tooltip = new TooltipRequest(new ArrayList<>(lines), extra, null, null, mouseX, mouseY);
    }

    public void tooltipAt(List<Component> lines, int absX, int absY) {
        if (lines.isEmpty()) return;
        tooltip = new TooltipRequest(new ArrayList<>(lines), Optional.empty(), null, null, absX, absY);
    }

    public void itemTooltip(ItemStack stack) {
        if (stack.isEmpty()) return;
        tooltip = new TooltipRequest(null, Optional.empty(), stack, null, mouseX, mouseY);
    }

    public void rawTooltip(List<Either<FormattedText, TooltipComponent>> elements) {
        if (elements.isEmpty()) return;
        tooltip = new TooltipRequest(null, Optional.empty(), null, new ArrayList<>(elements), mouseX, mouseY);
    }

    public boolean hasTooltip() {
        return tooltip != null;
    }

    public void clearTooltip() {
        tooltip = null;
    }

    public void flushTooltip() {
        TooltipRequest request = tooltip;
        tooltip = null;
        if (request == null) return;
        gui.pose().pushPose();
        gui.pose().translate(0, 0, 400);
        if (request.stack != null) {
            gui.renderTooltip(font, request.stack, request.x, request.y);
        } else if (request.raw != null) {
            gui.renderComponentTooltipFromElements(font, request.raw, request.x, request.y, ItemStack.EMPTY);
        } else if (request.lines != null) {
            gui.renderTooltip(font, request.lines, request.extra, request.x, request.y);
        }
        gui.pose().popPose();
    }

    public void resetShader() {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private record TooltipRequest(@Nullable List<Component> lines, Optional<TooltipComponent> extra, @Nullable ItemStack stack,
                                  @Nullable List<Either<FormattedText, TooltipComponent>> raw, int x, int y) {
    }
}

package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.color.Color;
import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.AcceptsShiftEnter;
import com.breakinblocks.saelibvie.ui.core.EditSession;
import com.breakinblocks.saelibvie.ui.core.LayerOptions;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import com.breakinblocks.saelibvie.ui.util.UiSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public class ColorPicker extends Panel implements AcceptsShiftEnter {
    public static final int RECENT_CAP = 16;
    private static String lastPalette = "chat";

    private final int initial;
    private boolean allowAlpha;
    @Nullable
    private IntConsumer preview;
    private static int[] sessionRecents = new int[0];
    private Supplier<int[]> recentsSupplier = () -> sessionRecents;
    private Consumer<int[]> recentsConsumer = r -> sessionRecents = r;
    private final List<Palette> palettes = new ArrayList<>();
    private String paletteId = lastPalette;
    private float hue;
    private float saturation;
    private float brightness;
    private int alpha;
    @Nullable
    private EditSession session;
    private final Wheel wheel;
    private final BrightnessBar brightnessBar;
    private final AlphaBar alphaBar;
    private final TextField hexField;
    private final Button paletteButton;
    private final Swatches swatches;

    public ColorPicker(int initialArgb) {
        this.initial = initialArgb;
        chrome(Chrome.WINDOW);
        padding(4);
        palettes.addAll(Palette.defaults());
        setSize(170, 150);
        float[] hsb = Color.RGBtoHSB((initialArgb >> 16) & 255, (initialArgb >> 8) & 255, initialArgb & 255, null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = (initialArgb >>> 24) & 255;
        wheel = add(new Wheel());
        brightnessBar = add(new BrightnessBar());
        alphaBar = add(new AlphaBar());
        hexField = add(new TextField().maxLength(9).hint(Component.literal("RGB")));
        hexField.commitOnFocusLoss(true);
        hexField.onCommit(this::applyHex);
        hexField.validator(this::isHexValid);
        paletteButton = add(new Button(Component.empty(), this::openPaletteMenu));
        paletteButton.label(() -> currentPalette().name());
        swatches = add(new Swatches());
        add(new Button(Component.translatable("gui.accept"), this::accept).id("accept")
                .tooltip(List.of(TextUtil.hotkey("Shift+Enter"))));
        add(new Button(Component.translatable("gui.cancel"), this::cancel).id("cancel")
                .tooltip(List.of(TextUtil.hotkey("Esc"))));
        refreshHex();
        requestLayout();
    }

    public ColorPicker allowAlpha(boolean allow) {
        this.allowAlpha = allow;
        if (!allow) alpha = 255;
        refreshHex();
        requestLayout();
        return this;
    }

    public ColorPicker preview(IntConsumer preview) {
        this.preview = preview;
        return this;
    }

    public ColorPicker recents(Supplier<int[]> supplier, Consumer<int[]> consumer) {
        this.recentsSupplier = supplier;
        this.recentsConsumer = consumer;
        return this;
    }

    public ColorPicker palettes(List<Palette> extra) {
        palettes.clear();
        palettes.addAll(extra);
        return this;
    }

    public ColorPicker addPalette(Palette palette) {
        palettes.add(palette);
        return this;
    }

    public int currentColor() {
        int rgb = Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
        return ((allowAlpha ? alpha : 255) << 24) | rgb;
    }

    private void setColor(int argb) {
        float[] hsb = Color.RGBtoHSB((argb >> 16) & 255, (argb >> 8) & 255, argb & 255, null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        if (allowAlpha) alpha = (argb >>> 24) & 255;
        onChanged();
    }

    private void onChanged() {
        refreshHex();
        if (preview != null) preview.accept(currentColor());
    }

    private void refreshHex() {
        int color = currentColor();
        String text = allowAlpha && alpha < 255
                ? String.format(Locale.ROOT, "#%08X", color)
                : String.format(Locale.ROOT, "#%06X", color & 0xFFFFFF);
        hexField.seed(text);
        if (hexField.isDirty() || hexField.isFocused()) {
            hexField.setValue(text);
        }
    }

    private boolean isHexValid(String text) {
        String s = text.startsWith("#") ? text.substring(1) : text;
        if (s.length() != 6 && s.length() != 8) return false;
        for (char c : s.toCharArray()) {
            if (Character.digit(c, 16) < 0) return false;
        }
        return true;
    }

    private void applyHex(String text) {
        if (!isHexValid(text)) return;
        String s = text.startsWith("#") ? text.substring(1) : text;
        long value = Long.parseLong(s, 16);
        int argb = s.length() == 8 && allowAlpha ? (int) value : 0xFF000000 | (int) (value & 0xFFFFFF);
        setColor(argb);
    }

    private List<Palette> allPalettes() {
        List<Palette> all = new ArrayList<>(palettes);
        all.add(Palette.recent(recentsSupplier.get()));
        return all;
    }

    private Palette currentPalette() {
        for (Palette palette : allPalettes()) {
            if (palette.id().equals(paletteId)) return palette;
        }
        return allPalettes().get(0);
    }

    private void openPaletteMenu() {
        UiRoot root = root();
        if (root == null) return;
        List<MenuItem> items = new ArrayList<>();
        for (Palette palette : allPalettes()) {
            items.add(MenuItem.of(palette.name(), () -> {
                paletteId = palette.id();
                lastPalette = paletteId;
            }));
        }
        root.openContextMenu(items, paletteButton);
    }

    public void open(UiRoot root, EditSession session) {
        this.session = session;
        root.pushLayerAtMouse(this, LayerOptions.modal().scrim(false).session(session));
    }

    public void accept() {
        int chosen = currentColor();
        int[] recents = recentsSupplier.get();
        boolean present = false;
        for (int c : recents) {
            if (c == chosen) {
                present = true;
                break;
            }
        }
        if (!present) {
            int[] updated = new int[Math.min(RECENT_CAP, recents.length + 1)];
            updated[0] = chosen;
            System.arraycopy(recents, 0, updated, 1, updated.length - 1);
            recentsConsumer.accept(updated);
        }
        if (session != null) session.accept();
        remove();
    }

    public void cancel() {
        if (preview != null) preview.accept(initial);
        if (session != null) session.cancel();
        remove();
    }

    private void remove() {
        UiRoot root = root();
        if (root != null) root.removeLayer(this);
    }

    @Override
    public void onShiftEnter() {
        accept();
    }

    @Override
    protected void onLayout() {
        Rect c = contentRect();
        int x = c.x();
        int y = c.y();
        wheel.setBounds(new Rect(x, y, 64, 64));
        brightnessBar.setBounds(new Rect(x + 68, y, 10, 64));
        alphaBar.setVisible(allowAlpha);
        alphaBar.setBounds(new Rect(x + 82, y, 10, 64));
        int right = x + (allowAlpha ? 96 : 82);
        int swatchSize = 14;
        swatches.setBounds(new Rect(right, y, swatchSize * 4 + 3, swatchSize * 4 + 3));
        int width = Math.max(right + swatchSize * 4 + 3, c.x() + 150) + 4 - x;
        hexField.setBounds(new Rect(x, y + 68, 80, 14));
        paletteButton.setBounds(new Rect(x + 84, y + 68, width - 88, 14));
        int buttonsY = y + 86;
        find("accept").setBounds(new Rect(x, buttonsY, 60, 14));
        find("cancel").setBounds(new Rect(x + 64, buttonsY, 60, 14));
        int totalH = buttonsY + 14 + padding().bottom() + chromeInset();
        int totalW = x + width + padding().right() + chromeInset();
        if (totalW != width() || totalH != height()) {
            setSize(totalW, totalH);
        }
    }

    private static void checkerboard(UiGraphics g, Rect r) {
        for (int yy = r.y(); yy < r.bottom(); yy += 4) {
            for (int xx = r.x(); xx < r.right(); xx += 4) {
                boolean dark = ((xx - r.x()) / 4 + (yy - r.y()) / 4) % 2 == 0;
                g.fill(xx, yy, Math.min(xx + 4, r.right()), Math.min(yy + 4, r.bottom()), dark ? 0xFF808080 : 0xFFC0C0C0);
            }
        }
    }

    private final class Wheel extends Widget {
        private boolean dragging;

        @Override
        protected void paint(UiGraphics g) {
            int size = Math.min(width(), height());
            float radius = size / 2f;
            float cx = radius;
            float cy = radius;
            for (int py = 0; py < size; py++) {
                for (int px = 0; px < size; px++) {
                    float dx = px + 0.5f - cx;
                    float dy = py + 0.5f - cy;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist > radius) continue;
                    float h = (float) (Math.atan2(dy, dx) / (Math.PI * 2));
                    if (h < 0) h += 1f;
                    float s = Math.min(1f, dist / radius);
                    g.fill(px, py, px + 1, py + 1, Color.HSBtoRGB(h, s, brightness));
                }
            }
            double angle = hue * Math.PI * 2;
            int mx = Math.round(cx + (float) Math.cos(angle) * saturation * radius);
            int my = Math.round(cy + (float) Math.sin(angle) * saturation * radius);
            g.outline(mx - 2, my - 2, 5, 5, 0xFF000000);
            g.outline(mx - 1, my - 1, 3, 3, 0xFFFFFFFF);
        }

        private boolean pick(double lx, double ly, boolean requireInside) {
            int size = Math.min(width(), height());
            float radius = size / 2f;
            double dx = lx - radius;
            double dy = ly - radius;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (requireInside && dist > radius) return false;
            float h = (float) (Math.atan2(dy, dx) / (Math.PI * 2));
            if (h < 0) h += 1f;
            hue = h;
            saturation = (float) Math.min(1.0, dist / radius);
            onChanged();
            return true;
        }

        @Override
        protected boolean onMouseClicked(double lx, double ly, int button) {
            if (button != 0) return false;
            if (!pick(lx, ly, true)) return false;
            dragging = true;
            return true;
        }

        @Override
        protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
            if (!dragging) return false;
            pick(lx, ly, false);
            return true;
        }

        @Override
        protected boolean onMouseReleased(double lx, double ly, int button) {
            if (!dragging) return false;
            dragging = false;
            return true;
        }
    }

    private final class BrightnessBar extends Widget {
        private boolean dragging;

        @Override
        protected void paint(UiGraphics g) {
            int top = Color.HSBtoRGB(hue, saturation, 1f);
            g.fillGradient(0, 0, width(), height(), top, 0xFF000000);
            g.outline(localRect(), g.color(ColorToken.BORDER_SOFT));
            int my = Math.round((1f - brightness) * (height() - 1));
            g.fill(0, my, width(), my + 1, 0xFFFFFFFF);
        }

        private void pick(double ly) {
            brightness = 1f - Mth.clamp((float) ly / Math.max(1, height() - 1), 0f, 1f);
            onChanged();
        }

        @Override
        protected boolean onMouseClicked(double lx, double ly, int button) {
            if (button != 0) return false;
            dragging = true;
            pick(ly);
            return true;
        }

        @Override
        protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
            if (!dragging) return false;
            pick(ly);
            return true;
        }

        @Override
        protected boolean onMouseReleased(double lx, double ly, int button) {
            if (!dragging) return false;
            dragging = false;
            return true;
        }
    }

    private final class AlphaBar extends Widget {
        private boolean dragging;

        @Override
        protected void paint(UiGraphics g) {
            checkerboard(g, localRect());
            int rgb = currentColor() & 0xFFFFFF;
            g.fillGradient(0, 0, width(), height(), 0xFF000000 | rgb, rgb);
            g.outline(localRect(), g.color(ColorToken.BORDER_SOFT));
            int my = Math.round((1f - alpha / 255f) * (height() - 1));
            g.fill(0, my, width(), my + 1, 0xFFFFFFFF);
        }

        private void pick(double ly) {
            alpha = Math.round((1f - Mth.clamp((float) ly / Math.max(1, height() - 1), 0f, 1f)) * 255f);
            onChanged();
        }

        @Override
        protected boolean onMouseClicked(double lx, double ly, int button) {
            if (button != 0 || !allowAlpha) return false;
            dragging = true;
            pick(ly);
            return true;
        }

        @Override
        protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
            if (!dragging) return false;
            pick(ly);
            return true;
        }

        @Override
        protected boolean onMouseReleased(double lx, double ly, int button) {
            if (!dragging) return false;
            dragging = false;
            return true;
        }
    }

    private final class Swatches extends Widget {
        private static final int SIZE = 14;

        private Rect cell(int index) {
            int col = index % 4;
            int row = index / 4;
            return new Rect(col * (SIZE + 1), row * (SIZE + 1), SIZE, SIZE);
        }

        @Override
        protected void paint(UiGraphics g) {
            int[] colors = currentPalette().colors();
            for (int i = 0; i < 16; i++) {
                Rect r = cell(i);
                if (i >= colors.length) continue;
                int color = colors[i];
                if (((color >>> 24) & 255) < 255) {
                    checkerboard(g, r);
                }
                g.fill(r, color);
                g.outline(r, g.color(ColorToken.BORDER_SOFT));
                if (isHovered() && r.contains(g.localMouseX(), g.localMouseY())) {
                    g.outline(r, g.color(ColorToken.ACCENT));
                    g.tooltip(Component.literal(String.format(Locale.ROOT, "#%08X", color)));
                }
            }
        }

        @Override
        protected boolean onMouseClicked(double lx, double ly, int button) {
            int[] colors = currentPalette().colors();
            for (int i = 0; i < Math.min(16, colors.length); i++) {
                if (cell(i).contains(lx, ly)) {
                    UiSounds.click();
                    setColor(colors[i]);
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    protected void paintBackground(UiGraphics g) {
        super.paintBackground(g);
        Rect c = contentRect();
        Rect previewRect = new Rect(c.x() + 84, c.y() + 50, 40, 14);
        if (allowAlpha) {
            previewRect = new Rect(c.x() + 96, c.y() + 50, 40, 14);
        }
        if (((currentColor() >>> 24) & 255) < 255) {
            checkerboard(g, previewRect);
        }
        Painter.inset(g, previewRect.grow(1));
        g.fill(previewRect, currentColor());
    }
}

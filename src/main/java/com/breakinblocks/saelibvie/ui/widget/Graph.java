package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;

import java.util.function.Supplier;

public class Graph extends Widget {
    private final Supplier<Float> sample;
    private float[] samples = new float[1];
    private int head;
    private int count;
    private int sampleInterval = 1;
    private int tickCounter;
    private float fixedMax = -1f;
    private boolean drawBackground = true;
    private boolean lineMode;

    public Graph(Supplier<Float> sample) {
        this.sample = sample;
    }

    public Graph(Rect bounds, Supplier<Float> sample) {
        super(bounds);
        this.sample = sample;
        resizeBuffer();
    }

    public Graph sampleInterval(int ticks) {
        this.sampleInterval = Math.max(1, ticks);
        return this;
    }

    public Graph fixedMax(float max) {
        this.fixedMax = max;
        return this;
    }

    public Graph background(boolean draw) {
        this.drawBackground = draw;
        return this;
    }

    public Graph lineMode(boolean line) {
        this.lineMode = line;
        return this;
    }

    private void resizeBuffer() {
        int size = Math.max(1, width() - 2);
        if (samples.length != size) {
            samples = new float[size];
            head = 0;
            count = 0;
        }
    }

    @Override
    protected void onResized() {
        resizeBuffer();
    }

    public void push(float value) {
        samples[head] = Math.max(0f, value);
        head = (head + 1) % samples.length;
        count = Math.min(count + 1, samples.length);
    }

    public void clearSamples() {
        head = 0;
        count = 0;
    }

    @Override
    protected void onTick() {
        if (++tickCounter >= sampleInterval) {
            tickCounter = 0;
            Float value = sample.get();
            push(value == null ? 0f : value);
        }
    }

    private float sampleAt(int i) {
        return samples[(head - 1 - i + samples.length * 2) % samples.length];
    }

    @Override
    protected void paint(UiGraphics g) {
        Rect r = localRect();
        if (drawBackground) {
            Painter.inset(g, r);
        }
        if (count == 0) return;
        float max = fixedMax;
        if (max <= 0f) {
            for (int i = 0; i < count; i++) {
                max = Math.max(max, sampleAt(i));
            }
        }
        if (max <= 0f) return;
        Rect inner = drawBackground ? r.inset(1) : r;
        int bottom = inner.bottom();
        int rightX = inner.right() - 1;
        int fill = g.color(ColorToken.GRAPH_FILL);
        int line = g.color(ColorToken.GRAPH_LINE);
        int prevH = -1;
        for (int i = 0; i < count && i < inner.w(); i++) {
            float value = sampleAt(i);
            int x = rightX - i;
            int colH = value <= 0f ? 0 : Math.max(1, Math.round(value / max * inner.h()));
            if (lineMode) {
                if (colH > 0) {
                    int y = bottom - colH;
                    if (prevH >= 0) {
                        int y0 = Math.min(y, bottom - prevH);
                        int y1 = Math.max(y, bottom - prevH);
                        g.fill(x, y0, x + 1, y1 + 1, line);
                    } else {
                        g.fill(x, y, x + 1, y + 1, line);
                    }
                }
                prevH = colH;
            } else if (colH > 0) {
                g.fill(x, bottom - colH, x + 1, bottom, fill);
                g.fill(x, bottom - colH, x + 1, bottom - colH + 1, line);
            }
        }
    }
}

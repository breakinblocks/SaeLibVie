package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.Modifiers;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import com.breakinblocks.saelibvie.ui.util.UiSounds;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

public class NumberStepper extends Widget {
    private int min;
    private int max;
    private int step = 1;
    private int shiftStep = 10;
    private int ctrlStep = 64;
    private IntSupplier value;
    private int localValue;
    @Nullable
    private IntConsumer onChange;
    private IntFunction<Component> formatter = v -> Component.literal(TextUtil.grouped(v));
    private int buttonWidth = 14;
    private boolean minusPressed;
    private boolean plusPressed;

    public NumberStepper(int min, int max) {
        this.min = min;
        this.max = max;
        this.localValue = min;
        this.value = () -> localValue;
        focusable(true);
    }

    public NumberStepper(int min, int max, int initial, IntConsumer onChange) {
        this(min, max);
        this.localValue = Mth.clamp(initial, min, max);
        this.onChange = onChange;
    }

    public NumberStepper bind(IntSupplier value, IntConsumer onChange) {
        this.value = value;
        this.onChange = onChange;
        return this;
    }

    public NumberStepper steps(int base, int shift, int ctrl) {
        this.step = base;
        this.shiftStep = shift;
        this.ctrlStep = ctrl;
        return this;
    }

    public NumberStepper range(int min, int max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public NumberStepper formatter(IntFunction<Component> formatter) {
        this.formatter = formatter;
        return this;
    }

    public NumberStepper buttonWidth(int width) {
        this.buttonWidth = width;
        return this;
    }

    public int value() {
        return Mth.clamp(value.getAsInt(), min, max);
    }

    public void setValue(int newValue) {
        int clamped = Mth.clamp(newValue, min, max);
        if (clamped == value()) return;
        localValue = clamped;
        if (onChange != null) onChange.accept(clamped);
    }

    @FunctionalInterface
    public interface StepFunction {
        int next(int current, int direction, boolean shift, boolean ctrl);
    }

    @Nullable
    private StepFunction stepFunction;

    public NumberStepper stepFunction(@Nullable StepFunction function) {
        this.stepFunction = function;
        return this;
    }

    public static StepFunction doublingSteps() {
        return (current, direction, shift, ctrl) -> {
            if (shift) {
                return direction > 0 ? current + Math.max(1, current) : current - Math.max(1, current / 2);
            }
            return current + direction;
        };
    }

    public void adjust(int direction) {
        if (stepFunction != null) {
            setValue(stepFunction.next(value(), direction, Modifiers.shift(), Modifiers.ctrl()));
            return;
        }
        setValue(value() + direction * Modifiers.step(step, shiftStep, ctrlStep));
    }

    @Override
    protected Size measure() {
        return new Size(width() > 0 ? width() : buttonWidth * 2 + 40, height() > 0 ? height() : 14);
    }

    private Rect minusRect() {
        return new Rect(0, 0, buttonWidth, height());
    }

    private Rect plusRect() {
        return new Rect(width() - buttonWidth, 0, buttonWidth, height());
    }

    @Override
    protected void paint(UiGraphics g) {
        Rect r = localRect();
        Rect minus = minusRect();
        Rect plus = plusRect();
        double mx = g.localMouseX();
        double my = g.localMouseY();
        boolean canMinus = isEnabled() && value() > min;
        boolean canPlus = isEnabled() && value() < max;
        Painter.button(g, minus, new Painter.ButtonState(canMinus, isHovered() && minus.contains(mx, my), minusPressed, false, false));
        Painter.button(g, plus, new Painter.ButtonState(canPlus, isHovered() && plus.contains(mx, my), plusPressed, false, false));
        g.centeredText("-", minus.centerX(), minus.y() + (minus.h() - 8) / 2, Painter.buttonTextColor(g, new Painter.ButtonState(canMinus, false, false, false, false)), false);
        g.centeredText("+", plus.centerX(), plus.y() + (plus.h() - 8) / 2, Painter.buttonTextColor(g, new Painter.ButtonState(canPlus, false, false, false, false)), false);
        Rect middle = new Rect(minus.right(), 0, plus.x() - minus.right(), r.h());
        Painter.inset(g, middle);
        int color = isEnabled() ? g.color(ColorToken.TEXT) : g.color(ColorToken.TEXT_DISABLED);
        g.centeredText(formatter.apply(value()), middle.centerX(), middle.y() + (middle.h() - 8) / 2, color);
        if (isFocused()) {
            Painter.focusRing(g, r);
        }
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (button != 0) return false;
        requestFocus();
        if (minusRect().contains(lx, ly)) {
            minusPressed = true;
            UiSounds.click();
            adjust(-1);
            return true;
        }
        if (plusRect().contains(lx, ly)) {
            plusPressed = true;
            UiSounds.click();
            adjust(1);
            return true;
        }
        return true;
    }

    @Override
    protected boolean onMouseReleased(double lx, double ly, int button) {
        minusPressed = false;
        plusPressed = false;
        return false;
    }

    @Override
    protected boolean onMouseScrolled(double lx, double ly, double scrollX, double scrollY) {
        adjust(scrollY > 0 ? 1 : -1);
        return true;
    }

    @Override
    protected boolean onKeyPressed(int key, int scanCode, int modifiers) {
        if (key == InputConstants.KEY_UP || key == InputConstants.KEY_RIGHT) {
            adjust(1);
            return true;
        }
        if (key == InputConstants.KEY_DOWN || key == InputConstants.KEY_LEFT) {
            adjust(-1);
            return true;
        }
        return false;
    }
}

package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.Theme;
import com.breakinblocks.saelibvie.ui.core.CursorType;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Align;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import com.breakinblocks.saelibvie.ui.util.UiSounds;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Button extends Widget {
    @Nullable
    private Supplier<Component> label;
    @Nullable
    private Supplier<ResourceLocation> icon;
    private int iconSize = 16;
    @Nullable
    private Consumer<Button> onPress;
    @Nullable
    private Consumer<Button> onRightPress;
    @Nullable
    private BooleanSupplier selected;
    private boolean pressed;
    private boolean playSound = true;
    private boolean drawBackground = true;
    private int padding = 4;
    private Align textAlign = Align.CENTER;
    private boolean fitText = true;

    public Button() {
        focusable(true);
    }

    public Button(Component label) {
        this();
        this.label = () -> label;
    }

    public Button(Component label, Runnable onPress) {
        this(label);
        this.onPress = b -> onPress.run();
    }

    public Button(Component label, Consumer<Button> onPress) {
        this(label);
        this.onPress = onPress;
    }

    public static Button of(String literal, Runnable onPress) {
        return new Button(Component.literal(literal), onPress);
    }

    public Button label(Component label) {
        this.label = () -> label;
        return this;
    }

    public Button label(Supplier<Component> label) {
        this.label = label;
        return this;
    }

    @Nullable
    public Component currentLabel() {
        return label == null ? null : label.get();
    }

    public Button icon(ResourceLocation sprite) {
        this.icon = () -> sprite;
        return this;
    }

    public Button icon(Supplier<ResourceLocation> sprite) {
        this.icon = sprite;
        return this;
    }

    public Button iconSize(int size) {
        this.iconSize = size;
        return this;
    }

    @Nullable
    private Supplier<ItemStack> itemIcon;
    @Nullable
    private Supplier<FluidStack> fluidIcon;

    public Button itemIcon(Supplier<ItemStack> stack) {
        this.itemIcon = stack;
        this.fluidIcon = null;
        this.icon = null;
        return this;
    }

    public Button itemIcon(ItemStack stack) {
        return itemIcon(() -> stack);
    }

    public Button fluidIcon(Supplier<FluidStack> stack) {
        this.fluidIcon = stack;
        this.itemIcon = null;
        this.icon = null;
        return this;
    }

    private boolean hasAnyIcon() {
        return icon != null || itemIcon != null || fluidIcon != null;
    }

    private void drawIcon(UiGraphics g, int x, int y) {
        if (icon != null) {
            ResourceLocation sprite = icon.get();
            if (sprite != null) g.sprite(sprite, x, y, iconSize, iconSize);
        } else if (itemIcon != null) {
            ItemStack stack = itemIcon.get();
            if (stack == null || stack.isEmpty()) return;
            if (iconSize == 16) {
                g.item(stack, x, y);
            } else {
                g.pushTransform(x, y, iconSize / 16f);
                g.item(stack, 0, 0);
                g.popTransform();
            }
        } else if (fluidIcon != null) {
            FluidStack stack = fluidIcon.get();
            if (stack == null || stack.isEmpty()) return;
            PagedGrid.drawFluidCell(g, stack, new Rect(x - 1, y - 1, iconSize + 2, iconSize + 2));
        }
    }

    @Override
    @Nullable
    public CursorType cursor() {
        return isEnabled() ? CursorType.HAND : null;
    }

    public Button onPress(Runnable action) {
        this.onPress = b -> action.run();
        return this;
    }

    public Button onPress(Consumer<Button> action) {
        this.onPress = action;
        return this;
    }

    public Button onRightPress(Runnable action) {
        this.onRightPress = b -> action.run();
        return this;
    }

    public Button selectedWhen(BooleanSupplier condition) {
        this.selected = condition;
        return this;
    }

    public boolean isSelected() {
        return selected != null && selected.getAsBoolean();
    }

    public Button sound(boolean play) {
        this.playSound = play;
        return this;
    }

    public Button flat(boolean flat) {
        this.drawBackground = !flat;
        return this;
    }

    public Button padding(int padding) {
        this.padding = padding;
        return this;
    }

    public Button textAlign(Align align) {
        this.textAlign = align;
        return this;
    }

    public Button fitText(boolean fit) {
        this.fitText = fit;
        return this;
    }

    public boolean isPressed() {
        return pressed;
    }

    @Override
    protected Size measure() {
        int w = width();
        int h = height();
        if (w <= 0) {
            int textW = label != null ? TextUtil.font().width(label.get()) : 0;
            int iconW = hasAnyIcon() ? iconSize + (textW > 0 ? 2 : 0) : 0;
            w = textW + iconW + padding * 2;
            if (hasAnyIcon() && textW == 0) {
                w = iconSize + 4;
            }
        }
        if (h <= 0) {
            h = Math.max(hasAnyIcon() ? iconSize + 4 : 0, 14);
        }
        return new Size(w, h);
    }

    public Painter.ButtonState state() {
        return new Painter.ButtonState(isEnabled(), isHovered(), pressed, isSelected(), isFocused());
    }

    @Override
    protected void paint(UiGraphics g) {
        Rect r = localRect();
        Painter.ButtonState state = state();
        if (drawBackground) {
            Painter.button(g, r, state);
        } else if (state.hovered() && state.enabled()) {
            Painter.hoverTint(g, r);
        }
        paintContent(g, r, state);
    }

    protected void paintContent(UiGraphics g, Rect r, Painter.ButtonState state) {
        int color = Painter.buttonTextColor(g, state);
        Component text = label != null ? label.get() : null;
        boolean hasIcon = hasAnyIcon();
        int textW = text != null ? g.textWidth(text) : 0;
        int contentW = textW + (hasIcon ? iconSize + (textW > 0 ? 2 : 0) : 0);
        int available = r.w() - padding * 2;
        String fitted = null;
        if (text != null && fitText && contentW > available) {
            int textAvail = Math.max(0, available - (hasIcon ? iconSize + 2 : 0));
            fitted = g.fit(text, textAvail);
            textW = g.textWidth(fitted);
            contentW = textW + (hasIcon ? iconSize + 2 : 0);
        }
        int x = r.x() + padding + textAlign.offset(available, contentW);
        if (hasIcon) {
            int iy = r.y() + (r.h() - iconSize) / 2;
            if (textW == 0) {
                x = r.x() + (r.w() - iconSize) / 2;
            }
            drawIcon(g, x, iy);
            x += iconSize + (textW > 0 ? 2 : 0);
        }
        if (text != null) {
            int ty = r.y() + (r.h() - 8) / 2;
            boolean shadow = g.theme().style() == Theme.Style.BEVEL || g.theme().textShadow();
            if (fitted != null) {
                g.text(fitted, x, ty, color, shadow);
            } else {
                g.text(text, x, ty, color, shadow);
            }
        }
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (button == 0) {
            pressed = true;
            requestFocus();
            return true;
        }
        if (button == 1 && onRightPress != null) {
            press(onRightPress);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onMouseReleased(double lx, double ly, int button) {
        if (button == 0 && pressed) {
            pressed = false;
            if (contains(lx, ly) && isEnabled()) {
                press(onPress);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean onKeyPressed(int key, int scanCode, int modifiers) {
        if (key == InputConstants.KEY_RETURN || key == InputConstants.KEY_SPACE || key == InputConstants.KEY_NUMPADENTER) {
            press(onPress);
            return true;
        }
        return false;
    }

    protected void press(@Nullable Consumer<Button> action) {
        if (playSound) {
            UiSounds.click();
        }
        if (action != null) {
            action.accept(this);
        }
    }

    public void click() {
        press(onPress);
    }
}

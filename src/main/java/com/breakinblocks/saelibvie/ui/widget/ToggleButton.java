package com.breakinblocks.saelibvie.ui.widget;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ToggleButton extends Button {
    private BooleanSupplier state;
    @Nullable
    private Consumer<Boolean> onToggle;
    @Nullable
    private Component onLabel;
    @Nullable
    private Component offLabel;
    private boolean localState;

    public ToggleButton(Component label) {
        super(label);
        this.state = () -> localState;
        onPress(b -> toggle());
    }

    public ToggleButton(Component onLabel, Component offLabel) {
        this(offLabel);
        this.onLabel = onLabel;
        this.offLabel = offLabel;
        label(() -> isOn() ? onLabel : offLabel);
    }

    public ToggleButton bind(BooleanSupplier state, Consumer<Boolean> onToggle) {
        this.state = state;
        this.onToggle = onToggle;
        selectedWhen(state);
        return this;
    }

    public ToggleButton onToggle(Consumer<Boolean> onToggle) {
        this.onToggle = onToggle;
        selectedWhen(state);
        return this;
    }

    public ToggleButton initial(boolean on) {
        this.localState = on;
        selectedWhen(state);
        return this;
    }

    public boolean isOn() {
        return state.getAsBoolean();
    }

    public void setOn(boolean on) {
        localState = on;
    }

    public void toggle() {
        boolean next = !isOn();
        localState = next;
        if (onToggle != null) {
            onToggle.accept(next);
        }
    }
}

package com.breakinblocks.saelibvie.ui.widget;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class MultiStateButton extends Button {
    private final List<Identifier> icons;
    private final List<Component> tooltips;
    private IntSupplier state;
    private int localState;
    @Nullable
    private Consumer<Integer> onStateChange;

    public MultiStateButton(List<Identifier> icons, List<Component> tooltips) {
        this.icons = List.copyOf(icons);
        this.tooltips = List.copyOf(tooltips);
        this.state = () -> localState;
        icon(() -> this.icons.get(Math.floorMod(state.getAsInt(), this.icons.size())));
        if (!this.tooltips.isEmpty()) {
            tooltip(() -> List.of(this.tooltips.get(Math.floorMod(state.getAsInt(), this.tooltips.size()))));
        }
        onPress(b -> {
            int next = Math.floorMod(state.getAsInt() + 1, this.icons.size());
            localState = next;
            if (onStateChange != null) onStateChange.accept(next);
        });
    }

    public MultiStateButton bind(IntSupplier state, Consumer<Integer> onStateChange) {
        this.state = state;
        this.onStateChange = onStateChange;
        return this;
    }

    public MultiStateButton onStateChange(Consumer<Integer> onStateChange) {
        this.onStateChange = onStateChange;
        return this;
    }

    public int currentState() {
        return state.getAsInt();
    }

    public void setState(int value) {
        localState = value;
    }

    public Component hoverText() {
        return tooltips.isEmpty() ? Component.empty() : tooltips.get(Math.floorMod(state.getAsInt(), tooltips.size()));
    }
}

package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.util.Modifiers;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CycleButton<T> extends Button {
    private final List<T> values;
    private final Function<T, Component> labeler;
    private Supplier<T> current;
    @Nullable
    private Consumer<T> onChange;
    private int localIndex;

    public CycleButton(List<T> values, Function<T, Component> labeler) {
        this.values = List.copyOf(values);
        this.labeler = labeler;
        this.current = () -> this.values.get(localIndex);
        label(() -> labeler.apply(current.get()));
        onPress(b -> advance(Modifiers.shift() ? -1 : 1));
        onRightPress(() -> advance(-1));
    }

    public static <E extends Enum<E>> CycleButton<E> ofEnum(Class<E> type, Function<E, Component> labeler) {
        return new CycleButton<>(List.of(type.getEnumConstants()), labeler);
    }

    public CycleButton<T> bind(Supplier<T> current, Consumer<T> onChange) {
        this.current = current;
        this.onChange = onChange;
        return this;
    }

    public CycleButton<T> onChange(Consumer<T> onChange) {
        this.onChange = onChange;
        return this;
    }

    public CycleButton<T> initial(T value) {
        int index = values.indexOf(value);
        if (index >= 0) localIndex = index;
        return this;
    }

    public T value() {
        return current.get();
    }

    public void advance(int delta) {
        int index = values.indexOf(current.get());
        if (index < 0) index = 0;
        int next = Math.floorMod(index + delta, values.size());
        localIndex = next;
        if (onChange != null) {
            onChange.accept(values.get(next));
        }
    }

    public Component labelFor(T value) {
        return labeler.apply(value);
    }
}

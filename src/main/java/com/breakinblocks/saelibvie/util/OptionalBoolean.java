package com.breakinblocks.saelibvie.util;

import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class OptionalBoolean {
    public static final OptionalBoolean EMPTY = new OptionalBoolean(false, false);
    public static final OptionalBoolean TRUE = new OptionalBoolean(true, true);
    public static final OptionalBoolean FALSE = new OptionalBoolean(true, false);

    private final boolean present;
    private final boolean value;

    private OptionalBoolean(boolean present, boolean value) {
        this.present = present;
        this.value = value;
    }

    public static OptionalBoolean ofNullable(@Nullable Boolean value) {
        return value == null ? EMPTY : of(value);
    }

    public static OptionalBoolean of(boolean value) {
        return value ? TRUE : FALSE;
    }

    public boolean isPresent() {
        return present;
    }

    public boolean get() {
        if (!present) throw new NoSuchElementException("No value present");
        return value;
    }

    public boolean orElse(boolean other) {
        return present ? value : other;
    }

    public boolean orElseGet(BooleanSupplier other) {
        return present ? value : other.getAsBoolean();
    }

    public <X extends Throwable> boolean orElseThrow(Supplier<? extends X> exception) throws X {
        if (!present) throw exception.get();
        return value;
    }

    public void ifPresent(BooleanConsumer consumer) {
        if (present) consumer.accept(value);
    }

    @Override
    public String toString() {
        return present ? (value ? "OptionalBoolean[true]" : "OptionalBoolean[false]") : "OptionalBoolean.empty";
    }
}

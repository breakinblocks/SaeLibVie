package com.breakinblocks.saelibvie.util;

import java.util.function.Supplier;

public final class Lazy<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private volatile T value;
    private volatile boolean initialized;

    private Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    @Override
    public T get() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    value = supplier.get();
                    initialized = true;
                }
            }
        }
        return value;
    }

    public void invalidate() {
        synchronized (this) {
            initialized = false;
            value = null;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public String toString() {
        return "Lazy(" + (initialized ? String.valueOf(value) : "uninitialized") + ")";
    }
}

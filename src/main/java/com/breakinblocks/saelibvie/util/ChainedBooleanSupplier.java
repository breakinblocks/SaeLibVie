package com.breakinblocks.saelibvie.util;

import java.util.function.BooleanSupplier;

@FunctionalInterface
public interface ChainedBooleanSupplier extends BooleanSupplier {
    ChainedBooleanSupplier TRUE = () -> true;
    ChainedBooleanSupplier FALSE = () -> false;

    default ChainedBooleanSupplier not() {
        return () -> !getAsBoolean();
    }

    default ChainedBooleanSupplier or(BooleanSupplier other) {
        return () -> getAsBoolean() || other.getAsBoolean();
    }

    default ChainedBooleanSupplier and(BooleanSupplier other) {
        return () -> getAsBoolean() && other.getAsBoolean();
    }

    default ChainedBooleanSupplier xor(BooleanSupplier other) {
        return () -> getAsBoolean() ^ other.getAsBoolean();
    }

    static ChainedBooleanSupplier of(BooleanSupplier supplier) {
        return supplier instanceof ChainedBooleanSupplier chained ? chained : supplier::getAsBoolean;
    }
}

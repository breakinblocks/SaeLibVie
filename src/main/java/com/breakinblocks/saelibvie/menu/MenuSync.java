package com.breakinblocks.saelibvie.menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class MenuSync {
    public interface IntValue {
        int get();
    }

    public interface FloatValue {
        float get();
    }

    public interface BoolValue {
        boolean get();
    }

    public interface EnumValue<E extends Enum<E>> {
        E get();
    }

    public interface FluidValue {
        FluidStack get();
    }

    private final boolean server;
    private final List<IntSupplier> suppliers = new ArrayList<>();
    private final List<Integer> values = new ArrayList<>();
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return raw(index);
        }

        @Override
        public void set(int index, int value) {
            if (!server && index >= 0 && index < values.size()) {
                values.set(index, value);
            }
        }

        @Override
        public int getCount() {
            return suppliers.size();
        }
    };

    private MenuSync(boolean server) {
        this.server = server;
    }

    public static MenuSync server() {
        return new MenuSync(true);
    }

    public static MenuSync client() {
        return new MenuSync(false);
    }

    public static MenuSync forSide(boolean isClientSide) {
        return isClientSide ? client() : server();
    }

    public ContainerData data() {
        return data;
    }

    public int size() {
        return suppliers.size();
    }

    private int raw(int index) {
        if (index < 0 || index >= suppliers.size()) return 0;
        return server ? suppliers.get(index).getAsInt() : values.get(index);
    }

    private int slot(IntSupplier supplier) {
        suppliers.add(supplier);
        values.add(0);
        return suppliers.size() - 1;
    }

    public IntValue addShort(IntSupplier supplier) {
        int index = slot(supplier);
        return () -> (short) raw(index);
    }

    public IntValue addInt(IntSupplier supplier) {
        int low = slot(() -> supplier.getAsInt() & 0xFFFF);
        int high = slot(() -> (supplier.getAsInt() >>> 16) & 0xFFFF);
        return () -> (raw(low) & 0xFFFF) | ((raw(high) & 0xFFFF) << 16);
    }

    public FloatValue addFloat(Supplier<Float> supplier, int scale) {
        IntValue scaled = addInt(() -> Math.round(supplier.get() * scale));
        return () -> scaled.get() / (float) scale;
    }

    public FloatValue addFraction(Supplier<Float> supplier) {
        return addFloat(supplier, 10_000);
    }

    public BoolValue addBool(BooleanSupplier supplier) {
        int index = slot(() -> supplier.getAsBoolean() ? 1 : 0);
        return () -> raw(index) != 0;
    }

    public <E extends Enum<E>> EnumValue<E> addEnum(Class<E> type, Supplier<E> supplier) {
        E[] constants = type.getEnumConstants();
        int index = slot(() -> {
            E value = supplier.get();
            return value == null ? 0 : value.ordinal() + 1;
        });
        return () -> {
            int ordinal = raw(index) - 1;
            if (ordinal < 0 || ordinal >= constants.length) return null;
            return constants[ordinal];
        };
    }

    public IntValue addBitmask(IntSupplier supplier) {
        return addInt(supplier);
    }

    public FluidValue addFluid(Supplier<FluidStack> supplier) {
        IntValue fluidId = addInt(() -> BuiltInRegistries.FLUID.getId(supplier.get().getFluid()));
        IntValue amount = addInt(() -> supplier.get().getAmount());
        return () -> {
            Fluid fluid = BuiltInRegistries.FLUID.byId(fluidId.get());
            int amt = amount.get();
            if (fluid == Fluids.EMPTY || amt <= 0) return FluidStack.EMPTY;
            return new FluidStack(fluid, amt);
        };
    }

    public static boolean bit(int mask, int bit) {
        return (mask & (1 << bit)) != 0;
    }
}

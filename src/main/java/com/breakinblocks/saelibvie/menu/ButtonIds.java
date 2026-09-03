package com.breakinblocks.saelibvie.menu;

public final class ButtonIds {
    public record Range(int base, int count) {
        public int id(int index) {
            if (index < 0 || index >= count) throw new IndexOutOfBoundsException("index " + index + " outside range of " + count);
            return base + index;
        }

        public boolean contains(int id) {
            return id >= base && id < base + count;
        }

        public int index(int id) {
            return id - base;
        }

        public <E extends Enum<E>> int id(E value) {
            return id(value.ordinal());
        }

        public <E extends Enum<E>> E decode(int id, Class<E> type) {
            E[] constants = type.getEnumConstants();
            int index = index(id);
            return index >= 0 && index < constants.length ? constants[index] : null;
        }
    }

    private int next;

    public ButtonIds() {
        this(0);
    }

    public ButtonIds(int start) {
        this.next = start;
    }

    public int next() {
        return next++;
    }

    public Range range(int count) {
        Range range = new Range(next, count);
        next += count;
        return range;
    }

    public <E extends Enum<E>> Range range(Class<E> type) {
        return range(type.getEnumConstants().length);
    }

    public int size() {
        return next;
    }
}

package com.breakinblocks.saelibvie.math;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class Bits {
    private Bits() {
    }

    public static boolean getFlag(int flags, int flag) {
        return (flags & flag) != 0;
    }

    public static int setFlag(int flags, int flag, boolean value) {
        return value ? flags | flag : flags & ~flag;
    }

    public static int toInt(boolean[] bits) {
        int result = 0;
        for (int i = 0; i < bits.length; i++) {
            if (bits[i]) result |= 1 << i;
        }
        return result;
    }

    public static void toBool(boolean[] out, int value) {
        for (int i = 0; i < out.length; i++) {
            out[i] = (value & (1 << i)) != 0;
        }
    }

    public static long intsToLong(int a, int b) {
        return ((long) a << 32) | (b & 0xFFFFFFFFL);
    }

    public static int intFromLongA(long value) {
        return (int) (value >> 32);
    }

    public static int intFromLongB(long value) {
        return (int) value;
    }

    public static int shortsToInt(int a, int b) {
        return ((short) a << 16) | ((short) b & 0xFFFF);
    }

    public static short shortFromIntA(int value) {
        return (short) (value >> 16);
    }

    public static short shortFromIntB(int value) {
        return (short) value;
    }

    public static short bytesToShort(int a, int b) {
        return (short) (((a & 255) << 8) | (b & 255));
    }

    public static byte byteFromShortA(short value) {
        return (byte) (value >> 8);
    }

    public static byte byteFromShortB(short value) {
        return (byte) value;
    }

    public static int toUShort(byte[] bytes, int offset) {
        return ((bytes[offset] & 255) << 8) | (bytes[offset + 1] & 255);
    }

    public static int toInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 255) << 24) | ((bytes[offset + 1] & 255) << 16) | ((bytes[offset + 2] & 255) << 8) | (bytes[offset + 3] & 255);
    }

    public static long toLong(byte[] bytes, int offset) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result = (result << 8) | (bytes[offset + i] & 255L);
        }
        return result;
    }

    public static UUID toUUID(byte[] bytes, int offset) {
        return new UUID(toLong(bytes, offset), toLong(bytes, offset + 8));
    }

    public static List<UUID> toUUIDList(byte[] bytes) {
        List<UUID> list = new ArrayList<>(bytes.length / 16);
        for (int offset = 0; offset + 16 <= bytes.length; offset += 16) {
            list.add(toUUID(bytes, offset));
        }
        return list;
    }

    public static void fromUShort(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte) (value >> 8);
        bytes[offset + 1] = (byte) value;
    }

    public static void fromInt(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte) (value >> 24);
        bytes[offset + 1] = (byte) (value >> 16);
        bytes[offset + 2] = (byte) (value >> 8);
        bytes[offset + 3] = (byte) value;
    }

    public static void fromLong(byte[] bytes, int offset, long value) {
        for (int i = 7; i >= 0; i--) {
            bytes[offset + i] = (byte) value;
            value >>= 8;
        }
    }

    public static void fromUUID(byte[] bytes, int offset, UUID uuid) {
        fromLong(bytes, offset, uuid.getMostSignificantBits());
        fromLong(bytes, offset + 8, uuid.getLeastSignificantBits());
    }

    public static byte[] fromUUIDList(Collection<UUID> uuids) {
        byte[] bytes = new byte[uuids.size() * 16];
        int offset = 0;
        for (UUID uuid : uuids) {
            fromUUID(bytes, offset, uuid);
            offset += 16;
        }
        return bytes;
    }
}

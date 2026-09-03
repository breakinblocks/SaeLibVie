package com.breakinblocks.saelibvie.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.DataOutputStream;
import java.io.OutputStream;

public final class NbtUtil {
    private NbtUtil() {
    }

    public static final class CountingOutputStream extends OutputStream {
        private long size;

        @Override
        public void write(int b) {
            size++;
        }

        @Override
        public void write(byte[] b, int off, int len) {
            size += len;
        }

        public long getSize() {
            return size;
        }
    }

    public static long getSizeInBytes(CompoundTag tag, boolean compressed) {
        try {
            CountingOutputStream counter = new CountingOutputStream();
            if (compressed) {
                NbtIo.writeCompressed(tag, counter);
            } else {
                NbtIo.write(tag, new DataOutputStream(counter));
            }
            return counter.getSize();
        } catch (Exception e) {
            return -1L;
        }
    }
}

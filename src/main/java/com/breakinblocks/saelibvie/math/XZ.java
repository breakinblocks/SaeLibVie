package com.breakinblocks.saelibvie.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Locale;

public record XZ(int x, int z) {
    public static final Codec<XZ> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(XZ::x),
            Codec.INT.fieldOf("z").forGetter(XZ::z)
    ).apply(instance, XZ::new));

    public static final StreamCodec<FriendlyByteBuf, XZ> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, XZ::x,
            ByteBufCodecs.INT, XZ::z,
            XZ::new);

    public static XZ of(int x, int z) {
        return new XZ(x, z);
    }

    public static XZ of(long packed) {
        return new XZ((int) packed, (int) (packed >> 32));
    }

    public static XZ of(ChunkPos pos) {
        return new XZ(pos.x(), pos.z());
    }

    public static XZ chunkFromBlock(int x, int z) {
        return new XZ(x >> 4, z >> 4);
    }

    public static XZ chunkFromBlock(Vec3i pos) {
        return chunkFromBlock(pos.getX(), pos.getZ());
    }

    public static XZ regionFromChunk(int x, int z) {
        return new XZ(x >> 5, z >> 5);
    }

    public static XZ regionFromChunk(ChunkPos pos) {
        return regionFromChunk(pos.x(), pos.z());
    }

    public static XZ regionFromBlock(int x, int z) {
        return new XZ(x >> 9, z >> 9);
    }

    public static XZ regionFromBlock(Vec3i pos) {
        return regionFromBlock(pos.getX(), pos.getZ());
    }

    public ChunkDimPos dim(ResourceKey<Level> dimension) {
        return new ChunkDimPos(dimension, new ChunkPos(x, z));
    }

    public ChunkDimPos dim(Level level) {
        return dim(level.dimension());
    }

    public XZ offset(int dx, int dz) {
        return new XZ(x + dx, z + dz);
    }

    public long toLong() {
        return x & 0xFFFFFFFFL | (z & 0xFFFFFFFFL) << 32;
    }

    public String toRegionString() {
        return String.format(Locale.ROOT, "%05X-%05X", x + 60000, z + 60000);
    }
}

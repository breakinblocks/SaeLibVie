package com.breakinblocks.saelibvie.math;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public record ChunkDimPos(ResourceKey<Level> dimension, ChunkPos chunkPos) implements Comparable<ChunkDimPos> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ChunkDimPos> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), ChunkDimPos::dimension,
            ByteBufCodecs.INT, ChunkDimPos::x,
            ByteBufCodecs.INT, ChunkDimPos::z,
            ChunkDimPos::new);

    public ChunkDimPos(ResourceKey<Level> dimension, int x, int z) {
        this(dimension, new ChunkPos(x, z));
    }

    public ChunkDimPos(Level level, BlockPos pos) {
        this(level.dimension(), pos.getX() >> 4, pos.getZ() >> 4);
    }

    public ChunkDimPos(Entity entity) {
        this(entity.level(), entity.blockPosition());
    }

    public int x() {
        return chunkPos.x;
    }

    public int z() {
        return chunkPos.z;
    }

    public ChunkDimPos offset(int dx, int dz) {
        return new ChunkDimPos(dimension, chunkPos.x + dx, chunkPos.z + dz);
    }

    @Override
    public int compareTo(ChunkDimPos other) {
        int byDim = dimension.location().toString().compareTo(other.dimension.location().toString());
        if (byDim != 0) return byDim;
        return Long.compare(chunkPos.toLong(), other.chunkPos.toLong());
    }
}

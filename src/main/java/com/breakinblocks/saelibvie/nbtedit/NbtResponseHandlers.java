package com.breakinblocks.saelibvie.nbtedit;

import com.breakinblocks.saelibvie.SaeLibVie;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.util.ProblemReporter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NbtResponseHandlers {
    @FunctionalInterface
    public interface Handler {
        void handle(ServerPlayer player, CompoundTag info, CompoundTag data);
    }

    private static final Map<String, Handler> HANDLERS = new ConcurrentHashMap<>();
    private static final Handler NO_OP = (player, info, data) -> {
    };

    private NbtResponseHandlers() {
    }

    private static ValueInput input(HolderLookup.Provider registries, CompoundTag data) {
        return TagValueInput.create(ProblemReporter.DISCARDING, registries, data);
    }

    public static void register(String type, Handler handler) {
        HANDLERS.put(type, handler);
    }

    public static void dispatch(ServerPlayer player, CompoundTag info, CompoundTag data) {
        String type = info.getStringOr("type", "");
        HANDLERS.getOrDefault(type, NO_OP).handle(player, info, data);
    }

    public static void registerBuiltIn() {
        register("item", (player, info, data) -> {
            ItemStack.CODEC.parse(player.registryAccess().createSerializationContext(NbtOps.INSTANCE), data)
                    .result()
                    .ifPresent(stack -> player.setItemInHand(InteractionHand.MAIN_HAND, stack));
        });
        register("block", (player, info, data) -> {
            BlockPos pos = info.read("pos", BlockPos.CODEC).orElse(null);
            if (pos == null) return;
            ServerLevel level = player.level();
            if (!level.isLoaded(pos)) return;
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) return;
            data.putInt("x", pos.getX());
            data.putInt("y", pos.getY());
            data.putInt("z", pos.getZ());
            info.getString("id").ifPresent(id -> data.putString("id", id));
            blockEntity.loadWithComponents(input(level.registryAccess(), data));
            blockEntity.setChanged();
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        });
        register("player", (player, info, data) -> {
            UUID id = info.read("id", UUIDUtil.CODEC).orElse(null);
            if (id == null) return;
            ServerPlayer target = player.level().getServer().getPlayerList().getPlayer(id);
            if (target == null) return;
            target.load(input(target.registryAccess(), data));
            target.setUUID(id);
            target.teleportTo(target.getX(), target.getY(), target.getZ());
        });
        register("entity", (player, info, data) -> {
            Entity entity = player.level().getEntity(info.getIntOr("id", -1));
            if (entity == null) return;
            UUID uuid = entity.getUUID();
            entity.load(input(player.registryAccess(), data));
            entity.setUUID(uuid);
        });
        SaeLibVie.LOGGER.debug("Registered built-in NBT response handlers");
    }
}

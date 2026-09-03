package com.breakinblocks.saelibvie.nbtedit;

import com.breakinblocks.saelibvie.SaeLibVie;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;

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

    public static void register(String type, Handler handler) {
        HANDLERS.put(type, handler);
    }

    public static void dispatch(ServerPlayer player, CompoundTag info, CompoundTag data) {
        String type = info.getString("type");
        HANDLERS.getOrDefault(type, NO_OP).handle(player, info, data);
    }

    public static void registerBuiltIn() {
        register("item", (player, info, data) -> {
            ItemStack.parse(player.registryAccess(), data).ifPresent(stack -> player.setItemInHand(InteractionHand.MAIN_HAND, stack));
        });
        register("block", (player, info, data) -> {
            BlockPos pos = NbtUtils.readBlockPos(info, "pos").orElse(null);
            if (pos == null) return;
            ServerLevel level = player.serverLevel();
            if (!level.isLoaded(pos)) return;
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) return;
            data.putInt("x", pos.getX());
            data.putInt("y", pos.getY());
            data.putInt("z", pos.getZ());
            if (info.contains("id")) {
                data.putString("id", info.getString("id"));
            }
            blockEntity.loadWithComponents(data, level.registryAccess());
            blockEntity.setChanged();
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        });
        register("player", (player, info, data) -> {
            if (!info.hasUUID("id")) return;
            UUID id = info.getUUID("id");
            ServerPlayer target = player.server.getPlayerList().getPlayer(id);
            if (target == null) return;
            target.load(data);
            target.setUUID(id);
            target.teleportTo(target.getX(), target.getY(), target.getZ());
        });
        register("entity", (player, info, data) -> {
            if (!info.contains("id")) return;
            Entity entity = player.serverLevel().getEntity(info.getInt("id"));
            if (entity == null) return;
            UUID uuid = entity.getUUID();
            entity.load(data);
            entity.setUUID(uuid);
        });
        SaeLibVie.LOGGER.debug("Registered built-in NBT response handlers");
    }
}

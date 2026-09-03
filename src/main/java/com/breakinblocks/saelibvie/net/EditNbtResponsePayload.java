package com.breakinblocks.saelibvie.net;

import com.breakinblocks.saelibvie.SaeLibVie;
import com.breakinblocks.saelibvie.nbtedit.NbtEditSessions;
import com.breakinblocks.saelibvie.nbtedit.NbtResponseHandlers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EditNbtResponsePayload(CompoundTag info, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<EditNbtResponsePayload> TYPE = new Type<>(SaeLibVie.id("edit_nbt_response"));
    public static final StreamCodec<FriendlyByteBuf, EditNbtResponsePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, EditNbtResponsePayload::info,
            ByteBufCodecs.COMPOUND_TAG, EditNbtResponsePayload::tag,
            EditNbtResponsePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EditNbtResponsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                SaeLibVie.LOGGER.warn("{} sent an NBT edit response without permission", player.getGameProfile().name());
                return;
            }
            CompoundTag expected = NbtEditSessions.get(player.getUUID());
            if (expected == null || !expected.equals(payload.info())) {
                SaeLibVie.LOGGER.warn("{} sent an NBT edit response that does not match the open editor", player.getGameProfile().name());
                return;
            }
            NbtEditSessions.clear(player.getUUID());
            NbtResponseHandlers.dispatch(player, payload.info(), payload.tag());
        });
    }
}

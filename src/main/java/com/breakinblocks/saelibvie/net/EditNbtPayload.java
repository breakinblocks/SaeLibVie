package com.breakinblocks.saelibvie.net;

import com.breakinblocks.saelibvie.SaeLibVie;
import com.breakinblocks.saelibvie.nbtedit.NbtEditScreen;
import com.breakinblocks.saelibvie.ui.util.ClientTasks;
import com.breakinblocks.saelibvie.util.NbtUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EditNbtPayload(CompoundTag info, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<EditNbtPayload> TYPE = new Type<>(SaeLibVie.id("edit_nbt"));
    public static final StreamCodec<FriendlyByteBuf, EditNbtPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, EditNbtPayload::info,
            ByteBufCodecs.COMPOUND_TAG, EditNbtPayload::tag,
            EditNbtPayload::new);
    public static final long RESPONSE_SIZE_LIMIT = 30000L;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EditNbtPayload payload, IPayloadContext context) {
        ClientTasks.later(() -> NbtEditScreen.open(payload.info(), payload.tag(), Minecraft.getInstance().screen, (accepted, edited) -> {
            if (!accepted) return;
            long size = NbtUtil.getSizeInBytes(edited, false);
            if (size < 0 || size >= RESPONSE_SIZE_LIMIT) {
                SaeLibVie.LOGGER.error("Edited NBT is too large to send ({} bytes)", size);
                return;
            }
            PacketDistributor.sendToServer(new EditNbtResponsePayload(payload.info(), edited));
        }));
    }
}

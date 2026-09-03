package com.breakinblocks.saelibvie.net;

import com.breakinblocks.saelibvie.SaeLibVie;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UiActionPayload(int containerId, int action, int value, String text) implements CustomPacketPayload {
    public static final int MAX_TEXT = 256;
    public static final Type<UiActionPayload> TYPE = new Type<>(SaeLibVie.id("ui_action"));

    public static final StreamCodec<FriendlyByteBuf, UiActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, UiActionPayload::containerId,
            ByteBufCodecs.VAR_INT, UiActionPayload::action,
            ByteBufCodecs.VAR_INT, UiActionPayload::value,
            ByteBufCodecs.stringUtf8(MAX_TEXT), UiActionPayload::text,
            UiActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(UiActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (player.containerMenu == null || player.containerMenu.containerId != payload.containerId()) return;
            if (player.containerMenu instanceof UiActionHandler handler) {
                if (!handler.handleUiAction(player, payload.action(), payload.value(), payload.text())) {
                    SaeLibVie.LOGGER.debug("Unhandled UI action {} from {}", payload.action(), player.getGameProfile().getName());
                }
            }
        });
    }
}

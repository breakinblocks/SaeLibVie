package com.breakinblocks.saelibvie.net;

import com.breakinblocks.saelibvie.SaeLibVie;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SaeLibVie.MOD_ID)
public final class SaeNetworking {
    private SaeNetworking() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(SaeLibVie.MOD_ID).versioned("2").optional();
        NetUtil.registerC2S(registrar, UiActionPayload.TYPE, UiActionPayload.STREAM_CODEC, UiActionPayload::handleOnServer);
        NetUtil.registerS2C(registrar, EditNbtPayload.TYPE, EditNbtPayload.STREAM_CODEC, EditNbtPayload::handle);
        NetUtil.registerC2S(registrar, EditNbtResponsePayload.TYPE, EditNbtResponsePayload.STREAM_CODEC, EditNbtResponsePayload::handle);
    }

    public static void sendAction(AbstractContainerMenu menu, int action) {
        sendAction(menu, action, 0, "");
    }

    public static void sendAction(AbstractContainerMenu menu, int action, int value) {
        sendAction(menu, action, value, "");
    }

    public static void sendAction(AbstractContainerMenu menu, int action, int value, String text) {
        String clipped = text.length() > UiActionPayload.MAX_TEXT ? text.substring(0, UiActionPayload.MAX_TEXT) : text;
        PacketDistributor.sendToServer(new UiActionPayload(menu.containerId, action, value, clipped));
    }
}

package com.breakinblocks.saelibvie.net;

import net.minecraft.server.level.ServerPlayer;

public interface UiActionHandler {
    boolean handleUiAction(ServerPlayer player, int action, int value, String text);
}

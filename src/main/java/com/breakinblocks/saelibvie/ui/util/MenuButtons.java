package com.breakinblocks.saelibvie.ui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class MenuButtons {
    private MenuButtons() {
    }

    public static void send(AbstractContainerMenu menu, int id) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }
}

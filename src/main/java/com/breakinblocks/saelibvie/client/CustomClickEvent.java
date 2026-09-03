package com.breakinblocks.saelibvie.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

public class CustomClickEvent extends Event {
    private final ResourceLocation id;
    private boolean handled;

    public CustomClickEvent(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public void setHandled() {
        this.handled = true;
    }

    public boolean isHandled() {
        return handled;
    }
}

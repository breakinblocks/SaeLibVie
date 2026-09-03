package com.breakinblocks.saelibvie.client;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;

public class CustomClickEvent extends Event {
    private final Identifier id;
    private boolean handled;

    public CustomClickEvent(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return id;
    }

    public void setHandled() {
        this.handled = true;
    }

    public boolean isHandled() {
        return handled;
    }
}

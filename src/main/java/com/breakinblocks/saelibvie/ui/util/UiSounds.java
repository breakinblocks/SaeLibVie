package com.breakinblocks.saelibvie.ui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public final class UiSounds {
    private UiSounds() {
    }

    public static void click() {
        play(SoundEvents.UI_BUTTON_CLICK.value(), 1f);
    }

    public static void play(SoundEvent sound, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
    }
}

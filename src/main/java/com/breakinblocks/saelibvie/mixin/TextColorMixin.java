package com.breakinblocks.saelibvie.mixin;

import com.breakinblocks.saelibvie.text.TextColors;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextColor.class)
public class TextColorMixin {
    @Inject(method = "parseColor", at = @At("HEAD"), cancellable = true)
    private static void saelibvie$parseColor(String text, CallbackInfoReturnable<DataResult<TextColor>> cir) {
        TextColor custom = TextColors.get(text);
        if (custom != null) {
            cir.setReturnValue(DataResult.success(custom, Lifecycle.stable()));
        }
    }
}

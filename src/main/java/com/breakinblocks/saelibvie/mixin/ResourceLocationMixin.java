package com.breakinblocks.saelibvie.mixin;

import com.breakinblocks.saelibvie.text.StringUtil;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ResourceLocation.class)
public class ResourceLocationMixin {
    @Inject(method = "isValidPath", at = @At("RETURN"), cancellable = true)
    private static void saelibvie$isValidPath(String path, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && StringUtil.ignoreResourceLocationErrors) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isValidNamespace", at = @At("RETURN"), cancellable = true)
    private static void saelibvie$isValidNamespace(String namespace, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && StringUtil.ignoreResourceLocationErrors) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "validPathChar", at = @At("RETURN"), cancellable = true)
    private static void saelibvie$validPathChar(char c, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && StringUtil.ignoreResourceLocationErrors) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "validNamespaceChar", at = @At("RETURN"), cancellable = true)
    private static void saelibvie$validNamespaceChar(char c, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && StringUtil.ignoreResourceLocationErrors) {
            cir.setReturnValue(true);
        }
    }
}

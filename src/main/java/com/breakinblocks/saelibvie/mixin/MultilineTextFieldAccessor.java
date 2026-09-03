package com.breakinblocks.saelibvie.mixin;

import net.minecraft.client.gui.components.MultilineTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultilineTextField.class)
public interface MultilineTextFieldAccessor {
    @Accessor("selectCursor")
    int saelibvie$getSelectCursor();

    @Accessor("selectCursor")
    void saelibvie$setSelectCursor(int cursor);
}

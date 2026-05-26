package dev.imb11.skinshuffle.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.client.gui.components.toasts.ToastManager;

@Mixin(ToastManager.class)
public interface ToastManagerAccessor {
    @Accessor("visibleToasts")
    List<ToastManager.ToastInstance<?>> getVisibleEntries();
}

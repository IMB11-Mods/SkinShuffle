package dev.imb11.skinshuffle.mixin.screen;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Shadow
    protected abstract void removeWidget(GuiEventListener child);

    @Shadow
    protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T drawableElement);

    @Inject(method = "onClose", at = @At("HEAD"))
    protected void closeHook(CallbackInfo ci) {

    }

    @Inject(method = "added", at = @At("HEAD"))
    protected void onDisplayedHook(CallbackInfo ci) {

    }
}

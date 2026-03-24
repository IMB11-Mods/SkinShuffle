package dev.imb11.skinshuffle.mixin;

import dev.imb11.skinshuffle.client.SkinShuffleClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @Inject(method = "runTick", at = @At("HEAD"))
    public void render(boolean tick, CallbackInfo ci) {
        SkinShuffleClient.TOTAL_TICK_DELTA += Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
    }
}

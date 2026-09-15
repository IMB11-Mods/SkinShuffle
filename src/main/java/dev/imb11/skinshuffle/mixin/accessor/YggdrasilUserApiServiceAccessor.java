package dev.imb11.skinshuffle.mixin.accessor;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.authlib.services.MinecraftServicesUserApiService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServicesUserApiService.class)
public interface YggdrasilUserApiServiceAccessor {
    @Accessor("minecraftClient")
    MinecraftClient getMinecraftClient();
}

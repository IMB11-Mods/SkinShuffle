package dev.imb11.skinshuffle.mixin.accessor;

import com.mojang.authlib.minecraft.UserApiService;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftClientAccessor {
    @Accessor()
    UserApiService getUserApiService();
}

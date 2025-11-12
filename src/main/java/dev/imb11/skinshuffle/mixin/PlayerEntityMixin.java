package dev.imb11.skinshuffle.mixin;

import com.mojang.authlib.GameProfile;
import dev.imb11.skinshuffle.client.config.SkinPresetManager;
import dev.imb11.skinshuffle.client.preset.SkinPreset;
import dev.imb11.skinshuffle.compat.CapesCompat;
import dev.imb11.skinshuffle.compat.MinecraftCapesCompat;
import dev.imb11.skinshuffle.util.SkinShuffleClientPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class PlayerEntityMixin extends Player implements SkinShuffleClientPlayer {
    @Shadow
    @Nullable
    private PlayerInfo playerInfo;

    private @Unique PlayerSkin prevTextures;

    public PlayerEntityMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "getSkin", at = @At("TAIL"), cancellable = true)
    private void modifySkinTextures(CallbackInfoReturnable<PlayerSkin> cir) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.player != null) {
            if (this.getUUID().equals(client.player.getUUID())) {
                SkinPreset currentPreset = SkinPresetManager.getChosenPreset();
                var textures = currentPreset.getSkin().getSkinTextures();

                if (CapesCompat.IS_INSTALLED) {
                    textures = CapesCompat.loadTextures(this.getGameProfile(), textures);
                } else if (MinecraftCapesCompat.IS_INSTALLED) {
                    textures = MinecraftCapesCompat.loadTextures(uuid, textures);
                }

                if (currentPreset.getSkin().isLoading()) {
                    if (prevTextures != null)
                        cir.setReturnValue(prevTextures);
                    return;
                }
                prevTextures = textures;
                cir.setReturnValue(textures);
            }
        }
    }

    @Override
    public void skinShuffle$refreshPlayerListEntry() {
        this.playerInfo = null;
    }
}

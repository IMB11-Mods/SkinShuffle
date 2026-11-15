package dev.imb11.skinshuffle.mixin;

import com.mojang.authlib.GameProfile;
import dev.imb11.skinshuffle.SkinShuffle;
import dev.imb11.skinshuffle.client.config.SkinPresetManager;
import dev.imb11.skinshuffle.client.preset.SkinPreset;
import dev.imb11.skinshuffle.client.skin.CapeManager;
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

                // Cape mods are optional and their APIs change often: never let one crash rendering.
                try {
                    if (CapesCompat.IS_INSTALLED) {
                        textures = CapesCompat.loadTextures(this.getGameProfile(), textures);
                    } else if (MinecraftCapesCompat.IS_INSTALLED) {
                        textures = MinecraftCapesCompat.loadTextures(uuid, textures);
                    }
                } catch (Exception | LinkageError e) {
                    SkinShuffle.LOGGER.debug("Cape mod compatibility failed", e);
                }

                // Show the preset's own cape on the local player right away, without waiting for the
                // account's cape change to round-trip through Mojang's servers.
                if (currentPreset.hasCape()) {
                    var presetCape = CapeManager.getTexture(currentPreset);
                    if (presetCape != null) {
                        textures = new PlayerSkin(textures.body(), presetCape, textures.elytra(), textures.model(), textures.secure());
                    }
                } else if (SkinPreset.NO_CAPE.equals(currentPreset.getCapeId())) {
                    textures = new PlayerSkin(textures.body(), null, textures.elytra(), textures.model(), textures.secure());
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

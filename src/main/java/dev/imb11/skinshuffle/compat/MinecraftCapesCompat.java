package dev.imb11.skinshuffle.compat;

import dev.imb11.skinshuffle.compat.api.CompatHandler;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraftcapes.config.MinecraftCapesConfig;
import net.minecraftcapes.player.PlayerHandler;

import java.util.UUID;

public class MinecraftCapesCompat implements CompatHandler {
    public static boolean IS_INSTALLED = false;

    public static PlayerSkin loadTextures(UUID uuid, PlayerSkin textures) {
        PlayerHandler playerHandler = PlayerHandler.get(uuid);

        ClientAsset.Texture capeTexture = textures.cape();
        ClientAsset.Texture elytraTexture = textures.elytra();

        if (MinecraftCapesConfig.isCapeVisible() && playerHandler.getCapeLocation() != null) {
            PlayerSkin finalTextures = textures;
            capeTexture = new ClientAsset.Texture() {
                @Override
                public ResourceLocation texturePath() {
                    return playerHandler.getCapeLocation();
                }

                @Override
                public ResourceLocation id() {
                    return finalTextures.cape().id();
                }
            };
            elytraTexture = new ClientAsset.Texture() {
                @Override
                public ResourceLocation texturePath() {
                    return finalTextures.elytra().texturePath();
                }

                @Override
                public ResourceLocation id() {
                    return finalTextures.elytra().id();
                }
            };
        }

        textures = new PlayerSkin(
                textures.body(),
                capeTexture,
                elytraTexture,
                textures.model(),
                textures.secure()
        );

        return textures;
    }

    @Override
    public String getID() {
        return "minecraftcapes";
    }

    @Override
    public void execute() {
        MinecraftCapesCompat.IS_INSTALLED = true;
    }
}

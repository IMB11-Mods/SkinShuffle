package dev.imb11.skinshuffle.compat;

import dev.imb11.skinshuffle.compat.api.CompatHandler;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.minecraftcapes.config.MinecraftCapesConfig;
import net.minecraftcapes.player.PlayerHandler;

import java.util.UUID;

public class MinecraftCapesCompat implements CompatHandler {
    public static boolean IS_INSTALLED = false;

    public static SkinTextures loadTextures(UUID uuid, SkinTextures textures) {
        PlayerHandler playerHandler = PlayerHandler.get(uuid);

        AssetInfo.TextureAsset capeTexture = textures.cape();
        AssetInfo.TextureAsset elytraTexture = textures.elytra();

        if (MinecraftCapesConfig.isCapeVisible() && playerHandler.getCapeLocation() != null) {
            SkinTextures finalTextures = textures;
            capeTexture = new AssetInfo.TextureAsset() {
                @Override
                public Identifier texturePath() {
                    return playerHandler.getCapeLocation();
                }

                @Override
                public Identifier id() {
                    return finalTextures.cape().id();
                }
            };
            elytraTexture = new AssetInfo.TextureAsset() {
                @Override
                public Identifier texturePath() {
                    return finalTextures.elytra().texturePath();
                }

                @Override
                public Identifier id() {
                    return finalTextures.elytra().id();
                }
            };
        }

        textures = new SkinTextures(
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

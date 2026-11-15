package dev.imb11.skinshuffle.compat;

import dev.imb11.skinshuffle.compat.api.CompatHandler;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
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

        Identifier capeLocation = playerHandler == null ? null : playerHandler.getCapeLocation();
        if (capeLocation != null && MinecraftCapesConfig.isCapeVisible()) {
            // MinecraftCapes registers its own texture, so point the cape (and elytra) at it. The vanilla
            // cape/elytra may be null when the account has no cape, so never dereference them.
            capeTexture = new CapeTexture(capeLocation);
            elytraTexture = new CapeTexture(capeLocation);
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

    private record CapeTexture(Identifier id) implements ClientAsset.Texture {
        @Override
        public Identifier texturePath() {
            return id;
        }
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

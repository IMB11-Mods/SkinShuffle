package dev.imb11.skinshuffle.compat;

import com.mojang.authlib.GameProfile;
import dev.imb11.skinshuffle.compat.api.CompatHandler;
import me.cael.capes.handler.PlayerHandler;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerSkin;

public class CapesCompat implements CompatHandler {
    public static boolean IS_INSTALLED = false;

    public static PlayerSkin loadTextures(GameProfile profile, PlayerSkin textures) {
        PlayerHandler handler = PlayerHandler.Companion.fromProfile(profile);

        ClientAsset.Texture capeTexture = textures.cape();
        ClientAsset.Texture elytraTexture = textures.elytra();

        if (handler.getHasCape()) {
            capeTexture = handler.getCape();

            if (handler.getHasElytraTexture())
                elytraTexture = capeTexture;
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
        return "capes";
    }

    @Override
    public void execute() {
        CapesCompat.IS_INSTALLED = true;
    }
}

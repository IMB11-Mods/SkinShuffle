package dev.imb11.skinshuffle.compat;

import com.mojang.authlib.GameProfile;
import dev.imb11.skinshuffle.compat.api.CompatHandler;
import me.cael.capes.handler.PlayerHandler;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;

public class CapesCompat implements CompatHandler {
    public static boolean IS_INSTALLED = false;

    public static SkinTextures loadTextures(GameProfile profile, SkinTextures textures) {
        PlayerHandler handler = PlayerHandler.Companion.fromProfile(profile);

        AssetInfo.TextureAsset capeTexture = textures.cape();
        AssetInfo.TextureAsset elytraTexture = textures.elytra();

        if (handler.getHasCape()) {
            capeTexture = handler.getCape();

            if (handler.getHasElytraTexture())
                elytraTexture = capeTexture;
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
        return "capes";
    }

    @Override
    public void execute() {
        CapesCompat.IS_INSTALLED = true;
    }
}

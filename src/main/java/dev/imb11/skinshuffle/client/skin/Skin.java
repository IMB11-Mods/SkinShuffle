package dev.imb11.skinshuffle.client.skin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.imb11.skinshuffle.MixinStatics;
import dev.imb11.skinshuffle.SkinShuffle;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;

public interface Skin {
    Map<Identifier, MapCodec<? extends Skin>> TYPES = Map.of(
            UrlSkin.SERIALIZATION_ID, UrlSkin.CODEC,
            ResourceSkin.SERIALIZATION_ID, ResourceSkin.CODEC,
            ConfigSkin.SERIALIZATION_ID, ConfigSkin.CODEC,
            FileSkin.SERIALIZATION_ID, FileSkin.CODEC,
            UsernameSkin.SERIALIZATION_ID, UsernameSkin.CODEC,
            UUIDSkin.SERIALIZATION_ID, UUIDSkin.CODEC
    );
    Codec<Skin> CODEC = Identifier.CODEC.dispatch("type", Skin::getSerializationId, TYPES::get);

    static ResourceSkin randomDefaultSkin() {
        var uuid = UUID.randomUUID();
        var txt = DefaultPlayerSkin.get(uuid);
        return new ResourceSkin(txt.body().texturePath(), txt.model().name());
    }

    @Nullable ClientAsset.Texture getTextureAsset();

    Identifier getTexture();


    default PlayerSkin getSkinTextures() {
        Minecraft client = Minecraft.getInstance();
        CompletableFuture<Optional<PlayerSkin>> textureSupplier =
                client.getSkinManager().get(client.getGameProfile());

        CompletableFuture<Optional<PlayerSkin>> clientTexture;
        if (MixinStatics.INITIAL_SKIN_TEXTURES != null
                && MixinStatics.INITIAL_SKIN_TEXTURES.isDone()
                && !MixinStatics.INITIAL_SKIN_TEXTURES.isCompletedExceptionally()) {
            clientTexture = MixinStatics.INITIAL_SKIN_TEXTURES.join();
        } else {
            clientTexture = textureSupplier;
        }

        // The vanilla skin lookup can fail or come back empty (offline mode, auth failure, API outage). That
        // must not crash rendering: just render the preset's skin without the account's cape/elytra.
        ClientAsset.Texture cape = null;
        ClientAsset.Texture elytra = null;
        try {
            if (clientTexture != null && clientTexture.isDone() && !clientTexture.isCompletedExceptionally()) {
                Optional<PlayerSkin> vanilla = clientTexture.get();
                if (vanilla.isPresent()) {
                    cape = vanilla.get().cape();
                    elytra = vanilla.get().elytra();
                }
            }
        } catch (Exception e) {
            SkinShuffle.LOGGER.debug("Could not read the account's vanilla cape textures", e);
        }

        return new PlayerSkin(this.getTextureAsset(), cape, elytra, getModelEnum(), false);
    }

    default PlayerModelType getModelEnum() {
        String modelString = getModel();
        if (modelString == null) {
            return PlayerModelType.WIDE;
        }

        return switch (modelString.toLowerCase()) {
            case "slim" -> PlayerModelType.SLIM;
            case "classic", "wide", "default" -> PlayerModelType.WIDE;
            default -> {
                try {
                    yield PlayerModelType.valueOf(modelString.toUpperCase());
                } catch (IllegalArgumentException e) {
                    yield PlayerModelType.WIDE;
                }
            }
        };
    }

    boolean isLoading();

    String getModel();

    void setModel(String value);

    Identifier getSerializationId();

    /**
     * Saves this skin to the config and returns a new reference to it.
     * THIS METHOD CAN AND WILL THROW, MAKE SURE TO CATCH IT!
     *
     * @return A new reference to this skin.
     * @throws RuntimeException If the skin could not be saved for whatever reason.
     */
    ConfigSkin saveToConfig();
}

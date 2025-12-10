package dev.imb11.skinshuffle.client.skin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.imb11.skinshuffle.MixinStatics;
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
        if (MixinStatics.INITIAL_SKIN_TEXTURES.isDone()) clientTexture = MixinStatics.INITIAL_SKIN_TEXTURES.join();
        else clientTexture = textureSupplier;

        try {
            return new PlayerSkin(this.getTextureAsset(), clientTexture.get().get().cape(), clientTexture.get().get().elytra(), getModelEnum(), false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

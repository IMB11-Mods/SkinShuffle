package dev.imb11.skinshuffle.client.skin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.imb11.skinshuffle.SkinShuffle;
import dev.imb11.skinshuffle.api.MojangSkinAPI;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

public class UUIDSkin extends UrlSkin {
    public static final Identifier SERIALIZATION_ID = SkinShuffle.id("uuid");

    public static final MapCodec<UUIDSkin> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("uuid").forGetter(skin -> skin.uuid.toString()),
            Codec.STRING.optionalFieldOf("model").forGetter(skin -> Optional.ofNullable(skin.model))
    ).apply(instance, (uuid, model) -> new UUIDSkin(UUID.fromString(uuid), model.orElse(null))));

    protected UUID uuid;

    public UUIDSkin(UUID uuid, @Nullable String model) {
        super(model);
        this.uuid = uuid;
    }

    protected UUIDSkin(@Nullable String model) {
        super(model);
    }

    @Override
    protected Object getTextureUniqueness() {
        return uuid;
    }

    @Override
    protected @Nullable Supplier<AbstractTexture> loadTextureFactory() {
        var profile = MojangSkinAPI.getPlayerSkinTexture(uuid.toString());

        if (profile.skinURL() == null) {
            return null;
        }

        url = profile.skinURL();
        if (model == null) {
            model = profile.modelType();
            cacheModel();
        }

        return super.loadTextureFactory();
    }

    @Override
    public Identifier getSerializationId() {
        return SERIALIZATION_ID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UUIDSkin uuidSkin = (UUIDSkin) o;

        return Objects.equals(uuid, uuidSkin.uuid) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }
}

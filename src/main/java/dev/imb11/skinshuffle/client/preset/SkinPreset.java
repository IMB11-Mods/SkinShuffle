package dev.imb11.skinshuffle.client.preset;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.imb11.skinshuffle.api.MojangSkinAPI;
import dev.imb11.skinshuffle.client.skin.ResourceSkin;
import dev.imb11.skinshuffle.client.skin.Skin;
import dev.imb11.skinshuffle.client.skin.UrlSkin;
import dev.imb11.skinshuffle.util.NetworkingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class SkinPreset {
    public static final Codec<SkinPreset> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Skin.CODEC.fieldOf("skin").forGetter(SkinPreset::getSkin),
                    Codec.STRING.fieldOf("name").forGetter(SkinPreset::getName),
                    Codec.INT.optionalFieldOf("keybindId", -1).forGetter(SkinPreset::getKeybindId),
                    Codec.STRING.optionalFieldOf("capeId").forGetter(preset -> Optional.ofNullable(preset.capeId)),
                    Codec.STRING.optionalFieldOf("capeUrl").forGetter(preset -> Optional.ofNullable(preset.capeUrl))
            ).apply(instance, (skin, name, keybindId, capeId, capeUrl) ->
                    new SkinPreset(skin, name, keybindId, capeId.orElse(null), capeUrl.orElse(null))));

    /**
     * {@link #capeId} value meaning "this preset hides the cape". A null capeId means the preset does not
     * manage the cape at all (the default, and what presets saved before cape support behave like).
     */
    public static final String NO_CAPE = "";

    private String name;
    private Skin skin;
    private int keybindId;
    @Nullable
    private String capeId;
    @Nullable
    private String capeUrl;

    public SkinPreset(Skin skin) {
        this(skin, "Unnamed Preset", -1);
    }

    public SkinPreset(Skin skin, String name) {
        this(skin, name, -1);
    }

    public SkinPreset(Skin skin, String name, int keybindId) {
        this(skin, name, keybindId, null, null);
    }

    public SkinPreset(Skin skin, String name, int keybindId, @Nullable String capeId, @Nullable String capeUrl) {
        this.skin = skin;
        this.name = name;
        this.keybindId = keybindId;
        this.capeId = capeId;
        this.capeUrl = capeUrl;
    }

    public static SkinPreset generateDefaultPreset() {
        Minecraft client = Minecraft.getInstance();
        User session = client.getUser();
        String name = session.getName();

        if (!NetworkingUtil.isLoggedIn()) {
            Skin skin = new ResourceSkin(Identifier.parse("minecraft:textures/entity/player/wide/steve.png"), "default");
            return new SkinPreset(skin, name, -1);
        } else {
            var skinQueryResult = MojangSkinAPI.getPlayerSkinTexture(String.valueOf(client.getGameProfile().id()));

            if (skinQueryResult.usesDefaultSkin()) {
                var provider = client.getSkinManager();

                if (provider == null) {
                    return new SkinPreset(new ResourceSkin(Identifier.parse("minecraft:textures/entity/player/wide/steve.png"), "default"));
                }

                PlayerSkin skinTexture = provider.createLookup(client.getGameProfile(), false).get();
                Skin skin = new ResourceSkin(skinTexture.body().texturePath(), skinTexture.model().name());

                return new SkinPreset(skin, name, -1);
            }

            try (var urlSkin = new UrlSkin(skinQueryResult.skinURL(), skinQueryResult.modelType())) {
                return new SkinPreset(urlSkin.saveToConfig(), name, -1);
            }
        }
    }

    public Skin getSkin() {
        return skin;
    }

    public void setSkin(Skin skin) {
        this.skin = skin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKeybindId() {
        return keybindId;
    }

    public void setKeybindId(int keybindId) {
        this.keybindId = keybindId;
    }

    /**
     * @return The id of the owned cape this preset equips, {@link #NO_CAPE} if it hides the cape, or null if it
     * leaves the account's cape untouched.
     */
    @Nullable
    public String getCapeId() {
        return capeId;
    }

    /**
     * @return The texture URL of the selected cape, or null if no cape is selected.
     */
    @Nullable
    public String getCapeUrl() {
        return capeUrl;
    }

    public boolean hasCape() {
        return capeId != null && !capeId.isEmpty() && capeUrl != null;
    }

    public void setCape(@Nullable String capeId, @Nullable String capeUrl) {
        this.capeId = capeId;
        this.capeUrl = capeUrl;
    }

    public void copyFrom(SkinPreset other) {
        this.name = other.name;
        this.skin = other.skin;
        this.keybindId = other.keybindId;
        this.capeId = other.capeId;
        this.capeUrl = other.capeUrl;
    }

    public SkinPreset copy() {
        return new SkinPreset(this.skin, this.name, this.keybindId, this.capeId, this.capeUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SkinPreset that = (SkinPreset) o;

        if (keybindId != that.keybindId) return false;
        if (!Objects.equals(capeId, that.capeId)) return false;
        if (!name.equals(that.name)) return false;
        return skin.equals(that.skin);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + skin.hashCode();
        result = 31 * result + keybindId;
        result = 31 * result + Objects.hashCode(capeId);
        return result;
    }
}

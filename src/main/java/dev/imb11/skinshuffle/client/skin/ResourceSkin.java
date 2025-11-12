package dev.imb11.skinshuffle.client.skin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.imb11.skinshuffle.SkinShuffle;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.ResourceLocation;

public final class ResourceSkin implements Skin {
    public static final ResourceLocation SERIALIZATION_ID = SkinShuffle.id("resource");

    public static final MapCodec<ResourceSkin> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("texture").forGetter(ResourceSkin::getTexture),
            Codec.STRING.fieldOf("model").forGetter(ResourceSkin::getModel)
    ).apply(instance, ResourceSkin::new));
    private final ClientAsset.Texture texture;
    private String model;

    public ResourceSkin(ResourceLocation texture, String model) {
        this.texture = new ClientAsset.Texture() {
            @Override
            public ResourceLocation texturePath() {
                return texture;
            }

            @Override
            public ResourceLocation id() {
                return texture;
            }
        };
        this.model = model;
    }

    @Override
    public ClientAsset.@Nullable Texture getTextureAsset() {
        return texture;
    }

    @Override
    public ResourceLocation getTexture() {
        return texture.texturePath();
    }

    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public void setModel(String value) {
        this.model = value;
    }

    @Override
    public ResourceLocation getSerializationId() {
        return SERIALIZATION_ID;
    }

    @Override
    public ConfigSkin saveToConfig() {
        var textureName = String.valueOf(Math.abs(getTexture().hashCode()));
        var configSkin = new ConfigSkin(textureName, getModel());


        var resourceManager = Minecraft.getInstance().getResourceManager();
        //? if <1.21.4 {
        /*try (ResourceTexture.TextureData data = ResourceTexture.TextureData.load(resourceManager, getTexture())) {
            var nativeImage = data.getImage();
            nativeImage.writeTo(configSkin.getFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save ResourceSkin to config.", e);
        }
        *///?} else {
        try (var resource = new SimpleTexture(getTexture())) {
            var resourceTex = resource.loadContents(resourceManager);
            var image = resourceTex.image();
            image.writeToFile(configSkin.getFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save ResourceSkin to config.", e);
        }
        //?}

        return configSkin;
    }

    public String model() {
        return model;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ResourceSkin) obj;
        return Objects.equals(this.texture, that.texture) &&
                Objects.equals(this.model, that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texture, model);
    }

    @Override
    public String toString() {
        return "ResourceSkin[" +
                "texture=" + texture + ", " +
                "model=" + model + ']';
    }

}

package dev.imb11.skinshuffle.networking;

import com.mojang.authlib.properties.Property;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SkinRefreshPayload(Property textureProperty) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SkinRefreshPayload> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("skinshuffle", "skin_refresh"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkinRefreshPayload> PACKET_CODEC = StreamCodec.composite(
            StreamCodec.ofMember(
                    (value, buf) -> {
                        buf.writeBoolean(value.hasSignature());
                        buf.writeUtf(value.name());
                        buf.writeUtf(value.value());
                        if (value.hasSignature()) {
                            buf.writeUtf(value.signature());
                        }
                    },
                    (buf) -> {
                        if (buf.readBoolean()) {
                            return new Property(buf.readUtf(), buf.readUtf(), buf.readUtf());
                        }
                        return new Property(buf.readUtf(), buf.readUtf(), null);
                    }
            ),
            SkinRefreshPayload::textureProperty,
            SkinRefreshPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
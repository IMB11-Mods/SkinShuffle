package dev.imb11.skinshuffle.networking;

import net.minecraft.core.ClientAsset;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SetCapePayload(ClientAsset.DownloadedTexture cape) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetCapePayload> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("skinshuffle", "set_cape"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetCapePayload> PACKET_CODEC = StreamCodec.composite(
            StreamCodec.ofMember(
                    (value, buf) -> {
                        buf.writeIdentifier(value.texturePath());
                        buf.writeUtf(value.url());
                    },
                    (buf) -> new ClientAsset.DownloadedTexture(buf.readIdentifier(), buf.readUtf())
            ),
            SetCapePayload::cape,
            SetCapePayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}

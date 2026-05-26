package dev.imb11.skinshuffle.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HandshakePayload() implements CustomPacketPayload {
    public static final HandshakePayload INSTANCE = new HandshakePayload();
    public static final CustomPacketPayload.Type<HandshakePayload> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("skinshuffle", "handshake"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HandshakePayload> PACKET_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}

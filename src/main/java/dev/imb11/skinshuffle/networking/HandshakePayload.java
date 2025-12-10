package dev.imb11.skinshuffle.networking;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HandshakePayload() implements CustomPacketPayload {
    public static final HandshakePayload INSTANCE = new HandshakePayload();
    public static final CustomPacketPayload.Type<HandshakePayload> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("skinshuffle", "handshake"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}

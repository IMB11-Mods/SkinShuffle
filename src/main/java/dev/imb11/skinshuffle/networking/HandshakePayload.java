package dev.imb11.skinshuffle.networking;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HandshakePayload() implements CustomPacketPayload {
    public static final HandshakePayload INSTANCE = new HandshakePayload();
    public static final CustomPacketPayload.Type<HandshakePayload> PACKET_ID = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("skinshuffle", "handshake"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}

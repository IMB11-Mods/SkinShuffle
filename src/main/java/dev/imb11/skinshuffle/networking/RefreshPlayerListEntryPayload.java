package dev.imb11.skinshuffle.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RefreshPlayerListEntryPayload(int entityID) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RefreshPlayerListEntryPayload> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("skinshuffle", "refresh_player_list_entry"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RefreshPlayerListEntryPayload> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            RefreshPlayerListEntryPayload::entityID,
            RefreshPlayerListEntryPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}

//? neoforge {
/*package dev.imb11.skinshuffle.neoforge;

import dev.imb11.skinshuffle.networking.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static dev.imb11.skinshuffle.SkinShuffle.MOD_ID;
import static dev.imb11.skinshuffle.networking.ServerSkinHandling.handleSkinRefreshPacket;

@EventBusSubscriber(modid = MOD_ID)
public class SkinShuffleNeoForge {

	@SubscribeEvent
	public static void registerPayloads(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1").optional();
		registrar.playToServer(SkinRefreshPayload.PACKET_ID, SkinRefreshPayload.PACKET_CODEC, (payload, context) -> {
			handleSkinRefreshPacket(payload, context.player().level().getServer(), (ServerPlayer) context.player());
		});
		registrar.playToClient(HandshakePayload.PACKET_ID, HandshakePayload.PACKET_CODEC, (payload, context) -> {
			ClientSkinHandling.handshakeTakenPlace = true;
		});
		registrar.playToClient(RefreshPlayerListEntryPayload.PACKET_ID, RefreshPlayerListEntryPayload.PACKET_CODEC, (payload, context) -> {
			ClientSkinHandling.receive(payload);
		});
	}

	@SubscribeEvent
	public static void registerPayloads(PlayerEvent.PlayerLoggedInEvent event) {
		ServerSkinHandling.handleHandshakePacket((ServerPlayer) event.getEntity());
	}
}
*///?}
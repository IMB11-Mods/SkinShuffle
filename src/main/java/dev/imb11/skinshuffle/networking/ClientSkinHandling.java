package dev.imb11.skinshuffle.networking;

import dev.imb11.skinshuffle.api.data.SkinQueryResult;
import dev.imb11.skinshuffle.client.config.SkinPresetManager;
import dev.imb11.skinshuffle.util.SkinShuffleClientPlayer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;

public class ClientSkinHandling {
    private static boolean handshakeTakenPlace = false;

    private static boolean reconnectRequired = false;

    public static boolean isReconnectRequired() {
        return reconnectRequired;
    }

    public static void setReconnectRequired(boolean reconnectRequired) {
        ClientSkinHandling.reconnectRequired = reconnectRequired;
    }

    public static boolean isInstalledOnServer() {
        return handshakeTakenPlace;
    }

    public static void sendRefresh(SkinQueryResult result) {
        ClientPlayNetworking.send(new SkinRefreshPayload(result.toProperty()));
    }

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> SkinPresetManager.setApiPreset(null));

        ClientPlayConnectionEvents.INIT.register(ClientSkinHandling::onPlayInit);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onPlayDisconnect());

        ClientPlayNetworking.registerGlobalReceiver(HandshakePayload.PACKET_ID, (payload, context) -> ClientSkinHandling.handshake());

        ClientPlayNetworking.registerGlobalReceiver(RefreshPlayerListEntryPayload.PACKET_ID, ClientSkinHandling::receive);
    }

    public static void receive(RefreshPlayerListEntryPayload payload, ClientPlayNetworking.Context context) {
        int id = payload.entityID();
        context.client().execute(() -> {
            ClientLevel world = context.client().level;
            if (world != null) {
                Entity entity = world.getEntity(id);
                if (entity instanceof AbstractClientPlayer player) {
                    ((SkinShuffleClientPlayer) player).skinShuffle$refreshPlayerListEntry();
                }
            }
        });
    }

    public static void onPlayInit(ClientPacketListener handler, Minecraft client) {
        if (client.level == null) return;
        handshakeTakenPlace = false;
    }


    public static void onPlayDisconnect() {
        handshakeTakenPlace = false;
        setReconnectRequired(false);
        SkinPresetManager.setApiPreset(null);
    }

	public static void handshake() {
		handshakeTakenPlace = true;
        SkinPresetManager.apply();
	}
}

package dev.imb11.skinshuffle.networking;

import dev.imb11.skinshuffle.api.data.SkinQueryResult;
import dev.imb11.skinshuffle.client.config.SkinPresetManager;
import dev.imb11.skinshuffle.util.SkinShuffleClientPlayer;
//? fabric {
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
//? neoforge
/*import net.neoforged.neoforge.client.network.ClientPacketDistributor;*/

public class ClientSkinHandling {
    public static boolean handshakeTakenPlace = false;

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
        //? fabric
        ClientPlayNetworking.send(new SkinRefreshPayload(result.toProperty()));
        //? neoforge
        /*ClientPacketDistributor.sendToServer(new SkinRefreshPayload(result.toProperty()));*/
    }

    public static void init() {
        //? fabric {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            SkinPresetManager.setApiPreset(null);
        });

        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            if (client.level == null) return;
            handshakeTakenPlace = false;
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            handshakeTakenPlace = false;
            setReconnectRequired(false);
            SkinPresetManager.setApiPreset(null);
        });

        ClientPlayNetworking.registerGlobalReceiver(HandshakePayload.PACKET_ID, (payload, context) -> {
            handshakeTakenPlace = true;
        });

        ClientPlayNetworking.registerGlobalReceiver(RefreshPlayerListEntryPayload.PACKET_ID, ClientSkinHandling::receive);
        //?}
    }

    public static void receive(RefreshPlayerListEntryPayload payload
            //? fabric
            , ClientPlayNetworking.Context context
    ) {
        int id = payload.entityID();
        //? fabric
        Minecraft client = context.client();
        //? neoforge
        /*Minecraft client = Minecraft.getInstance();*/
        client.execute(() -> {
            ClientLevel world = client.level;
            if (world != null) {
                Entity entity = world.getEntity(id);
                if (entity instanceof AbstractClientPlayer player) {
                    ((SkinShuffleClientPlayer) player).skinShuffle$refreshPlayerListEntry();
                }
            }
        });
    }
}

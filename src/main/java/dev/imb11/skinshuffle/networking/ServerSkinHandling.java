package dev.imb11.skinshuffle.networking;

import com.google.common.collect.HashMultimap;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import dev.imb11.skinshuffle.Platform;
import dev.imb11.skinshuffle.SkinShuffle;
import dev.imb11.skinshuffle.mixin.accessor.GameProfileAccessor;
import dev.imb11.skinshuffle.util.SkinShufflePlayer;
//? fabric {
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//?}
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ServerSkinHandling {

    private static void handleSkinRefresh(MinecraftServer server, ServerPlayer player, Property skinData) {
        SkinShuffle.LOGGER.info("Recieved skin refresh packet from: " + player.getName().getString());

        server.execute(() -> {
            var properties = HashMultimap.create(player.getGameProfile().properties());
            try {
                properties.removeAll("textures");
            } catch (Exception ignored) {
            }

            try {
                properties.put("textures", skinData);
            } catch (Error e) {
                SkinShuffle.LOGGER.error("Failed to refresh GameProfile for " + player.getName() + "\n" + e.getMessage());
            }

            var access = (GameProfileAccessor) (Object) player.getGameProfile();
            access.setProperties(new PropertyMap(properties));

            SkinShufflePlayer skinShufflePlayer = (SkinShufflePlayer) player;
            skinShufflePlayer.skinShuffle$refreshSkin();
        });
    }

    /**
     * Attempt to refresh the player list entry for a player.
     *
     * @param player   The player to refresh the entry for.
     * @param entityID The entity ID of the player.
     * @return Whether the refresh was successful.
     */
    public static boolean attemptPlayerListEntryRefresh(ServerPlayer player, int entityID) {
        if (Platform.canSend(player, RefreshPlayerListEntryPayload.PACKET_ID)) {
            Platform.send(player, new RefreshPlayerListEntryPayload(entityID));
            return true;
        }
        return false;
    }

    public static void init() {
        // Send handshake packet to client.
        //? fabric {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            handleHandshakePacket(handler.getPlayer());
        });

        ServerPlayNetworking.registerGlobalReceiver(SkinRefreshPayload.PACKET_ID, (payload, context) -> handleSkinRefreshPacket(payload, context.server(), context.player()));
        //?}
    }

    public static void handleSkinRefreshPacket(SkinRefreshPayload payload, MinecraftServer context, ServerPlayer player) {
        try {
            ServerSkinHandling.handleSkinRefresh(context, player, payload.textureProperty());
        } catch (Exception e) {
            SkinShuffle.LOGGER.error("Failed to handle skin refresh packet from " + player.getName().getString() + "\n" + e.getMessage());
        }
    }

    public static void handleHandshakePacket(ServerPlayer player) {
        if (Platform.canSend(player, HandshakePayload.PACKET_ID)) {
            Platform.send(player, new HandshakePayload());
        }
    }
}

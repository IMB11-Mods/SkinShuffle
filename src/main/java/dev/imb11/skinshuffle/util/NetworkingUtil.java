package dev.imb11.skinshuffle.util;

import com.mojang.authlib.yggdrasil.YggdrasilUserApiService;
import dev.imb11.skinshuffle.client.config.SkinPresetManager;
import dev.imb11.skinshuffle.mixin.accessor.MinecraftClientAccessor;
import dev.imb11.skinshuffle.networking.ClientSkinHandling;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.quickplay.QuickPlay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;

public class NetworkingUtil {
    public static boolean isLoggedIn() {
        Minecraft client = Minecraft.getInstance();
        return ((MinecraftClientAccessor) client).getUserApiService() instanceof YggdrasilUserApiService;
    }

    public static void handleReconnect(Minecraft client) {
        ClientSkinHandling.setReconnectRequired(false);
        SkinPresetManager.setApiPreset(null);

        boolean isSingleplayer = client.isLocalServer();
        String folderName, serverAddress;

        if (isSingleplayer) {
            serverAddress = null;
            folderName = client.getSingleplayerServer().getWorldPath(LevelResource.ROOT).toFile().getName();
//            client.world.disconnect(Text.literal("Rejoining world."));
            client.disconnect(new GenericMessageScreen(Component.translatable("skinshuffle.reconnect.rejoining")), false);
        } else {
            folderName = null;
            if (!client.getConnection().getConnection().isMemoryConnection()) {
                serverAddress = client.getConnection().getServerData().ip;
            } else {
                serverAddress = null;
            }
//            client.world.disconnect(Text.literal("Rejoining world."));
            client.disconnect(new GenericMessageScreen(Component.translatable("skinshuffle.reconnect.reconnecting")), false);
        }


        if (client.isLocalServer()) {
            client.doRunTask(() -> {
                try {
                    Thread.sleep(250);
                    client.execute(() -> QuickPlay.joinSingleplayerWorld(client, folderName));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            client.doRunTask(() -> {
                try {
                    Thread.sleep(250);
                    client.execute(() -> QuickPlay.joinMultiplayerWorld(client, serverAddress));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}

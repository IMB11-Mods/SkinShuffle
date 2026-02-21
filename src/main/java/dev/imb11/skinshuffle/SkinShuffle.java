package dev.imb11.skinshuffle;

import com.mojang.authlib.GameProfile;
import dev.imb11.skinshuffle.api.MojangSkinAPI;
import dev.imb11.skinshuffle.compat.api.CompatLoader;
import dev.imb11.skinshuffle.networking.HandshakePayload;
import dev.imb11.skinshuffle.networking.RefreshPlayerListEntryPayload;
import dev.imb11.skinshuffle.networking.ServerSkinHandling;
import dev.imb11.skinshuffle.networking.SkinRefreshPayload;
import dev.imb11.skinshuffle.util.SkinCacheRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkinShuffle implements ModInitializer {
    public static final String MOD_ID = "skinshuffle";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Path DATA_DIR = FabricLoader.getInstance().getConfigDir().resolve("skinshuffle");

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(
                SkinRefreshPayload.PACKET_ID,
                SkinRefreshPayload.PACKET_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
                HandshakePayload.PACKET_ID,
                StreamCodec.unit(HandshakePayload.INSTANCE)
        );
        PayloadTypeRegistry.clientboundPlay().register(
                RefreshPlayerListEntryPayload.PACKET_ID,
                RefreshPlayerListEntryPayload.PACKET_CODEC
        );

        ensureDataDir();
        SkinCacheRegistry.initialize();
        ServerSkinHandling.init();
        CompatLoader.init();

        MixinStatics.INITIAL_SKIN_TEXTURES = CompletableFuture.supplyAsync(this::getInitialSkinTextures);
    }

    private CompletableFuture<Optional<PlayerSkin>> getInitialSkinTextures() {
        while (Minecraft.getInstance() == null) {
            Thread.onSpinWait();
        }
        while (Minecraft.getInstance().getSkinManager() == null) {
            Thread.onSpinWait();
        }

        Minecraft client = Minecraft.getInstance();

        return client.getSkinManager().get(client.getGameProfile());
    }

    private void ensureDataDir() {
        if (!DATA_DIR.toFile().exists()) {
            try {
                Files.createDirectories(DATA_DIR);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create " + DATA_DIR, e);
            }
        }
    }
}

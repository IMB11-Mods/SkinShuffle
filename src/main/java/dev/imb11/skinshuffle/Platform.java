package dev.imb11.skinshuffle;

import dev.imb11.skinshuffle.networking.RefreshPlayerListEntryPayload;
//? fabric {
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
//?}
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
//? neoforge {
/*import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.network.PacketDistributor;
*///?}

import java.nio.file.Path;

public class Platform {
	public static Path getConfigDir() {
		//? fabric
		return FabricLoader.getInstance().getConfigDir();
		//? neoforge
		/*return FMLPaths.CONFIGDIR.get();*/
	}

	public static boolean isModLoaded(String id) {
		//? fabric
		return FabricLoader.getInstance().isModLoaded(id);
		//? neoforge
		/*return FMLLoader.getCurrent().getLoadingModList().getModFileById(id) != null;*/
	}

	public static boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> packetId) {
		//? fabric
		return ServerPlayNetworking.canSend(player, packetId);
		//? neoforge
		/*return player.connection.hasChannel(packetId);*/
	}

	public static void send(ServerPlayer player, CustomPacketPayload payload) {
		//? fabric
		ServerPlayNetworking.send(player, payload);
		//? neoforge
		/*PacketDistributor.sendToPlayer(player, payload);*/
	}
}

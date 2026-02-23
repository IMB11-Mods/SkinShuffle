//? neoforge {
/*package dev.imb11.skinshuffle.neoforge;

import dev.imb11.skinshuffle.client.SkinShuffleClient;
import dev.imb11.skinshuffle.client.config.SkinPresetManager;
import dev.imb11.skinshuffle.client.config.SkinShuffleConfig;
import dev.imb11.skinshuffle.client.util.KeybindManager;
import dev.imb11.skinshuffle.networking.ClientSkinHandling;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import static dev.imb11.skinshuffle.SkinShuffle.MOD_ID;
import static dev.imb11.skinshuffle.networking.ClientSkinHandling.handshakeTakenPlace;
import static dev.imb11.skinshuffle.networking.ClientSkinHandling.setReconnectRequired;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class SkinShuffleNeoForgeClient {

	@SubscribeEvent
	public static void registerKeybinds(RegisterKeyMappingsEvent event) {
		KeybindManager.init();
		for (KeyMapping presetKeybinding : KeybindManager.presetKeybindings) {
			event.register(presetKeybinding);
		}
	}

	@SubscribeEvent
	public static void clientTick(ClientTickEvent.Post event) {
		KeybindManager.onEndTick(Minecraft.getInstance());
	}

	@SubscribeEvent
	public static void clientPlayConnectionEventsJoinAndInit(ClientPlayerNetworkEvent.LoggingIn event) {
		SkinPresetManager.setApiPreset(null);
		if (event.getPlayer().level() == null) return;
		handshakeTakenPlace = false;
	}

	@SubscribeEvent
	public static void clientPlayConnectionEventsDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
		handshakeTakenPlace = false;
		setReconnectRequired(false);
		SkinPresetManager.setApiPreset(null);
	}
}
*///?}
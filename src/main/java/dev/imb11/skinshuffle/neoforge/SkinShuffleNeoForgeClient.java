//? neoforge {
/*package dev.imb11.skinshuffle.neoforge;

import dev.imb11.skinshuffle.client.config.SkinPresetManager;
import dev.imb11.skinshuffle.client.gui.GeneratedScreens;
import dev.imb11.skinshuffle.client.util.KeybindManager;
import dev.imb11.skinshuffle.networking.ClientSkinHandling;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import static dev.imb11.skinshuffle.SkinShuffle.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class SkinShuffleNeoForgeClient {

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (container1, modListScreen) -> GeneratedScreens.getCarouselScreen(modListScreen));
	}

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
		ClientSkinHandling.onPlayInit();
		SkinPresetManager.setApiPreset(null);
	}

	@SubscribeEvent
	public static void clientPlayConnectionEventsDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
		ClientSkinHandling.onPlayDisconnect();
	}
}
*///?}
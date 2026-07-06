package dev.imb11.skinshuffle.fabric;

import dev.imb11.skinshuffle.client.util.KeybindManager;
//? fabric {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//?}
import net.minecraft.client.KeyMapping;

public class SkinShuffleFabric {
	public static void onEndTick() {
		//? fabric
		ClientTickEvents.END_CLIENT_TICK.register(KeybindManager::onEndTick);
	}

	public static KeyMapping registerKeyMapping(KeyMapping keyMapping) {
		//? fabric {
		return KeyMappingHelper.registerKeyMapping(keyMapping);
		//?} else {
		/*return keyMapping;
		*///?}

	}
}

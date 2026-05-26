package dev.imb11.skinshuffle.client.util;

import com.mojang.blaze3d.platform.InputConstants;
import dev.imb11.skinshuffle.SkinShuffle;
import dev.imb11.skinshuffle.client.config.SkinPresetManager;
import dev.imb11.skinshuffle.client.config.SkinShuffleConfig;
import dev.imb11.skinshuffle.client.gui.GeneratedScreens;
import dev.imb11.skinshuffle.networking.ClientSkinHandling;
//? fabric {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//?}
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.lwjgl.glfw.GLFW;

/**
 * Manages keybinds for quickly switching between skin presets.
 */
public class KeybindManager {

    private static final int MAX_KEYBIND_COUNT = 9;
    private static final String TRANSLATION_KEY_PREFIX = "key.skinshuffle.preset_";
    private static final KeyMapping.Category KEYBIND_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("skinshuffle", "presets"));

    public static KeyMapping[] presetKeybindings;

    /**
     * Initialize all keybindings for skin presets.
     * This should be called during mod initialization.
     */
    public static void init() {
        presetKeybindings = new KeyMapping[MAX_KEYBIND_COUNT];

        // Register keybindings for each preset slot (1-9)
        for (int i = 0; i < MAX_KEYBIND_COUNT; i++) {
            final int presetId = i + 1;

            // Create unbound keybinds for each preset slot
            KeyMapping keyMapping = new KeyMapping(
                    TRANSLATION_KEY_PREFIX + presetId,
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_UNKNOWN, // Initially unbound
                    KEYBIND_CATEGORY
            );

            //? neoforge
            /*presetKeybindings[i] = keyMapping;*/

            //? fabric
            presetKeybindings[i] = KeyMappingHelper.registerKeyMapping(keyMapping);
        }

        // Register the tick event for checking keybinds
        //? fabric
        ClientTickEvents.END_CLIENT_TICK.register(KeybindManager::onEndTick);
    }

    /**
     * Check if any keybinds are pressed and handle them.
     *
     * @param client The Minecraft client instance
     */
    private static void checkKeybindings(Minecraft client) {
        for (int i = 0; i < presetKeybindings.length; i++) {
            if (presetKeybindings[i].consumeClick()) {
                int presetId = i + 1;
                applyPreset(presetId, client);
            }
        }
    }

    /**
     * Apply a preset by its ID and handle reconnect behavior if necessary.
     *
     * @param presetId The preset ID to apply (1-9)
     * @param client   The Minecraft client instance
     */
    private static void applyPreset(int presetId, Minecraft client) {
        if (SkinPresetManager.getChosenPreset().getKeybindId() == presetId) return;
        boolean success = SkinPresetManager.applyPresetByKeybindId(presetId);

        if (success) {
            SkinShuffle.LOGGER.info("Applied skin preset with keybind ID: " + presetId);

            // If the mod is not installed on server, prompt for reconnect
            if (client.level != null && !SkinShuffleConfig.get().disableAPIUpload && !ClientSkinHandling.isInstalledOnServer()) {
                client.setScreen(GeneratedScreens.getReconnectScreen(client.screen));
            } else {
                if (SkinShuffleConfig.get().playKeybindSoundEffect) {
                    if (client.player != null)
                        client.player.playSound(SoundEvents.UI_TOAST_IN, 0.46f, 2f);
                }
            }
        }
    }

    public static void onEndTick(Minecraft client) {
        if (client.player != null) {
            checkKeybindings(client);
        }
    }
}

package dev.imb11.skinshuffle.client;

import dev.imb11.skinshuffle.client.config.SkinPresetManager;
import dev.imb11.skinshuffle.client.config.SkinShuffleConfig;
import dev.imb11.skinshuffle.client.util.KeybindManager;
import dev.imb11.skinshuffle.networking.ClientSkinHandling;
import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.entrypoint.client.ClientModInitializer;

public class SkinShuffleClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(ModContainer mod) {
        SkinShuffleConfig.load();

        SkinPresetManager.setup();
        SkinPresetManager.loadPresets();
        KeybindManager.init();

        ClientSkinHandling.init();
    }

    public static float TOTAL_TICK_DELTA = 0;
}

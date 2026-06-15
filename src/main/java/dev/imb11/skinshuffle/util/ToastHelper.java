package dev.imb11.skinshuffle.util;

import dev.imb11.skinshuffle.client.config.SkinShuffleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

public class ToastHelper {
    public static void showToast(String id) {
        var client = Minecraft.getInstance();
        client.gui.toastManager().addToast(new SystemToast(
                SystemToast.SystemToastId.PACK_LOAD_FAILURE,
                Component.translatable(id + ".title"),
                Component.translatable(id + ".message")));
    }

    public static void showRefusedReconnectToast() {
        if (!SkinShuffleConfig.get().disableReconnectToast)
            showToast("skinshuffle.toasts.refused_reconnect");
    }

    public static void showOfflineModeToast() {
        if (!SkinShuffleConfig.get().disableOfflineToast)
            showToast("skinshuffle.toasts.offline");
    }

    public static void showEditorFailToast() {
        showToast("skinshuffle.toasts.editor_failure");
    }
}

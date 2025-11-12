package dev.imb11.skinshuffle.client.gui;

import dev.imb11.skinshuffle.client.config.SkinShuffleConfig;
import dev.imb11.skinshuffle.client.gui.widgets.OpenCarouselButton;
import dev.imb11.skinshuffle.client.gui.widgets.buttons.WarningIndicatorButton;
import dev.imb11.skinshuffle.mixin.accessor.GameMenuScreenAccessor;
import dev.imb11.skinshuffle.networking.ClientSkinHandling;
import dev.imb11.skinshuffle.util.NetworkingUtil;
import dev.imb11.skinshuffle.util.ToastHelper;
import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class GeneratedScreens {
    public static Screen getConfigScreen(Screen parent) {
        return SkinShuffleConfig.getInstance().generateScreen(parent);
    }

    public static ArrayList<AbstractWidget> createCarouselWidgets(Screen screen) {
        ArrayList<AbstractWidget> widgets = new ArrayList<>();
        int y = (screen.height / 4 + 48) + 84;
        int x = screen.width / 2 + 104 + 25;

        if (screen instanceof PauseScreen gameMenuScreen) {
            if (!gameMenuScreen.showsPauseMenu())
                return new ArrayList<>();

            if (!SkinShuffleConfig.get().displayInPauseMenu) return widgets;
            y = ((GameMenuScreenAccessor) gameMenuScreen).getDisconnectButton().getY();
            x -= 25 / 2;

            widgets.add(new WarningIndicatorButton(x + 72, y, gameMenuScreen));
        }

        widgets.add(new OpenCarouselButton(x, y, 72, 20));

        return widgets;
    }

    public static Screen getReconnectScreen(Screen target) {
        Minecraft client = Minecraft.getInstance();
        return new ConfirmScreen((boolean result) -> {
            if (result) {
                NetworkingUtil.handleReconnect(client);
            } else {
                if (!ClientSkinHandling.isInstalledOnServer()) {
                    ToastHelper.showRefusedReconnectToast();
                    ClientSkinHandling.setReconnectRequired(true);
                }

                client.setScreen(target);
            }
        }, Component.translatable("skinshuffle.reconnect.title",
                client.isLocalServer() ? I18n.get("skinshuffle.reconnect.c_rejoin") : I18n.get("skinshuffle.reconnect.c_reconnect")).withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                Component.translatable("skinshuffle.reconnect.message",
                        client.isLocalServer() ? I18n.get("skinshuffle.reconnect.rejoin") : I18n.get("skinshuffle.reconnect.reconnect_to"),
                        client.isLocalServer() ? I18n.get("skinshuffle.reconnect.world") : I18n.get("skinshuffle.reconnect.server"),
                        client.isLocalServer() ? I18n.get("skinshuffle.reconnect.rejoin") : I18n.get("skinshuffle.reconnect.reconnect")));
    }

    public static Screen getCarouselScreen(Screen parent) {
        var factoryValue = SkinShuffleConfig.get().carouselView.factory.apply(parent);

        if (!SkinShuffleConfig.get().welcomeGuideShown) {
            SkinShuffleConfig.get().welcomeGuideShown = true;
            SkinShuffleConfig.save();
            return new WelcomeGuideScreen(factoryValue);
        }

        return factoryValue;
    }
}

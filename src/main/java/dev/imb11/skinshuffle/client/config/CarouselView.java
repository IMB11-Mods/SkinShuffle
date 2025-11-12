package dev.imb11.skinshuffle.client.config;

import dev.imb11.skinshuffle.SkinShuffle;
import dev.imb11.skinshuffle.client.gui.carousels.CarouselScreen;
import dev.imb11.skinshuffle.client.gui.carousels.CompactCarouselScreen;
import dev.imb11.skinshuffle.client.gui.carousels.LargeCarouselScreen;
import java.util.function.Function;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public enum CarouselView {
    LARGE(LargeCarouselScreen::new, SkinShuffle.id("textures/gui/large-view-button.png"),
            Component.translatable("skinshuffle.carousel.view_type_button.large.tooltip")),
    COMPACT(CompactCarouselScreen::new, SkinShuffle.id("textures/gui/compact-view-button.png"),
            Component.translatable("skinshuffle.carousel.view_type_button.compact.tooltip"));

    public final Function<Screen, ? extends CarouselScreen> factory;
    public final ResourceLocation iconTexture;
    public final Component tooltip;

    CarouselView(Function<Screen, ? extends CarouselScreen> factory, ResourceLocation iconTexture, Component tooltip) {
        this.factory = factory;
        this.iconTexture = iconTexture;
        this.tooltip = tooltip;
    }
}



package dev.imb11.skinshuffle.client.config;

import dev.imb11.skinshuffle.SkinShuffle;
import dev.imb11.skinshuffle.client.gui.CarouselScreen;
import dev.imb11.skinshuffle.client.gui.CompactCarouselScreen;
import dev.imb11.skinshuffle.client.gui.LargeCarouselScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public enum CarouselView {
    LARGE(LargeCarouselScreen::new, SkinShuffle.id("textures/gui/large-view-button.png"),
            Text.translatable("skinshuffle.carousel.view_type_button.large.tooltip")),
    COMPACT(CompactCarouselScreen::new, SkinShuffle.id("textures/gui/compact-view-button.png"),
            Text.translatable("skinshuffle.carousel.view_type_button.compact.tooltip"));

    public final Function<Screen, ? extends CarouselScreen> factory;
    public final Identifier iconTexture;
    public final Text tooltip;

    CarouselView(Function<Screen, ? extends CarouselScreen> factory, Identifier iconTexture, Text tooltip) {
        this.factory = factory;
        this.iconTexture = iconTexture;
        this.tooltip = tooltip;
    }
}

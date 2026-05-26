package dev.imb11.skinshuffle.client.gui.components;

import dev.imb11.skinshuffle.client.preset.SkinPreset;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.network.chat.Component;

/**
 * Base class for tab components in the preset editor.
 */
public abstract class TabComponent extends GridLayoutTab {
    protected final Font textRenderer;
    protected final SkinPreset preset;

    /**
     * Constructor for a tab component.
     *
     * @param title        The title of this tab
     * @param textRenderer The text renderer
     * @param preset       The skin preset being edited
     */
    public TabComponent(Component title, Font textRenderer, SkinPreset preset) {
        super(title);
        this.textRenderer = textRenderer;
        this.preset = preset;
    }

    /**
     * Called when the tab is initialized.
     *
     * @param width       Screen width
     * @param height      Screen height
     * @param sideMargins Side margins for positioning
     */
    public abstract void initialize(int width, int height, int sideMargins);
}
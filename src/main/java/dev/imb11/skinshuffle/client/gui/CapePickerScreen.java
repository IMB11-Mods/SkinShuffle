package dev.imb11.skinshuffle.client.gui;

import dev.imb11.skinshuffle.api.data.OwnedCape;
import dev.imb11.skinshuffle.client.preset.SkinPreset;
import dev.imb11.skinshuffle.client.skin.CapeManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of the capes the current account owns, shown with a small thumbnail of each, for choosing the cape
 * a preset uses. The first two entries leave the account's cape alone or hide it.
 */
public class CapePickerScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int ROW_WIDTH = 200;
    private static final int THUMB_WIDTH = 10;
    private static final int THUMB_HEIGHT = 16;
    private static final int LIST_TOP = 36;
    private static final int BOTTOM_BAR_HEIGHT = 36;

    private final Screen parent;
    private final SkinPreset preset;
    private final List<Option> options = new ArrayList<>();
    private int page = 0;

    /**
     * One row of the list. A null id means "leave the account's cape alone", {@link SkinPreset#NO_CAPE} means
     * "hide the cape".
     */
    private record Option(@Nullable String id, @Nullable String url, Component label) {
    }

    public CapePickerScreen(Screen parent, SkinPreset preset) {
        super(Component.translatable("skinshuffle.cape_picker.title"));
        this.parent = parent;
        this.preset = preset;
    }

    @Override
    protected void init() {
        this.options.clear();
        this.options.add(new Option(null, null, Component.translatable("skinshuffle.edit.customize.cape.unchanged")));
        this.options.add(new Option(SkinPreset.NO_CAPE, null, Component.translatable("skinshuffle.edit.customize.cape.none")));
        for (OwnedCape cape : CapeManager.getOwnedCapes()) {
            this.options.add(new Option(cape.id(), cape.url(), Component.literal(cape.alias())));
        }

        // Owned capes come from the Mojang API, so they might not be available yet. Rebuild once they are.
        if (!CapeManager.isLoaded()) {
            CapeManager.loadOwnedCapes(false, () -> {
                if (this.minecraft != null && this.minecraft.gui.screen() == this) {
                    this.rebuildWidgets();
                }
            });
        }

        int perPage = perPage();
        int pageCount = Math.max(1, (this.options.size() + perPage - 1) / perPage);
        this.page = Math.min(this.page, pageCount - 1);

        int rowX = this.width / 2 - ROW_WIDTH / 2;
        int first = this.page * perPage;
        int last = Math.min(this.options.size(), first + perPage);
        for (int i = first; i < last; i++) {
            Option option = this.options.get(i);
            int y = LIST_TOP + (i - first) * ROW_HEIGHT;
            boolean selected = isSelected(option);

            Component label = selected
                    ? Component.empty().append(Component.literal("✔ ")).append(option.label())
                    : option.label();

            this.addRenderableWidget(Button.builder(label, button -> choose(option))
                    .bounds(rowX + THUMB_WIDTH + 6, y, ROW_WIDTH - THUMB_WIDTH - 6, ROW_HEIGHT - 4)
                    .build());
        }

        int barY = this.height - BOTTOM_BAR_HEIGHT + 8;
        Button previous = this.addRenderableWidget(Button.builder(Component.literal("<"), button -> changePage(-1))
                .bounds(this.width / 2 - ROW_WIDTH / 2, barY, 20, 20).build());
        Button next = this.addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(1))
                .bounds(this.width / 2 + ROW_WIDTH / 2 - 20, barY, 20, 20).build());
        previous.active = this.page > 0;
        next.active = this.page < pageCount - 1;

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 50, barY, 100, 20).build());
    }

    private int perPage() {
        return Math.max(1, (this.height - LIST_TOP - BOTTOM_BAR_HEIGHT) / ROW_HEIGHT);
    }

    private boolean isSelected(Option option) {
        String current = this.preset.getCapeId();
        return current == null ? option.id() == null : current.equals(option.id());
    }

    private void choose(Option option) {
        this.preset.setCape(option.id(), option.url());
        this.onClose();
    }

    private void changePage(int delta) {
        this.page += delta;
        this.rebuildWidgets();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.centeredText(this.font, this.title, this.width / 2, 14, 0xFFFFFFFF);

        int perPage = perPage();
        int first = this.page * perPage;
        int last = Math.min(this.options.size(), first + perPage);
        int rowX = this.width / 2 - ROW_WIDTH / 2;

        for (int i = first; i < last; i++) {
            Option option = this.options.get(i);
            ClientAsset.Texture texture = CapeManager.getTexture(option.url());
            if (texture == null) continue;

            int y = LIST_TOP + (i - first) * ROW_HEIGHT + (ROW_HEIGHT - 4 - THUMB_HEIGHT) / 2;
            // The front of a cape starts at (1, 1) in the standard 64x32 texture.
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture.texturePath(), rowX, y, 1, 1,
                    THUMB_WIDTH, THUMB_HEIGHT, 64, 32);
        }

        if (CapeManager.isLoading() || !CapeManager.isLoaded()) {
            graphics.centeredText(this.font, Component.translatable("skinshuffle.cape_picker.loading"),
                    this.width / 2, this.height - BOTTOM_BAR_HEIGHT - 12, 0xFFAAAAAA);
        } else if (CapeManager.getOwnedCapes().isEmpty()) {
            graphics.centeredText(this.font, Component.translatable("skinshuffle.cape_picker.none_found"),
                    this.width / 2, this.height - BOTTOM_BAR_HEIGHT - 12, 0xFFAAAAAA);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}

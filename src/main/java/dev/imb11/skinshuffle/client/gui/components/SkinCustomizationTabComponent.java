package dev.imb11.skinshuffle.client.gui.components;

import dev.imb11.skinshuffle.client.config.SkinPresetManager;
import dev.imb11.skinshuffle.client.preset.SkinPreset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

/**
 * Tab component for customizing skin preset properties.
 */
public class SkinCustomizationTabComponent extends TabComponent {

    private static final int MAX_KEYBIND_ID = 9;

    /**
     * Constructor for the skin customization tab component.
     */
    public SkinCustomizationTabComponent(Font textRenderer, SkinPreset preset) {
        super(Component.translatable("skinshuffle.edit.customize.title"), textRenderer, preset);
    }

    @Override
    public void initialize(int width, int height, int sideMargins) {
        this.layout.defaultCellSetting().paddingLeft(width / 3).alignHorizontallyCenter();
        var gridAdder = this.layout.rowSpacing(8).createRowHelper(1);

        // Add preset name field
        var presetNameField = new EditBox(textRenderer, 0, 0, 256, 20, Component.empty());
        presetNameField.setValue(preset.getName());
        presetNameField.setResponder(preset::setName);
        presetNameField.setMaxLength(2048);

        gridAdder.addChild(new StringWidget(Component.translatable("skinshuffle.edit.customize.preset_name"), textRenderer));
        gridAdder.addChild(presetNameField);

        // Add keybind ID selector
        gridAdder.addChild(new StringWidget(Component.translatable("skinshuffle.edit.customize.keybind_id"), textRenderer));

        // Create list of available keybind IDs (1-9 and None/-1)
        List<Integer> availableKeybindIds = new ArrayList<>();
        availableKeybindIds.add(-1); // None

        // Get all keybind IDs that are already in use (except by this preset)
        List<Integer> usedKeybindIds = SkinPresetManager.getLoadedPresets().stream()
                .filter(p -> p != preset && p.getKeybindId() >= 0)
                .map(SkinPreset::getKeybindId)
                .toList();

        // Add all unused keybind IDs
        IntStream.rangeClosed(1, MAX_KEYBIND_ID)
                .filter(id -> !usedKeybindIds.contains(id))
                .forEach(availableKeybindIds::add);

        // Always add currently assigned ID if it exists
        if (preset.getKeybindId() > 0 && !availableKeybindIds.contains(preset.getKeybindId())) {
            availableKeybindIds.add(preset.getKeybindId());
        }

        // Sort the IDs for nicer display
        availableKeybindIds.sort(Integer::compare);

        // Find the starting index for the current keybind ID
        int currentIndex = availableKeybindIds.indexOf(preset.getKeybindId());
        if (currentIndex < 0) currentIndex = 0; // Fallback to "None" if not found

        // Create the cycling button
        var keybindIdButton = CycleButton.builder(this::formatKeybindIdText, (Supplier<Integer>) ()->-1).withValues(availableKeybindIds).create(0, 0, 100, 30, Component.translatable("skinshuffle.edit.customize.keybind_id_prefix"), (button, value) -> {  // on value change
            preset.setKeybindId(value);
        });
        gridAdder.addChild(keybindIdButton);
    }

    /**
     * Format the keybind ID for display in the cycling button.
     *
     * @param keybindId The keybind ID to format
     * @return Formatted text for the given keybind ID
     */
    private Component formatKeybindIdText(int keybindId) {
        return keybindId < 0 ?
                Component.translatable("skinshuffle.edit.customize.keybind_id.none") :
                Component.nullToEmpty(String.valueOf(keybindId));
    }
}
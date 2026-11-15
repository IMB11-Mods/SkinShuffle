package dev.imb11.skinshuffle.client.gui.components;

import dev.imb11.skinshuffle.client.config.SkinPresetManager;
import dev.imb11.skinshuffle.client.preset.SkinPreset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import dev.imb11.skinshuffle.client.util.KeybindManager;
import dev.imb11.skinshuffle.api.data.OwnedCape;
import dev.imb11.skinshuffle.client.skin.CapeManager;
import dev.imb11.skinshuffle.client.gui.CapePickerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

/**
 * Tab component for customizing skin preset properties.
 */
public class SkinCustomizationTabComponent extends TabComponent {

    private static final int MAX_KEYBIND_ID = 9;

    private Button capeButton;

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
        var keybindIdButton = CycleButton.builder(this::formatKeybindIdText, (Supplier<Integer>) ()->-1).withValues(availableKeybindIds).create(0, 0, 256, 20, Component.translatable("skinshuffle.edit.customize.keybind_id_prefix"), (button, value) -> {  // on value change
            preset.setKeybindId(value);
        });
        gridAdder.addChild(keybindIdButton);

        // Add cape selector
        gridAdder.addChild(new StringWidget(Component.translatable("skinshuffle.edit.customize.cape"), textRenderer));
        this.capeButton = Button.builder(Component.empty(), button -> openCapePicker()).width(256).build();
        gridAdder.addChild(this.capeButton);
        updateCapeButton();

        // Owned capes come from the Mojang API, so they might not be available yet.
        CapeManager.loadOwnedCapes(false, this::updateCapeButton);
    }

    private int currentCapeChoice() {
        String capeId = preset.getCapeId();
        if (capeId == null) return 0;
        if (capeId.equals(SkinPreset.NO_CAPE)) return 1;

        List<OwnedCape> owned = CapeManager.getOwnedCapes();
        for (int i = 0; i < owned.size(); i++) {
            if (owned.get(i).id().equals(capeId)) return i + 2;
        }
        return 0;
    }

    private void openCapePicker() {
        Minecraft client = Minecraft.getInstance();
        client.setScreen(new CapePickerScreen(client.screen, preset));
    }

    private void updateCapeButton() {
        if (this.capeButton == null) return;

        int choice = currentCapeChoice();
        Component value;
        if (choice == 0) {
            value = Component.translatable("skinshuffle.edit.customize.cape.unchanged");
        } else if (choice == 1) {
            value = Component.translatable("skinshuffle.edit.customize.cape.none");
        } else {
            value = Component.literal(CapeManager.getOwnedCapes().get(choice - 2).alias());
        }

        Component prefix = Component.translatable("skinshuffle.edit.customize.cape_prefix");
        this.capeButton.setMessage(Component.empty().append(prefix).append(value));
    }

    /**
     * Format the keybind ID for display in the cycling button.
     *
     * @param keybindId The keybind ID to format
     * @return Formatted text for the given keybind ID
     */
    private Component formatKeybindIdText(int keybindId) {
		if (keybindId < 0) return Component.translatable("skinshuffle.edit.customize.keybind_id.none");
	    return Component.translatable("skinshuffle.edit.customize.keybind_id.bound", String.valueOf(keybindId), Component.keybind(KeybindManager.presetKeybindings[keybindId-1].getName()));
	}
}
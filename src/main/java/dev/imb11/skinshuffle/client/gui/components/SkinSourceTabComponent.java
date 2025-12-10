package dev.imb11.skinshuffle.client.gui.components;

import dev.imb11.skinshuffle.SkinShuffle;
import dev.imb11.skinshuffle.client.gui.widgets.buttons.IconButtonWidget;
import dev.imb11.skinshuffle.client.preset.SkinPreset;
import dev.imb11.skinshuffle.client.util.SkinLoader;
import dev.imb11.skinshuffle.client.util.ValidationUtils;
import dev.imb11.skinshuffle.util.ToastHelper;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.network.chat.Component;

/**
 * Tab component for selecting and configuring skin sources.
 */
public class SkinSourceTabComponent extends TabComponent {
    private final Minecraft client;
    private final Consumer<Boolean> onValidationChanged;

    private EditBox textFieldWidget;
    private MultiLineTextWidget errorLabel;
    private CycleButton<String> skinModelButton;
    private IconButtonWidget loadButton;
    private SkinLoader.SourceType currentSourceType;
    private boolean loading = false;

    /**
     * Constructor for the skin source tab component.
     *
     * @param textRenderer        The text renderer
     * @param preset              The skin preset being edited
     * @param client              The Minecraft client instance
     * @param onValidationChanged Callback when validation status changes
     */
    public SkinSourceTabComponent(Font textRenderer, SkinPreset preset,
                                  Minecraft client, Consumer<Boolean> onValidationChanged) {
        super(Component.translatable("skinshuffle.edit.source.title"), textRenderer, preset);
        this.client = client;
        this.onValidationChanged = onValidationChanged;
        this.currentSourceType = SkinLoader.SourceType.UNCHANGED;
    }

    @Override
    public void initialize(int width, int height, int sideMargins) {
        this.layout.defaultCellSetting().paddingLeft(width / 3).paddingRight(sideMargins).alignHorizontallyCenter();
        var gridAdder = this.layout.rowSpacing(4).createRowHelper(1);

        // Create text field for source input
        this.textFieldWidget = new EditBox(textRenderer, 0, 0, 230, 20, Component.empty());
        this.textFieldWidget.setMaxLength(2048);
        this.textFieldWidget.setResponder(str -> validateInput());

        // Create error label
        this.errorLabel = new MultiLineTextWidget(0, 0, Component.empty(), textRenderer) {
            @Override
            public int getHeight() {
                int minHeight = textRenderer.lineHeight * 5;
                return Math.max(super.getHeight(), minHeight);
            }
        };

        // Create load button
        this.loadButton = new IconButtonWidget(
                0, 0, 20, 20,
                0, 0, 0, 2,
                32, 16, 16, 16, 48,
                SkinShuffle.id("textures/gui/reload-button-icon.png"),
                button -> {
                    if (currentSourceType != SkinLoader.SourceType.UNCHANGED) {
                        loadSkin();
                    }
                }
        );

        // Create skin model selection button
        this.skinModelButton = new CycleButton.Builder<>(Component::nullToEmpty, () ->preset.getSkin().getModel())
                .withValues("classic", "slim")
                .create(0, 0, 192, 20, Component.translatable("skinshuffle.edit.source.skin_model"), (widget, val) -> {
                    preset.getSkin().setModel(val);
                });

        // Create source type selection button
        gridAdder.addChild(
                CycleButton.builder((object)-> Component.translatable(object.getTranslationKey()), currentSourceType).withValues(Arrays.stream(SkinLoader.SourceType.values()).toList()).create(0, 0, 192, 20, Component.translatable("skinshuffle.edit.customize.keybind_id_prefix"), (button, value) -> {  // on value change
                    this.currentSourceType = value;
                    this.errorLabel.setMessage(Component.empty());
                    validateInput();
                })
                , layout.newCellSettings().paddingTop(Math.min(height / 2 - 60, 20)));

        gridAdder.addChild(skinModelButton);

        // Add text field and load button in a subgrid
        var subGrid = new net.minecraft.client.gui.layouts.GridLayout();
        var subGridAdder = subGrid.columnSpacing(4).createRowHelper(2);
        gridAdder.addChild(subGrid, layout.newCellSettings().paddingTop(6).paddingBottom(6));
        subGridAdder.addChild(textFieldWidget);
        subGridAdder.addChild(loadButton);

        gridAdder.addChild(errorLabel, layout.newCellSettings().alignHorizontallyLeft());

        // Initial validation
        validateInput();
    }

    /**
     * Validates the current input and updates UI based on validity.
     */
    public void validateInput() {
        boolean isValid = true;

        if (currentSourceType != SkinLoader.SourceType.UNCHANGED) {
            String input = textFieldWidget.getValue();

            isValid = switch (currentSourceType) {
                case URL -> ValidationUtils.isValidUrl(input);
                case FILE -> ValidationUtils.isValidPngFilePath(input);
                case RESOURCE_LOCATION -> ValidationUtils.isValidIdentifier(input, client);
                case USERNAME -> ValidationUtils.isValidUsername(input);
                case UUID -> ValidationUtils.isValidUUID(input);
                default -> false;
            };

            // Update error message
            if (!isValid) {
                errorLabel.setMessage(Component.translatable(currentSourceType.getInvalidInputTranslationKey()));
            } else {
                errorLabel.setMessage(Component.empty());
            }
        }

        // Update visibility of input fields
        textFieldWidget.setVisible(currentSourceType != SkinLoader.SourceType.UNCHANGED);
        loadButton.visible = currentSourceType != SkinLoader.SourceType.UNCHANGED;
        loadButton.active = currentSourceType != SkinLoader.SourceType.UNCHANGED && isValid;
        skinModelButton.visible = currentSourceType != SkinLoader.SourceType.UNCHANGED;

        // Notify validation status change
        onValidationChanged.accept(isValid);
    }

    /**
     * Loads the skin from the specified source.
     */
    public void loadSkin() {
        loading = true;
        String skinSource = textFieldWidget.getValue();
        String model = skinModelButton.getValue();

        CompletableFuture<Void> future = SkinLoader.loadSkin(
                currentSourceType, skinSource, model, preset);

        future.thenRun(() -> loading = false);
    }

    /**
     * Handles files being dropped onto the screen.
     */
    public void handleFileDrop(Path path) {
        if (ValidationUtils.isValidPngFilePath(path.toString())) {
            currentSourceType = SkinLoader.SourceType.FILE;
            errorLabel.setMessage(Component.empty());
            textFieldWidget.setValue(path.toString());
            validateInput();
            loadSkin();
        } else {
            ToastHelper.showToast("invalid_dropped_file");
        }
    }

    /**
     * Gets the current source type.
     */
    public SkinLoader.SourceType getCurrentSourceType() {
        return currentSourceType;
    }

    /**
     * Gets the source text field.
     */
    public EditBox getTextFieldWidget() {
        return textFieldWidget;
    }

    /**
     * Returns whether a skin is currently loading.
     */
    public boolean isLoading() {
        return loading;
    }
}
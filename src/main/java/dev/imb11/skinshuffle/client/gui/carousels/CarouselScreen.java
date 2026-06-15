package dev.imb11.skinshuffle.client.gui.carousels;

import com.mojang.blaze3d.Blaze3D;
import dev.imb11.skinshuffle.SkinShuffle;
import dev.imb11.skinshuffle.client.config.CarouselView;
import dev.imb11.skinshuffle.client.config.SkinPresetManager;
import dev.imb11.skinshuffle.client.config.SkinShuffleConfig;
import dev.imb11.skinshuffle.client.gui.GeneratedScreens;
import dev.imb11.skinshuffle.client.gui.widgets.buttons.SkinShuffleIconButton;
import dev.imb11.skinshuffle.client.gui.widgets.presets.AbstractCardWidget;
import dev.imb11.skinshuffle.client.gui.widgets.presets.AddCardWidget;
import dev.imb11.skinshuffle.client.gui.widgets.presets.PresetWidget;
import dev.imb11.skinshuffle.client.preset.SkinPreset;
import dev.imb11.skinshuffle.client.skin.Skin;
import dev.imb11.skinshuffle.networking.ClientSkinHandling;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.render.SpruceGuiGraphics;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.tooltip.Tooltip;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.SpruceIconButtonWidget;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class CarouselScreen extends SpruceScreen {
    public static final double scaleFactor = 1.0D;
    public final Screen parent;
    public final CarouselView viewType;
    public final CarouselView nextViewType;
    public boolean hasEditedPreset = false;
    public ArrayList<AbstractCardWidget<CarouselScreen>> carouselWidgets = new ArrayList<>();
    protected SpruceIconButtonWidget configButton;
    protected SpruceIconButtonWidget viewTypeButton;
    protected SpruceButtonWidget cancelButton;
    protected SpruceButtonWidget selectButton;
    private double cardIndex = -1;
    private double lastCardIndex = 0;
    private double lastCardSwitchTime = 0;

    public CarouselScreen(Screen parent, CarouselView viewType, CarouselView nextViewType) {
        super(Component.translatable("skinshuffle.carousel.title"));
        this.parent = parent;
        this.viewType = viewType;
        this.nextViewType = nextViewType;
    }

    @Override
    protected void init() {
        super.init();
        carouselWidgets.clear();

        var loadedPresets = SkinPresetManager.getLoadedPresets();
        for (SkinPreset preset : loadedPresets) {
            var presetWidget = this.widgetFromPreset(preset);
            this.carouselWidgets.add(presetWidget);
        }

        var addPresetWidget = new AddCardWidget(this,
                Position.of(0, 0), getCardWidth(), getCardHeight());
        addPresetWidget.setCallback(() -> {
            SkinPreset unnamed = new SkinPreset(Skin.randomDefaultSkin());
            SkinPresetManager.addPreset(unnamed);
            var newWidget = this.loadPreset(unnamed);
            this.addRenderableWidget(newWidget);
        });
        this.carouselWidgets.add(addPresetWidget);

        // We don't want to switch back to the selected preset when we return from a subscreen.
        if (!loadedPresets.isEmpty()) {
            if (this.cardIndex < 0) {
                //noinspection IntegerDivisionInFloatingPointContext
                this.cardIndex = loadedPresets.indexOf(SkinPresetManager.getChosenPreset()) / getRows() * getRows();
            }
            this.lastCardIndex = this.cardIndex;
        } else {
            // Set default values when no presets exist
            this.cardIndex = 0;
            this.lastCardIndex = 0;
        }

        for (SpruceWidget presetCards : this.carouselWidgets) {
            this.addRenderableWidget(presetCards);
        }

        this.cancelButton = this.addRenderableWidget(new SpruceButtonWidget(Position.of(this.width / 2 - 128 - 5, this.height - 23), 128, 20, CommonComponents.GUI_CANCEL, button -> {
            this.onClose();
        }));

        this.configButton = this.addRenderableWidget(new SkinShuffleIconButton(Position.of(2, 2), 20, 20, Component.empty(),
                (btn) -> this.minecraft.setScreenAndShow(GeneratedScreens.getConfigScreen(this)),
                (btn) -> SkinShuffle.id("textures/gui/config-button-icon.png")));
        this.configButton.setTooltip(Component.translatable("skinshuffle.carousel.config_button.tooltip"));

        this.viewTypeButton = this.addRenderableWidget(new SkinShuffleIconButton(Position.of(24, 2), 20, 20, Component.empty(), (btn) -> {
            this.minecraft.setScreenAndShow(nextViewType.factory.apply(this.parent));
            SkinShuffleConfig.get().carouselView = nextViewType;
            SkinShuffleConfig.save();
        }, (btn) -> viewType.iconTexture));
        this.viewTypeButton.setTooltip(viewType.tooltip);

        this.selectButton = this.addRenderableWidget(new SpruceButtonWidget(Position.of(this.width / 2 + 5, this.height - 23), 128, 20, Component.translatable("skinshuffle.carousel.save_button"), button -> {
            if (Math.round(cardIndex) >= 0 && Math.round(cardIndex) < this.carouselWidgets.size()) {
                SpruceWidget chosenPresetWidget = this.carouselWidgets.get((int) Math.round(cardIndex));

                if (chosenPresetWidget instanceof AddCardWidget) {
                    this.onClose();
                    return;
                }

                if (chosenPresetWidget instanceof PresetWidget<?> presetWidget) {
                    SkinPresetManager.setChosenPreset(presetWidget.getPreset(), this.hasEditedPreset);
                    SkinPresetManager.savePresets();
                }
            }

            this.handleCloseBehaviour();
        }));

        refreshPresetState();
    }

    public void handleCloseBehaviour() {
        if (this.minecraft.level != null && !SkinShuffleConfig.get().disableAPIUpload && !ClientSkinHandling.isInstalledOnServer()) {
            this.minecraft.gui.setScreen(GeneratedScreens.getReconnectScreen(this.parent));
        } else {
            this.onClose();
        }
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);

        // Save all presets to config when closing the screen, skipping any we can't
        for (var preset : SkinPresetManager.getLoadedPresets()) {
            try {
                var configSkin = preset.getSkin().saveToConfig();
                preset.setSkin(configSkin);
            } catch (Exception ignored) {
            }
        }

        SkinPresetManager.savePresets();
    }

    @Override
    public void extractRenderState(SpruceGuiGraphics graphics, int mouseX, int mouseY, float delta) {
        var cardAreaWidth = getCardWidth() + getCardGap();

        graphics.fill(0, this.font.lineHeight * 3, this.width, this.height - (this.font.lineHeight * 3), 0x7F000000);

        graphics.fillGradient(0, (int) (this.font.lineHeight * 2.75), this.width, this.font.lineHeight * 3, 0x00000000, 0x00000000, 0x7F000000, 0x7F000000);
        graphics.fillGradient(0, this.height - (this.font.lineHeight * 3), this.width, (int) (this.height - (this.font.lineHeight * 2.75)), 0x7F000000, 0x7F000000, 0x00000000, 0x00000000);

        // Carousel Widgets
        int rows = getRows();
        int fullHeight = getCardHeight() * rows + getCardGap() * (rows - 1);
        int top = this.height / 2 - fullHeight / 2;

        double deltaIndex = getDeltaScrollIndex() / rows;
        int scrollOffset = (int) ((-deltaIndex + 1) * cardAreaWidth);
        var scrollOffsetPosition = Position.of(scrollOffset, 0);
        int draggingIndex = -1;
        int hoveredIndex = -1;

        int i = 0;
        int leftI = this.width / 2 - cardAreaWidth - getCardWidth() / 2;
        int rowI = 0;
        for (AbstractCardWidget<?> widget : this.carouselWidgets) {
            var topI = top + (getCardHeight() + getCardGap()) * rowI;
            var position = Position.of(
                    scrollOffsetPosition, widget.getDeltaX(leftI),
                    widget.getDeltaY(topI)
            );

            if (supportsDragging()) {
                var boundLeft = leftI + scrollOffsetPosition.getX();
                var boundTop = topI + scrollOffsetPosition.getY();
                if (mouseX > boundLeft && mouseX < boundLeft + widget.getWidth() &&
                        mouseY > boundTop && mouseY < boundTop + widget.getHeight() && widget.isMovable()) {
                    hoveredIndex = i;
                }

                if (widget.isDragging() && widget.isMovable()) {
                    position = Position.of(mouseX - (int) widget.getDragStartX(), mouseY - (int) widget.getDragStartY());
                    draggingIndex = i;
                }
            }

            if (widget instanceof PresetWidget<?> loadedPreset) {
                loadedPreset.overridePosition(position);
                loadedPreset.setScaleFactor(scaleFactor);
            } else if (widget instanceof AddCardWidget addCardWidget) {
                addCardWidget.overridePosition(position);
            }

            widget.setActive(Math.round(cardIndex) == i || getRows() > 1);
            widget.updateVisibility(i);

            if (++rowI >= rows) {
                rowI = 0;
                leftI += cardAreaWidth;
            }
            i++;
        }

        if (draggingIndex != -1 && hoveredIndex != -1 && draggingIndex != hoveredIndex) {
            swapPresets(draggingIndex, hoveredIndex);
            refreshPresetState();
        }

        this.extractWidgets(graphics, mouseX, mouseY, delta);

        this.renderTitle(graphics.vanilla(), mouseX, mouseY, delta);
        Tooltip.extractAllRenderStates(graphics.vanilla());
    }

    public void renderTitle(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.centeredText(this.font, this.getTitle().getVisualOrderText(), this.width / 2, this.font.lineHeight, 0xFFFFFFFF);
    }

    public void scrollCarousel(double amount, boolean wrapAround) {
        if (this.carouselWidgets.size() == 1) {
            return;
        }

        amount *= getRows();
        var cardIndex = this.cardIndex + amount;

        if (wrapAround) {
            cardIndex = (cardIndex + this.carouselWidgets.size()) % this.carouselWidgets.size();
        }
        //noinspection IntegerDivisionInFloatingPointContext
        cardIndex = Mth.clamp(cardIndex, 0, (this.carouselWidgets.size() - 1) / getRows() * getRows());

        setCardIndex(cardIndex);
    }

    public void snapCarousel() {
        long cardIndex = Math.round(this.cardIndex) / getRows() * getRows();
        setCardIndex(cardIndex);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hozAmount, double verticalAmount) {
        var sign = SkinShuffleConfig.get().invertCarouselScroll ? -1 : 1;
        var horizontalSign = SkinShuffleConfig.get().invertCarouselHorizontalScroll ? -1 : 1;
        hozAmount = hozAmount*horizontalSign;
        scrollCarousel((-verticalAmount - hozAmount) / 4 * SkinShuffleConfig.get().carouselScrollSensitivity * sign, false);
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        carouselWidgets.forEach(widget -> widget.setDragging(false));

        return super.mouseReleased(event);
    }

    protected abstract int getRows();

    public int getCardWidth() {
        return this.width / 4;
    }

    public int getCardHeight() {
        return ((int) (this.height / 1.5) - getCardGap() * (getRows() - 1)) / getRows();
    }

    public int getCardGap() {
        return (int) (10 * scaleFactor) / getRows();
    }

    protected boolean supportsDragging() {
        return false;
    }

    protected abstract AbstractCardWidget<CarouselScreen> widgetFromPreset(SkinPreset preset);

    private double getDeltaScrollIndex() {
        var deltaTime = (Blaze3D.getTime() - lastCardSwitchTime) * 5;
        deltaTime = Mth.clamp(deltaTime, 0, 1);
        deltaTime = Math.sin(deltaTime * Math.PI / 2);
        return Mth.lerp(deltaTime, lastCardIndex, cardIndex);
    }

    public AbstractCardWidget<CarouselScreen> loadPreset(SkinPreset preset) {
        var widget = widgetFromPreset(preset);
        this.carouselWidgets.getLast().refreshLastPosition();
        this.carouselWidgets.add(this.carouselWidgets.set(this.carouselWidgets.size() - 1, widget));
        refreshPresetState();
        return widget;
    }

    public void swapPresets(int index1, int index2) {
        this.carouselWidgets.get(index1).refreshLastPosition();
        this.carouselWidgets.get(index2).refreshLastPosition();
        Collections.swap(this.carouselWidgets, index1, index2);
        SkinPresetManager.swapPresets(index1, index2);
        refreshPresetState();
    }

    public Optional<PresetWidget<?>> getPresetWidget(int index) {
        if (index < 0 || index >= this.carouselWidgets.size()) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.carouselWidgets.get(index))
                .filter(widget -> widget instanceof PresetWidget<?>)
                .map(widget -> (PresetWidget<?>) widget);
    }

    public void refreshPresetState() {
        this.carouselWidgets.forEach(AbstractCardWidget::refreshState);
    }

    public void setCardIndex(double index) {
        lastCardIndex = getDeltaScrollIndex();
        lastCardSwitchTime = Blaze3D.getTime();
        cardIndex = index;
    }

    public double getLastCardSwitchTime() {
        return lastCardSwitchTime;
    }

    public void refresh() {
        this.children().clear();
        this.init();
        Minecraft.getInstance().gui.setScreen(this);
    }
}

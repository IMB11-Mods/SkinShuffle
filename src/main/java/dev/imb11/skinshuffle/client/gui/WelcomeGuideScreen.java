package dev.imb11.skinshuffle.client.gui;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.navigation.NavigationEvent;
import dev.lambdaurora.spruceui.render.SpruceGuiGraphics;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceContainerWidget;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

public class WelcomeGuideScreen extends SpruceScreen {
    private final Screen parent;
    private ScrollableTextContainer textContainer;

    public WelcomeGuideScreen(Screen parent) {
        super(Component.translatable("skinshuffle.welcome.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        // Create our custom scrollable container
        int containerHeight = this.height - 60; // Example height
        textContainer = new ScrollableTextContainer(
                Position.of(0, 20), // Position just below the title
                this.width,
                containerHeight
        );
        this.addRenderableWidget(textContainer);

        // Add Continue and More Info buttons
        this.addRenderableWidget(new SpruceButtonWidget(
                Position.of(this.width / 2 - 128 - 5, this.height - 23),
                128, 20,
                CommonComponents.GUI_CONTINUE,
                button -> this.minecraft.setScreen(parent)
        ));

        this.addRenderableWidget(new SpruceButtonWidget(
                Position.of(this.width / 2 + 5, this.height - 23),
                128, 20,
                Component.translatable("skinshuffle.welcome.more_info"),
                button -> this.minecraft.setScreen(
                        new ConfirmLinkScreen(
                                ignored -> onClose(),
                                "https://youtu.be/CNMASU7GQBs",
                                true
                        )
                )
        ));
    }

    @Override
    public void extractRenderState(@NotNull SpruceGuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.vanilla().text(
                this.minecraft.font,
                this.title,
                this.width / 2 - this.minecraft.font.width(this.title) / 2,
                10,
                0xFFFFFFFF
        );
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private static class ScrollableTextContainer extends SpruceContainerWidget {
        private final Component[] lines;
        private double scrollOffset = 0;
        private double maxScrollOffset = 0;

        public ScrollableTextContainer(Position position, int width, int height) {
            super(position, width, height);

            this.lines = new Component[]{
                    Component.translatable("screen.skinshuffle.thankyou"),
                    Component.translatable("screen.skinshuffle.read_info"),
                    Component.translatable("screen.skinshuffle.blank"),
                    Component.translatable("screen.skinshuffle.carousel_heading"),
                    Component.translatable("screen.skinshuffle.carousel_desc1"),
                    Component.translatable("screen.skinshuffle.carousel_desc2"),
                    Component.translatable("screen.skinshuffle.carousel_desc3"),
                    Component.translatable("screen.skinshuffle.carousel_desc4"),
                    Component.translatable("screen.skinshuffle.blank"),
                    Component.translatable("screen.skinshuffle.preset_edit_heading"),
                    Component.translatable("screen.skinshuffle.preset_edit_desc1"),
                    Component.translatable("screen.skinshuffle.preset_edit_desc2"),
                    Component.translatable("screen.skinshuffle.blank"),
                    Component.translatable("screen.skinshuffle.config_heading"),
                    Component.translatable("screen.skinshuffle.config_desc"),
                    Component.translatable("screen.skinshuffle.blank"),
                    Component.translatable("screen.skinshuffle.hotswapping_heading"),
                    Component.translatable("screen.skinshuffle.hotswapping_desc1"),
                    Component.translatable("screen.skinshuffle.hotswapping_desc2")
            };
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            scrollOffset = Math.max(0, Math.min(scrollOffset - scrollY * 10, maxScrollOffset));
            return true;
        }

        @Override
        public void extractRenderState(SpruceGuiGraphics context, int mouseX, int mouseY, float delta) {
            super.extractRenderState(context, mouseX, mouseY, delta);

            context.enableScissor(
                    this.getX(),
                    this.getY(),
                    this.getX() + this.width,
                    this.getY() + this.height
            );

            int lineHeight = this.client.font.lineHeight + 5;
            int currentY = this.getY() + 5 - (int) scrollOffset;
            int wrapWidth = this.width - 20;
            int totalContentHeight = 0;

            // First pass: measure the total height of wrapped lines
            for (Component line : lines) {
                // Directly use the Text object
                if (line.getString().isEmpty()) {
                    totalContentHeight += lineHeight;
                } else {
                    var wrappedLines = this.client.font.split(line, wrapWidth);
                    totalContentHeight += wrappedLines.size() * lineHeight;
                }
            }

            maxScrollOffset = Math.max(0, totalContentHeight - this.height + 10);

            // Second pass: render wrapped text
            for (Component line : lines) {
                if (line.getString().isEmpty()) {
                    currentY += lineHeight;
                    continue;
                }
                // Wrap the Text object, then draw each wrapped line
                var wrappedLines = this.client.font.split(line, wrapWidth);
                for (FormattedCharSequence wrappedLine : wrappedLines) {
                    context.vanilla().text(
                            this.client.font,
                            wrappedLine,
                            this.getX() + 10,
                            currentY,
                            0xFFFFFFFF
                    );
                    currentY += lineHeight;
                }
            }

            // Draw scrollbar if needed
            if (maxScrollOffset > 0) {
                int scrollbarHeight = (int) ((this.height * (double) this.height) / totalContentHeight);
                int scrollbarY = (int) (this.getY() + (scrollOffset / maxScrollOffset) * (this.height - scrollbarHeight));

                context.fill(
                        this.getX() + this.width - 5,
                        this.getY(),
                        this.getX() + this.width,
                        this.getY() + this.height,
                        0x80000000
                );
                context.fill(
                        this.getX() + this.width - 5,
                        scrollbarY,
                        this.getX() + this.width,
                        scrollbarY + scrollbarHeight,
                        0xFFFFFFFF
                );
            }

            context.disableScissor();
        }

        @Override
        public boolean onNavigation(NavigationEvent direction) {
            return super.onNavigation(direction);
        }
    }
}

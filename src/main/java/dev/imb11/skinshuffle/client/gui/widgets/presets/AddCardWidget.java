package dev.imb11.skinshuffle.client.gui.widgets.presets;

import dev.imb11.skinshuffle.SkinShuffle;
import dev.imb11.skinshuffle.client.gui.carousels.CarouselScreen;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.render.SpruceGuiGraphics;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class AddCardWidget extends AbstractCardWidget<CarouselScreen> {
    //    private final DummyClientPlayerEntity entity;
    private Runnable action;

    public AddCardWidget(CarouselScreen parent, Position position, int width, int height) {
        super(position, width, height, parent);

        addChild(new SpruceButtonWidget(Position.of(3, getHeight() - 24), width - 6, 20, Component.translatable("skinshuffle.carousel.create"), button -> {
            action.run();
        }));
    }

    public void setCallback(@Nullable Runnable action) {
        this.action = action;
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        for (SpruceWidget child : this.children()) {
            child.setActive(active);
        }
    }

    @Override
    protected void renderBackground(SpruceGuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.vanilla().submitOutline(getX(), getY(), getWidth(), getHeight(), this.active ? 0xDF000000 : 0x5F000000);
        graphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, this.active ? 0x7F000000 : 0x0D000000);

        graphics.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                SkinShuffle.id("textures/gui/carousel_add.png"),
                getX() + (this.getWidth() / 2) - 16,
                getY() + (this.getHeight() / 2) - 16,
                0,
                !this.active ? 32 : 0,
                32,
                32,
                32,
                64);


        var text = Component.translatable("skinshuffle.carousel.new");
        graphics.vanilla().drawString(this.client.font, text, getX() + (this.width / 2) - this.client.font.width(text) / 2, getY() + this.client.font.lineHeight / 2, this.active ? 0xFFFFFFFF : 0xFF808080);
    }

    @Override
    public boolean isMovable() {
        return false;
    }
}

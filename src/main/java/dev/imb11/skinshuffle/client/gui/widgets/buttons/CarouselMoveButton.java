package dev.imb11.skinshuffle.client.gui.widgets.buttons;

import dev.imb11.skinshuffle.SkinShuffle;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.render.SpruceGuiGraphics;
import dev.lambdaurora.spruceui.widget.AbstractSpruceWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class CarouselMoveButton extends AbstractSpruceWidget {
    private static final Identifier ARROW_TEXTURES = SkinShuffle.id("textures/gui/carousel_arrows.png");
    private final Type type;
    private @Nullable Runnable action;

    public CarouselMoveButton(Position position, boolean isRight) {
        super(position);
        this.width = 16;
        this.height = 16;
        this.type = isRight ? Type.RIGHT : Type.LEFT;
        if (isRight) {
            position.setRelativeX(position.getRelativeX() - width);
        }
    }

    public CarouselMoveButton(Position position, Type type) {
        super(position);
        this.width = type.width * 2;
        this.height = type.height * 2;
        this.type = type;
        position.setRelativeX(position.getRelativeX() - width / 2);
        position.setRelativeY(position.getRelativeY() - height / 2);
    }

    public void setCallback(@Nullable Runnable action) {
        this.action = action;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return this.isMouseOver(event.x(), event.y()) && this.onMouseClick(event, doubleClick);
    }

    @Override
    protected boolean onMouseClick(MouseButtonEvent event, boolean doubleClick) {
        if (this.action != null) {
            try {
                this.action.run();
                this.playDownSound();
            } catch (Exception e) {
                throw new RuntimeException("Failed to trigger callback for CarouselMoveButton{x=" + getX() + ", y=" + getY() + "}\n" + e);
            }
        }
        return false;
    }

    @Override
    protected void renderWidget(SpruceGuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        var matrices = guiGraphics.vanilla().pose();
        matrices.pushMatrix();
        guiGraphics.vanilla().blit(
                RenderPipelines.GUI_TEXTURED,
                ARROW_TEXTURES,
                getX(),
                getY(),
                this.type.u,
                (this.active ? (this.hovered || this.focused ? this.type.height : 0) : this.type.height),
                width,
                height,
                this.type.width,
                this.type.height,
                64,
                64
        );
        matrices.popMatrix();
    }

    @Override
    protected @Nullable Component getNarrationMessage() {
        return Component.translatable("skinshuffle.carousel." + this.type.name);
    }

    public enum Type {
        LEFT("left", 0),
        RIGHT("right", 8),
        UP("up", 16),
        DOWN("down", 24),
        LEFT_RIGHT("left_right", 32, 16, 16),
        UP_DOWN("up_down", 48, 16, 16);

        public final String name;
        public final int u;
        public final int width;
        public final int height;

        Type(String name, int u) {
            this.name = name;
            this.u = u;
            this.width = 8;
            this.height = 8;
        }

        Type(String name, int u, int width, int height) {
            this.name = name;
            this.u = u;
            this.width = width;
            this.height = height;
        }
    }
}

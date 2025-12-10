package dev.imb11.skinshuffle.client.gui.widgets.buttons;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class IconButtonWidget extends Button {
    public final int iconWidth;
    public final int iconHeight;
    protected final Identifier iconTexture;
    protected final int iconU;
    protected final int iconV;
    protected final int iconDisabledVOffset;
    protected final int iconTextureWidth;
    protected final int iconTextureHeight;
    private final int iconXOffset;
    private final int iconYOffset;

    public IconButtonWidget(int x, int y, int width, int height, int iconU, int iconV, int iconXOffset, int iconYOffset, int iconDisabledVOffset, int iconWidth, int iconHeight, int iconTextureWidth, int iconTextureHeight, Identifier iconTexture, Button.OnPress onPress) {
        super(x, y, width, height, Component.nullToEmpty(""), onPress, DEFAULT_NARRATION);
        this.iconTextureWidth = iconTextureWidth;
        this.iconTextureHeight = iconTextureHeight;
        this.iconU = iconU;
        this.iconV = iconV;
        this.iconDisabledVOffset = iconDisabledVOffset;
        this.iconTexture = iconTexture;
        this.iconXOffset = iconXOffset;
        this.iconYOffset = iconYOffset;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
    }

    @Override
    protected void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderDefaultSprite(context);
        context.blit(
                RenderPipelines.GUI_TEXTURED,
                this.iconTexture,
                this.getIconX(),
                this.getIconY(),
                this.iconU,
                this.iconV + (active ? (isHovered ? 16 : 0) : this.iconDisabledVOffset),
                this.iconWidth,
                this.iconHeight,
                this.iconTextureWidth,
                this.iconTextureHeight
        );
    }

    @Override
    protected void renderScrollingStringOverContents(ActiveTextCollector activeTextCollector, Component component, int color) {
        int i = this.getX() + 2;
        int j = this.getX() + this.getWidth() - this.iconWidth - 6;
        super.renderScrollingStringOverContents(activeTextCollector, component, color);
    }

    int getIconX() {
        return this.getX() + (this.width / 2 - this.iconWidth / 2) + this.iconXOffset;
    }

    int getIconY() {
        return this.getY() + this.iconYOffset;
    }
}
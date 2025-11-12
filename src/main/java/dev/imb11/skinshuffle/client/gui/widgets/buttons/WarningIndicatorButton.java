package dev.imb11.skinshuffle.client.gui.widgets.buttons;

import dev.imb11.skinshuffle.SkinShuffle;
import dev.imb11.skinshuffle.client.gui.GeneratedScreens;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class WarningIndicatorButton extends IconButtonWidget {
    public WarningIndicatorButton(int x, int y, Screen parent) {
        super(x, y, 20, 20,
                0, 0, 0, 2,
                16, 16, 16, 16, 32,
                SkinShuffle.id("textures/gui/warning-icon.png"),
                button -> {
                    var client = Minecraft.getInstance();
                    client.setScreen(GeneratedScreens.getReconnectScreen(parent));
                }
        );

        var client = Minecraft.getInstance();

        this.setTooltip(Tooltip.create(Component.literal(I18n.get("skinshuffle.reconnect.warning",
                client.isLocalServer() ? I18n.get("skinshuffle.reconnect.rejoin") : I18n.get("skinshuffle.reconnect.reconnect"))).withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
    }

    @Override
    public Component getMessage() {
        return Component.translatable("skinshuffle.indicator");
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

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
}
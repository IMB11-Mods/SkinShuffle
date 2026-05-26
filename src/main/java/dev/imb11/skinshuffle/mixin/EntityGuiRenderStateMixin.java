package dev.imb11.skinshuffle.mixin;

import dev.imb11.skinshuffle.client.gui.renderer.InstancedGuiEntityElementRenderer;
import dev.imb11.skinshuffle.client.gui.renderer.InstancedGuiEntityRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiEntityRenderState.class)
public class EntityGuiRenderStateMixin implements InstancedGuiEntityRenderState {
    @Unique
    private float alpha;

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public float getAlpha() {
        return this.alpha;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public InstancedGuiEntityElementRenderer newRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        return new InstancedGuiEntityElementRenderer(vertexConsumers, Minecraft.getInstance().getEntityRenderDispatcher());
    }
}

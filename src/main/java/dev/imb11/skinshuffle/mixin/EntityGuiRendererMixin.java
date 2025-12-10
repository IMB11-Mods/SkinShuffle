package dev.imb11.skinshuffle.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.imb11.skinshuffle.MixinStatics;
import dev.imb11.skinshuffle.client.gui.renderer.InstancedGuiEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityGuiRendererMixin {

    @Inject(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitShadow(Lcom/mojang/blaze3d/vertex/PoseStack;FLjava/util/List;)V"))
    public void beforeRenderDispatcher(EntityRenderState entityGuiElementRenderState, CameraRenderState cameraRenderState, double d, double e, double f, PoseStack matrixStack, SubmitNodeCollector orderedRenderCommandQueue, CallbackInfo ci) {
        if (entityGuiElementRenderState instanceof InstancedGuiEntityRenderState guiState) {
            MixinStatics.RENDERING_STATE = guiState;
        }
    }

    @Inject(method = "submit", at = @At(value = "RETURN"))
    public void afterRenderDispatcher(EntityRenderState renderState, CameraRenderState cameraRenderState, double d, double e, double f, PoseStack matrixStack, SubmitNodeCollector orderedRenderCommandQueue, CallbackInfo ci) {
        MixinStatics.RENDERING_STATE = null;
    }
}

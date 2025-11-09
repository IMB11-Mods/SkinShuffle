package dev.imb11.skinshuffle.mixin;

import dev.imb11.skinshuffle.MixinStatics;
import dev.imb11.skinshuffle.client.gui.renderer.InstancedGuiEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderManager.class)
public abstract class EntityGuiRendererMixin {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitShadowPieces(Lnet/minecraft/client/util/math/MatrixStack;FLjava/util/List;)V"))
    public void beforeRenderDispatcher(EntityRenderState entityGuiElementRenderState, CameraRenderState cameraRenderState, double d, double e, double f, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CallbackInfo ci) {
        if (entityGuiElementRenderState instanceof InstancedGuiEntityRenderState guiState) {
            MixinStatics.RENDERING_STATE = guiState;
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitDebugHitbox(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/client/render/entity/state/EntityHitboxAndView;)V"))
    public void afterRenderDispatcher(EntityRenderState renderState, CameraRenderState cameraRenderState, double d, double e, double f, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CallbackInfo ci) {
        MixinStatics.RENDERING_STATE = null;
    }
}

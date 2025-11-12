package dev.imb11.skinshuffle.mixin;

import dev.imb11.skinshuffle.MixinStatics;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @ModifyArg(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
            ),
            index = 4
    )
    private int changeAlpha(int originalColour) {
        if (MixinStatics.RENDERING_STATE == null) return originalColour;
        if (MixinStatics.RENDERING_STATE.getAlpha() == 0) return originalColour;

        int alpha = (int) (Mth.clamp(MixinStatics.RENDERING_STATE.getAlpha(), 0.0F, 1.0F) * 255.0F) & 0xFF;
        return (alpha << 24) | (originalColour & 0x00FFFFFF);
    }
}

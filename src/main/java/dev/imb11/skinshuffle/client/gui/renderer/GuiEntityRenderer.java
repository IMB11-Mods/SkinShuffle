package dev.imb11.skinshuffle.client.gui.renderer;

import dev.imb11.skinshuffle.client.SkinShuffleClient;
import dev.imb11.skinshuffle.client.config.SkinShuffleConfig;
import dev.imb11.skinshuffle.client.skin.Skin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GuiEntityRenderer {

    public static void drawEntity(GuiGraphics context, int x1, int y1, int x2, int y2, int size,
                                  float rotation, double mouseX, double mouseY, Skin skin,
                                  SkinShuffleConfig.SkinRenderStyle style, float alpha) {

        // Calculate center position
        int centerX = (x1 + x2) / 2;
        int centerY = (y1 + y2) / 2;

        // Calculate head rotation based on style
        float headYaw = 0.0f;
        float headPitch = 0.0f;

        if (style == SkinShuffleConfig.SkinRenderStyle.CURSOR) {
            // Calculate mouse position relative to the render area center
            float deltaX = (float) (centerX - mouseX);
            float deltaY = (float) (mouseY - centerY);

            // Use atan2 for smooth, natural head rotation
            // Scale the input to create a reasonable sensitivity
            float sensitivity = 0.003f; // Adjust this value to control sensitivity
            headYaw = (float) Math.toDegrees(Math.atan(deltaX * sensitivity));
            headPitch = (float) Math.toDegrees(Math.atan(deltaY * sensitivity));
        }

        // Create player render state
        AvatarRenderState renderState = createPlayerRenderState(skin, headYaw, headPitch, rotation);

        // Create base rotation quaternion (try Z-axis rotation if Y-axis doesn't work)
//        System.out.println(rotation);
        Quaternionf baseRotation = new Quaternionf();
        baseRotation.rotationZ((float) (Math.PI * 1.0f));

        // Calculate entity position (negative Y to fix upside down rendering)
        Vector3f entityPosition = new Vector3f(0.0F, 1.1F, 0.0F);

        // Render the entity within the scissor area
        context.enableScissor(x1, y1, x2, y2);

        GuiEntityRenderState state = new GuiEntityRenderState(renderState, entityPosition, baseRotation, null, x1, y1, x2, y2, (float) size, context.scissorStack.peek());

        ((InstancedGuiEntityRenderState) (Object) state).setAlpha(alpha);

        context.guiRenderState.submitPicturesInPictureState(state);
        context.disableScissor();
    }

    private static AvatarRenderState createPlayerRenderState(Skin skin, float headYaw, float headPitch, float rotation) {
        AvatarRenderState state = new AvatarRenderState();

        // Basic entity properties
        state.ageInTicks = SkinShuffleClient.TOTAL_TICK_DELTA;
        state.boundingBoxWidth = 0.6F;
        state.boundingBoxHeight = 1.8F;
        state.eyeHeight = 1.62F;
        state.isInvisible = false;
        state.isDiscrete = false;
        state.displayFireAnimation = false;

        // Body orientation - keep body facing forward
        state.bodyRot = headYaw * 0.1f + rotation;
        state.yRot = headYaw; // Only the head turns with mouse
        state.xRot = headPitch; // Head pitch
        state.deathTime = 0.0F;

        // Gentle arm swaying animation
        float animationTime = SkinShuffleClient.TOTAL_TICK_DELTA * 0.067F;
        state.walkAnimationPos = Mth.sin(animationTime) * 0.05F;
        state.walkAnimationSpeed = 0.1F;

        // Standard entity state
        state.scale = 1.0F;
        state.ageScale = 1.0F;
        state.isUpsideDown = false; // Ensure this is explicitly false
        state.isFullyFrozen = false;
        state.isBaby = false;
        state.isInWater = false;
        state.isAutoSpinAttack = false;
        state.hasRedOverlay = false;
        state.isInvisibleToPlayer = false;
        state.outlineColor = 0;
        state.shadowPieces.clear();
        state.lightCoords = 15728880;
        state.bedOrientation = null;
        state.nameTag = null;
        state.pose = Pose.STANDING;

        // Biped state - keep everything neutral
        state.swimAmount = 0.0F;
        state.attackTime = 0.0F;
        state.speedValue = 1.0F;
        state.maxCrossbowChargeDuration = 0.0F;
        state.ticksUsingItem = 0;
        state.isCrouching = false;
        state.isFallFlying = false;
        state.isVisuallySwimming = false;
        state.isPassenger = false;
        state.isUsingItem = false;
        state.elytraRotX = 0.0F;
        state.elytraRotY = 0.0F;
        state.elytraRotZ = 0.0F;

        // Player-specific properties
        state.skin = skin.getSkinTextures();
        state.scoreText = Component.nullToEmpty(String.valueOf(skin.hashCode()));
        state.isSpectator = false;
        state.arrowCount = 0;
        state.stingerCount = 0;
        state.fallFlyingTimeInTicks = 0.0F;
        state.shouldApplyFlyingYRot = false;
        state.flyingYRot = 0.0F;

        // Skin layer visibility
        state.showHat = true;
        state.showJacket = true;
        state.showLeftPants = true;
        state.showRightPants = true;
        state.showLeftSleeve = true;
        state.showRightSleeve = true;
        state.showCape = SkinShuffleConfig.get().showCapeInPreview;

        // Misc properties
        state.scoreText = null;
        state.parrotOnLeftShoulder = null;
        state.parrotOnRightShoulder = null;
        state.id = 0;

        return state;
    }
}
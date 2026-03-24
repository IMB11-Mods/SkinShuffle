package dev.imb11.skinshuffle.mixin.screen;

import dev.imb11.skinshuffle.MixinStatics;
import dev.imb11.skinshuffle.client.config.SkinPresetManager;
import dev.imb11.skinshuffle.client.config.SkinShuffleConfig;
import dev.imb11.skinshuffle.client.gui.GeneratedScreens;
import dev.imb11.skinshuffle.util.NetworkingUtil;
import dev.imb11.skinshuffle.util.ToastHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    @Shadow
    @Final
    private boolean fading;
    @Unique
    private ArrayList<AbstractWidget> openCarouselWidgets;

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    public void refreshConfig(CallbackInfo ci) {
        if (!MixinStatics.APPLIED_SKIN_MANAGER_CONFIGURATION && this.fading) {
            MixinStatics.APPLIED_SKIN_MANAGER_CONFIGURATION = true;
            SkinPresetManager.apply();

            if (!NetworkingUtil.isLoggedIn()) {
                ToastHelper.showOfflineModeToast();
            }
        }
    }

    @Override
    public void onClose() {
        this.openCarouselWidgets = null;
    }

    @Inject(method = "added", at = @At("TAIL"), cancellable = false)
    public void updateVisibility(CallbackInfo ci) {
        if (!SkinShuffleConfig.get().displayInTitleScreen && this.openCarouselWidgets != null) {
            for (AbstractWidget openCarouselWidget : this.openCarouselWidgets) {
                this.removeWidget(openCarouselWidget);
            }
            this.openCarouselWidgets = null;
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void addButton(CallbackInfo ci) {
        /*
            TODO: Maybe different types of buttons?
             - Small icon button
             - Bedrock-style skin preview
         */
        if (SkinShuffleConfig.get().displayInTitleScreen) {
            this.openCarouselWidgets = GeneratedScreens.createCarouselWidgets(this);

            for (AbstractWidget carouselWidget : this.openCarouselWidgets) {
                this.addRenderableWidget(carouselWidget);
            }
        }
    }
}

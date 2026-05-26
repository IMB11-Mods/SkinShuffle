package dev.imb11.skinshuffle;

import dev.imb11.skinshuffle.client.gui.renderer.InstancedGuiEntityRenderState;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.world.entity.player.PlayerSkin;

public class MixinStatics {
    public static InstancedGuiEntityRenderState RENDERING_STATE;
    public static boolean APPLIED_SKIN_MANAGER_CONFIGURATION = false;
    public static CompletableFuture<CompletableFuture<Optional<PlayerSkin>>> INITIAL_SKIN_TEXTURES = null;
}

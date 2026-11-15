package dev.imb11.skinshuffle.client.skin;

import com.mojang.blaze3d.platform.NativeImage;
import dev.imb11.skinshuffle.SkinShuffle;
import dev.imb11.skinshuffle.api.MojangSkinAPI;
import dev.imb11.skinshuffle.api.data.OwnedCape;
import dev.imb11.skinshuffle.client.preset.SkinPreset;
import dev.imb11.skinshuffle.util.NetworkingUtil;
import kong.unirest.Unirest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Keeps track of the capes owned by the current account and loads cape textures for previews.
 */
public final class CapeManager {
    private static volatile List<OwnedCape> ownedCapes = List.of();
    private static volatile boolean loaded = false;
    private static volatile boolean loading = false;

    /** Cape texture URL -> registered texture id. Only touched on the render thread. */
    private static final Map<String, Identifier> TEXTURES = new HashMap<>();
    /** Cape texture URLs that are currently downloading or that failed, so we don't retry every frame. */
    private static final Set<String> PENDING_OR_FAILED = new HashSet<>();

    private CapeManager() {
    }

    public static List<OwnedCape> getOwnedCapes() {
        return ownedCapes;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static boolean isLoading() {
        return loading;
    }

    /**
     * Fetches the owned capes from the Minecraft services API, if that hasn't been done yet.
     * The callback runs on the render thread once the list is available (or failed to load).
     */
    public static void loadOwnedCapes(boolean force, Runnable onDone) {
        if (loading) return;
        if (loaded && !force) {
            onDone.run();
            return;
        }

        if (!NetworkingUtil.isLoggedIn()) {
            loaded = true;
            onDone.run();
            return;
        }

        loading = true;
        CompletableFuture.supplyAsync(MojangSkinAPI::getOwnedCapes, Util.backgroundExecutor())
                .whenComplete((capes, throwable) -> Minecraft.getInstance().execute(() -> {
                    if (throwable == null && capes != null) {
                        ownedCapes = capes;
                    }
                    loaded = true;
                    loading = false;
                    onDone.run();
                }));
    }

    /**
     * @return The cape texture for the given URL, or null while it is still downloading or if it failed to load.
     */
    @Nullable
    public static ClientAsset.Texture getTexture(@Nullable String url) {
        if (url == null) return null;

        Identifier id = TEXTURES.get(url);
        if (id != null) {
            return new CapeTexture(id);
        }

        if (PENDING_OR_FAILED.add(url)) {
            CompletableFuture.supplyAsync(() -> Unirest.get(url).asBytes().getBody(), Util.backgroundExecutor())
                    .thenAcceptAsync(bytes -> register(url, bytes), Minecraft.getInstance())
                    .exceptionally(throwable -> {
                        SkinShuffle.LOGGER.warn("Failed to download cape texture {}", url, throwable);
                        return null;
                    });
        }

        return null;
    }

    /**
     * @return The cape texture the given preset wants to display, if it has one and it has finished loading.
     */
    @Nullable
    public static ClientAsset.Texture getTexture(SkinPreset preset) {
        return preset.hasCape() ? getTexture(preset.getCapeUrl()) : null;
    }

    private static void register(String url, byte[] bytes) {
        try (var in = new ByteArrayInputStream(bytes)) {
            NativeImage image = NativeImage.read(in);
            Identifier id = SkinShuffle.id("cape/" + Math.abs(url.hashCode()));
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> id.toString(), image));
            TEXTURES.put(url, id);
        } catch (Exception e) {
            SkinShuffle.LOGGER.warn("Failed to read cape texture {}", url, e);
        }
    }

    private record CapeTexture(Identifier id) implements ClientAsset.Texture {
        @Override
        public Identifier texturePath() {
            return id;
        }
    }
}

package dev.imb11.skinshuffle.client.config;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.imb11.skinshuffle.SkinShuffle;
import dev.imb11.skinshuffle.api.MojangSkinAPI;
import dev.imb11.skinshuffle.api.SkinShuffleAPI;
import dev.imb11.skinshuffle.api.data.SkinQueryResult;
import dev.imb11.skinshuffle.client.preset.SkinPreset;
import dev.imb11.skinshuffle.client.skin.ConfigSkin;
import dev.imb11.skinshuffle.client.skin.ResourceSkin;
import dev.imb11.skinshuffle.client.skin.UrlSkin;
import dev.imb11.skinshuffle.networking.ClientSkinHandling;
import dev.imb11.skinshuffle.util.NetworkingUtil;
import dev.imb11.skinshuffle.util.SkinCacheRegistry;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.core.ClientAsset;
import net.minecraft.client.Minecraft;

public class SkinPresetManager {
    public static final Path PERSISTENT_SKINS_DIR = SkinShuffle.DATA_DIR.resolve("skins");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<SkinPreset> loadedPresets = new ArrayList<>();
    public static boolean LOADING_LOCK = false;
    private static Path PRESETS = SkinShuffle.DATA_DIR.resolve("presets.json");
    private static SkinPreset chosenPreset = null;
    private static SkinPreset apiPreset = null;
    private static SkinShuffleAPI SKIN_SHUFFLE_API;
    private static final ExecutorService REFRESH_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "SkinShuffle-Refresh");
        thread.setDaemon(true);
        return thread;
    });
    private static final ExecutorService APPLY_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "SkinShuffle-Apply");
        thread.setDaemon(true);
        return thread;
    });

    public static List<SkinPreset> getLoadedPresets() {
        return Collections.unmodifiableList(loadedPresets);
    }

    public static SkinPreset getChosenPreset() {
        if (chosenPreset == null) {
            if (!loadedPresets.isEmpty()) {
                chosenPreset = loadedPresets.get(0);
            } else {
                chosenPreset = SkinPreset.generateDefaultPreset();
                loadedPresets.add(chosenPreset);
            }
            savePresets();
        }
        return chosenPreset;
    }

    public static SkinPreset getApiPreset() {
        return apiPreset;
    }

    public static void setApiPreset(SkinPreset preset) {
        apiPreset = preset;
    }

    /**
     * Gets a preset by its keybind ID.
     *
     * @param keybindId The keybind ID to look for
     * @return Optional containing the preset if found, empty otherwise
     */
    public static Optional<SkinPreset> getPresetByKeybindId(int keybindId) {
        if (keybindId < 0) return Optional.empty();

        return loadedPresets.stream()
                .filter(preset -> preset.getKeybindId() == keybindId)
                .findFirst();
    }

    /**
     * Apply a skin preset by keybind ID. Used when hotkeys are pressed.
     *
     * @param keybindId The keybind ID of the preset to apply
     * @return true if a preset was found and applied, false otherwise
     */
    public static boolean applyPresetByKeybindId(int keybindId) {
        Optional<SkinPreset> preset = getPresetByKeybindId(keybindId);
        if (preset.isPresent()) {
            setChosenPreset(preset.get(), true);
            return true;
        }
        return false;
    }

    public static void swapPresets(int index1, int index2) {
        if (index1 < 0 || index2 < 0 || index1 >= loadedPresets.size() || index2 >= loadedPresets.size()) {
            return;
        }
        Collections.swap(loadedPresets, index1, index2);
        savePresets();
    }

    public static void setChosenPreset(SkinPreset preset, boolean ignoreMatch) {
        if (chosenPreset == preset && !ignoreMatch) return;
        chosenPreset = preset;
        savePresets();
        apply();
    }

    public static void savePresets() {
        JsonObject presetFile = new JsonObject();
        presetFile.addProperty("chosenPreset", loadedPresets.indexOf(chosenPreset));
        presetFile.addProperty("apiPreset", apiPreset == null ? -1 : loadedPresets.indexOf(apiPreset));

        JsonArray array = new JsonArray();
        for (SkinPreset loadedPreset : loadedPresets) {
            DataResult<JsonElement> dataResult = SkinPreset.CODEC.encodeStart(JsonOps.INSTANCE, loadedPreset);
            array.add(dataResult.result().orElseThrow(() -> new RuntimeException("Failed to encode skin preset.")));
        }
        presetFile.add("loadedPresets", array);

        String jsonString = GSON.toJson(presetFile);
        try {
            Files.writeString(PRESETS, jsonString, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasLoadedPresets() {
        return chosenPreset != null;
    }

    public static Path getAccountPresetsPath(String username) {
        return SkinShuffle.DATA_DIR.resolve("presets-" + username + ".json");
    }

    public static Path getGlobalPresetsPath() {
        return SkinShuffle.DATA_DIR.resolve("presets.json");
    }

    public static void loadPresets() {
        if (LOADING_LOCK) return;
        LOADING_LOCK = true;

        SKIN_SHUFFLE_API = new SkinShuffleAPI(SkinShuffleConfig.get().mineskinProxyDomain);

        if (SkinShuffleConfig.get().enableMultiAccountSupport) {
            var username = Minecraft.getInstance().getGameProfile().name();
            PRESETS = getAccountPresetsPath(username);
        } else {
            PRESETS = getGlobalPresetsPath();
        }

        if (!Files.exists(PRESETS)) {
            if (chosenPreset == null) {
                chosenPreset = SkinPreset.generateDefaultPreset();
                apiPreset = chosenPreset;
                loadedPresets.add(chosenPreset);
            }
            savePresets();
        }

        loadedPresets.clear();
        chosenPreset = null;

        try {
            String jsonString = Files.readString(PRESETS);
            JsonObject presetFile = GSON.fromJson(jsonString, JsonObject.class);
            int chosenPresetIndex = presetFile.get("chosenPreset").getAsInt();
            int apiPresetIndex = presetFile.has("apiPreset") ? presetFile.get("apiPreset").getAsInt() : -1;

            JsonArray array = presetFile.get("loadedPresets").getAsJsonArray();
            for (JsonElement jsonElement : array) {
                DataResult<Pair<SkinPreset, JsonElement>> dataResult = SkinPreset.CODEC.decode(JsonOps.INSTANCE, jsonElement);
                Pair<SkinPreset, JsonElement> pair = dataResult.result().orElseThrow(() -> new RuntimeException("Failed to decode skin preset."));
                SkinPreset preset = pair.getFirst();
                loadedPresets.add(preset);
            }

            // Validate keybind IDs for uniqueness
            validateAndFixKeybindIds();

            chosenPreset = loadedPresets.get(chosenPresetIndex);
            apiPreset = apiPresetIndex < 0 ? null : loadedPresets.get(apiPresetIndex);
        } catch (Exception e) {
            SkinShuffle.LOGGER.error("Failed to load presets, resetting!");
            if (chosenPreset == null) {
                chosenPreset = SkinPreset.generateDefaultPreset();
                apiPreset = chosenPreset;
                loadedPresets.add(chosenPreset);
            }
            savePresets();
        } finally {
            LOADING_LOCK = false;
        }
    }

    /**
     * Validates that all keybind IDs are unique (except for -1 which means "no keybind")
     * and fixes any duplicates by resetting them to -1.
     */
    private static void validateAndFixKeybindIds() {
        // Skip validation if there are no presets
        if (loadedPresets.isEmpty()) return;

        // Track used keybind IDs
        List<Integer> usedKeybindIds = new ArrayList<>();
        boolean hasChanges = false;

        for (SkinPreset preset : loadedPresets) {
            int keybindId = preset.getKeybindId();

            // Skip -1 (no keybind) values
            if (keybindId == -1) continue;

            // If this keybind ID is already used, reset it to -1
            if (usedKeybindIds.contains(keybindId)) {
                SkinShuffle.LOGGER.warn("Duplicate keybind ID detected: " + keybindId + ". Resetting to -1 for preset: " + preset.getName());
                preset.setKeybindId(-1);
                hasChanges = true;
            } else {
                usedKeybindIds.add(keybindId);
            }
        }

        // Save changes if any duplicates were fixed
        if (hasChanges) {
            savePresets();
        }
    }

    public static void setup() {
        try {
            if (!Files.exists(PERSISTENT_SKINS_DIR)) Files.createDirectories(PERSISTENT_SKINS_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addPreset(SkinPreset preset) {
        loadedPresets.add(preset);
        savePresets();
    }

    public static void deletePreset(SkinPreset skinPreset) {
        loadedPresets.remove(skinPreset);
        if (chosenPreset == skinPreset && !loadedPresets.isEmpty()) {
            chosenPreset = loadedPresets.getFirst();
        }
        savePresets();
    }

    /**
     * Applies the chosen preset when the game launches. Respects the "apply on startup" option, and never
     * uploads a built-in default (Steve/Alex) skin, which would otherwise overwrite the account's real skin
     * when the preset list was regenerated or the real skin could not be fetched.
     */
    /**
     * Picks a random preset to use, preferring presets that have a keybind ID (so users can choose which presets
     * take part in the shuffle). Falls back to all presets if none have a keybind. Does not apply the result.
     *
     * @return true if a different preset was chosen.
     */
    public static boolean shuffleChosenPreset() {
        if (loadedPresets.size() < 2) return false;

        List<SkinPreset> pool = loadedPresets.stream().filter(preset -> preset.getKeybindId() >= 0).toList();
        if (pool.isEmpty()) pool = loadedPresets;

        SkinPreset picked = pool.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(pool.size()));
        if (picked == chosenPreset) return false;

        chosenPreset = picked;
        savePresets();
        return true;
    }

    public static void applyOnStartup() {
        if (SkinShuffleConfig.get().shufflePresetOnStartup) {
            shuffleChosenPreset();
        }

        if (!SkinShuffleConfig.get().applyPresetOnStartup) {
            SkinShuffle.LOGGER.info("Skipping startup skin preset application due to user preference.");
            return;
        }

        if (getChosenPreset().getSkin() instanceof ResourceSkin) {
            SkinShuffle.LOGGER.info("Skipping startup skin preset application: the chosen preset is a built-in default skin.");
            return;
        }

        apply();
    }

    /**
     * Equips (or hides) the cape chosen by the preset on the account. Presets that don't manage the cape,
     * such as ones created before cape support, leave the account's cape untouched.
     * Blocks on network I/O, so only call this off the render thread.
     */
    private static void applyCape(SkinPreset preset) {
        String capeId = preset.getCapeId();
        if (capeId == null) return;

        MojangSkinAPI.setActiveCape(capeId.equals(SkinPreset.NO_CAPE) ? null : capeId);
    }

    private static void refreshServerSkin(Minecraft client, SkinPreset preset, ConfigSkin configSkin) {
        try {
            String cachedURL = SkinCacheRegistry.getCachedUploadedSkin(configSkin.getFile().toFile());

            SkinQueryResult result;
            if (cachedURL != null) {
                result = SKIN_SHUFFLE_API.uploadUrlSkin(cachedURL, preset.getSkin().getModel());
            } else {
                result = SKIN_SHUFFLE_API.uploadFileSkin(configSkin.getFile(), preset.getSkin().getModel());
            }

            if (result == null) {
                throw new Exception("Failed to upload skin to MineSkin proxy.");
            }

            client.execute(() -> {
                ClientAsset.DownloadedTexture cape = configSkin.getSkinTextures().cape() instanceof ClientAsset.DownloadedTexture downloaded ? downloaded : null;
                ClientSkinHandling.sendRefresh(result, Optional.ofNullable(cape));
            });
        } catch (Exception e) {
            SkinShuffle.LOGGER.error("Failed to refresh the skin on the server due to {}", e.toString());
        }
    }

    public static void apply() {
        Minecraft client = Minecraft.getInstance();
        SkinPreset preset = getChosenPreset();

        if (SkinShuffleConfig.get().disableAPIUpload) {
            SkinShuffle.LOGGER.info("Skipping skin preset application due to user preference.");
            return;
        }

        if (!NetworkingUtil.isLoggedIn()) {
            SkinShuffle.LOGGER.warn("Skipping skin preset application due to offline mode being active.");
            return;
        }

        // Everything below talks to the network (downloading the skin, the Mojang API and the MineSkin proxy),
        // so it runs off the render thread on a single worker. That keeps the game from freezing on slow or
        // proxied connections, and keeps rapid successive applies in order.
        APPLY_EXECUTOR.execute(() -> {
            try {
                ConfigSkin configSkin = preset.getSkin().saveToConfig();

                try {
                    boolean successful;
                    if (preset.getSkin() instanceof UrlSkin urlSkin) {
                        successful = MojangSkinAPI.setSkinTexture(urlSkin.getUrl(), urlSkin.getModel());
                    } else {
                        successful = false;
                    }
                    // Skin-by-URL can be rejected by Mojang, so fall back to uploading the downloaded skin file.
                    if (!successful) {
                        successful = MojangSkinAPI.setSkinTexture(configSkin.getFile().toFile(), configSkin.getModel());
                    }
                    if (successful) client.execute(() -> setApiPreset(preset));
                    applyCape(preset);

                    // The MineSkin proxy is only needed to tell a server that has SkinShuffle installed about the
                    // new skin. It can be slow (or time out), so it runs on its own worker and never delays the
                    // Mojang skin/cape updates above.
                    if (client.level != null && ClientSkinHandling.isInstalledOnServer()) {
                        REFRESH_EXECUTOR.execute(() -> refreshServerSkin(client, preset, configSkin));
                    }
                } catch (Exception e) {
                    SkinShuffle.LOGGER.error("Failed to apply skin preset.", e);
                }
            } catch (Exception ignored) {
                SkinShuffle.LOGGER.info("Skipping skin preset application due to skin not being fully loaded. If this is first startup, please ignore this message.");
            }
        });

        savePresets();
    }
}

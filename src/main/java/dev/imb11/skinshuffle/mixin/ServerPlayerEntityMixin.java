package dev.imb11.skinshuffle.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import dev.imb11.skinshuffle.networking.ServerSkinHandling;
import dev.imb11.skinshuffle.util.SkinShufflePlayer;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Most mixin methods in this class are credit to FabricTailor and various mods that are licensed under LGPL or GPL.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player implements SkinShufflePlayer {
    @Shadow
    public ServerGamePacketListenerImpl connection;
    @Shadow
    @Final
    public ServerPlayerGameMode gameMode;

    public ServerPlayerEntityMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow
    public abstract void onUpdateAbilities();

    @Shadow
    public abstract boolean hasDisconnected();

    @Shadow
    public abstract ServerLevel level();

    @Shadow
    @Final
    private MinecraftServer server;

    /**
     * @author Pyrofab
     * <p>
     * This method has been adapted from the Impersonate mod's <a href="https://github.com/Ladysnake/Impersonate/blob/1.16/src/main/java/io/github/ladysnake/impersonate/impl/ServerPlayerSkins.java">source code</a>
     * under GNU Lesser General Public License.
     * <p>
     * Reloads player's skin for all the players (including the one that has changed the skin)
     * </p>
     */
    @Override
    public void skinShuffle$refreshSkin() {
        if (this.hasDisconnected()) return;

        // Refreshing in tablist for each player
        PlayerList playerManager = this.server.getPlayerList();
        playerManager.broadcastAll(new ClientboundPlayerInfoRemovePacket(new ArrayList<>(Collections.singleton(this.getUUID()))));
        playerManager.broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(Collections.singleton((ServerPlayer) (Object) this)));

        ServerChunkCache manager = this.level().getChunkSource();

        var storage = manager.chunkMap;
        var trackerEntry = storage.entityMap.get(this.getId());

        // Refreshing skin in world for all that see the player
        trackerEntry.seenBy.forEach(tracking -> {
            if (!ServerSkinHandling.attemptPlayerListEntryRefresh(tracking.getPlayer(), this.getId())) {
                trackerEntry.serverEntity.addPairing(tracking.getPlayer());
            }
        });

        if (!ServerSkinHandling.attemptPlayerListEntryRefresh((ServerPlayer) (Object) this, this.getId())) {
            // If we could not send refresh packet, we change the player entity on the client
            ServerLevel level = this.level();

            this.connection.send(new ClientboundRespawnPacket(

                    new CommonPlayerSpawnInfo(
                            level.dimensionTypeRegistration(),
                            level.dimension(),
                            BiomeManager.obfuscateSeed(level.getSeed()),
                            this.gameMode.getGameModeForPlayer(),
                            this.gameMode.getPreviousGameModeForPlayer(),
                            level.isDebug(),
                            level.isFlat(),
                            this.getLastDeathLocation(),
                            this.getPortalCooldown(),
                            level.getSeaLevel()
                    ), (byte) 3)
            );


            this.connection.send(new ClientboundPlayerPositionPacket(0, PositionMoveRotation.of(this), Collections.emptySet()));
            this.connection.send(new ClientboundSetHeldSlotPacket(this.getInventory().getSelectedSlot()));
            this.connection.send(new ClientboundChangeDifficultyPacket(level.getDifficulty(), level.getLevelData().isDifficultyLocked()));
            this.connection.send(new ClientboundSetExperiencePacket(this.experienceProgress, this.totalExperience, this.experienceLevel));

            playerManager.sendLevelInfo((ServerPlayer) (Object) this, level);
            playerManager.sendPlayerPermissionLevel((ServerPlayer) (Object) this);

            this.connection.send(new ClientboundSetHealthPacket(this.getHealth(), this.getFoodData().getFoodLevel(), this.getFoodData().getSaturationLevel()));

            for (MobEffectInstance statusEffect : this.getActiveEffects()) {
                this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), statusEffect, false));
            }

            var equipmentList = new ArrayList<Pair<EquipmentSlot, ItemStack>>();
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                ItemStack itemStack = this.getItemBySlot(equipmentSlot);
                if (!itemStack.isEmpty()) {
                    equipmentList.add(new Pair<>(equipmentSlot, itemStack.copy()));
                }
            }

            if (!equipmentList.isEmpty()) {
                this.connection.send(new ClientboundSetEquipmentPacket(this.getId(), equipmentList));
            }

            if (!this.getPassengers().isEmpty()) {
                this.connection.send(new ClientboundSetPassengersPacket(this));
            }
            if (this.isPassenger()) {
                this.connection.send(new ClientboundSetPassengersPacket(this.getVehicle()));
            }

            this.onUpdateAbilities();
            playerManager.sendAllPlayerInfo((ServerPlayer) (Object) this);
        }
    }
}
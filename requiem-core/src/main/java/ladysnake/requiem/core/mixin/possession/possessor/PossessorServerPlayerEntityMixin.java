/*
 * Requiem
 * Copyright (C) 2017-2024 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.core.mixin.possession.possessor;

import com.mojang.authlib.GameProfile;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.event.requiem.PossessionEvents;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.MobResurrectable;
import ladysnake.requiem.core.RequiemCore;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.function.Function;

import static ladysnake.requiem.core.mixin.possession.possessor.PlayerTagKeys.*;

@Mixin(ServerPlayerEntity.class)
public abstract class PossessorServerPlayerEntityMixin extends PlayerEntity implements MobResurrectable, RequiemPlayer {
    @Nullable
    private NbtCompound requiem_possessedEntityTag;

    public PossessorServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Override
    public void setResurrectionEntity(MobEntity secondLife) {
        NbtCompound tag = new NbtCompound();
        if (secondLife.saveSelfNbt(tag)) {
            setResurrectionEntity(tag);
        } else {
            RequiemCore.LOGGER.warn("Could not serialize possessed entity {} !", secondLife);
        }
    }

    @Override
    public boolean hasResurrectionEntity() {
        return this.requiem_possessedEntityTag != null;
    }

    @Override
    public void spawnResurrectionEntity() {
        if (this.requiem_possessedEntityTag != null) {
            Entity formerPossessed = EntityType.loadEntityWithPassengers(
                this.requiem_possessedEntityTag,
                getWorld(),
                Function.identity()
            );

            if (formerPossessed instanceof MobEntity host) {
                host.copyPositionAndRotation(this);
                if (getWorld().spawnEntity(host)) {
                    if (PossessionComponent.get(this).startPossessing(host)) {
                        PossessionEvents.POST_RESURRECTION.invoker().onResurrected(((ServerPlayerEntity) (Object) this), host);
                    }
                } else {
                    RequiemCore.LOGGER.error("Failed to spawn possessed entity {}", host);
                }
            } else {
                RequiemCore.LOGGER.error("Could not recreate possessed entity {}", requiem_possessedEntityTag);
            }

            this.requiem_possessedEntityTag = null;
        }
    }

    @Unique
    private void setResurrectionEntity(@Nullable NbtCompound serializedSecondLife) {
        this.requiem_possessedEntityTag = serializedSecondLife;
    }

    @Inject(method = "teleportTo", at = @At(value = "HEAD", shift = At.Shift.AFTER))
    // Let cancelling mixins do their job
    private void changePossessedDimension(TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir) {
        prepareDimensionChange();
    }

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V", at = @At(value = "HEAD", shift = At.Shift.AFTER))
    // Let cancelling mixins do their job
    private void changePossessedDimension(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        prepareDimensionChange();
    }

    @Unique
    private void prepareDimensionChange() {
        MobEntity currentHost = PossessionComponent.getHost(this);
        if (currentHost != null && !currentHost.isRemoved()) {
            this.setResurrectionEntity(currentHost);
            currentHost.remove(RemovalReason.UNLOADED_WITH_PLAYER);
        }
    }

    @Inject(method = "teleportTo", at = @At(value = "RETURN", ordinal = 1))
    private void onTeleportDone(TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir) {
        spawnResurrectionEntity();
    }

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V", at = @At(value = "RETURN"))
    private void onTeleportDone(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        spawnResurrectionEntity();
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void clonePlayer(ServerPlayerEntity original, boolean fromEnd, CallbackInfo ci) {
        // We can safely cast a class to a mixin from that same mixin
        //noinspection ConstantConditions
        this.requiem_possessedEntityTag = ((PossessorServerPlayerEntityMixin) (Object) original).requiem_possessedEntityTag;

        if (this.requiem_possessedEntityTag != null) {
            this.getInventory().clone(original.getInventory());
        }
    }

    @Inject(method = "swingHand", at = @At("HEAD"))
    private void swingHand(Hand hand, CallbackInfo ci) {
        LivingEntity possessed = PossessionComponent.get(this).getHost();

        if (possessed != null) {
            possessed.swingHand(hand);
        }
    }

    @Inject(method = "onStatusEffectApplied", at = @At("RETURN"))
    private void onStatusEffectAdded(StatusEffectInstance effect, Entity entity, CallbackInfo ci) {
        MobEntity possessed = PossessionComponent.get(this).getHost();

        if (possessed != null) {
            possessed.addStatusEffect(new StatusEffectInstance(effect));
        }
    }

    @Inject(method = "onStatusEffectUpgraded", at = @At("RETURN"))
    private void onStatusEffectUpdated(StatusEffectInstance effect, boolean upgrade, @Nullable Entity entity, CallbackInfo ci) {
        if (upgrade) {
            MobEntity possessed = PossessionComponent.get(this).getHost();

            if (possessed != null) {
                possessed.addStatusEffect(new StatusEffectInstance(effect));
            }
        }
    }

    @Inject(method = "onStatusEffectRemoved", at = @At("RETURN"))
    private void onStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci) {
        MobEntity possessed = PossessionComponent.get(this).getHost();

        if (possessed != null) {
            possessed.removeStatusEffect(effect.getEffectType());
        }
    }


    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void writePossessedMobToTag(NbtCompound tag, CallbackInfo info) {
        Entity possessedEntity = PossessionComponent.get(this).getHost();

        if (possessedEntity != null) {
            Entity possessedEntityVehicle = possessedEntity.getRootVehicle();
            NbtCompound possessedRoot = new NbtCompound();
            NbtCompound serializedPossessed = new NbtCompound();
            possessedEntityVehicle.saveSelfNbt(serializedPossessed);
            possessedRoot.put(POSSESSED_ENTITY_TAG, serializedPossessed);
            possessedRoot.putUuid(POSSESSED_UUID_TAG, possessedEntity.getUuid());
            tag.put(POSSESSED_ROOT_TAG, possessedRoot);
        } else if (this.requiem_possessedEntityTag != null) {
            NbtCompound possessedRoot = new NbtCompound();
            possessedRoot.put(POSSESSED_ENTITY_TAG, this.requiem_possessedEntityTag);
            possessedRoot.putUuid(POSSESSED_UUID_TAG, this.requiem_possessedEntityTag.getUuid("UUID"));
            tag.put(POSSESSED_ROOT_TAG, possessedRoot);
        }
    }
}

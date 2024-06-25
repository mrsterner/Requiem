/*
 * Requiem
 * Copyright (C) 2017-2024 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package org.ladysnake.vaquero.impl.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.vaquero.api.ExternalJumpingMount;
import org.ladysnake.vaquero.api.events.JumpingMountEvents;
import org.ladysnake.vaquero.api.events.MobTravelRidingCallback;
import org.ladysnake.vaquero.impl.mixin.PossessionRidingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntityMixin {
    /**
     *  Warning: this injector is very fragile, as it gets overridden in every rideable entity - redundant WrapOperations are done in important places
     */
    @Inject(method = "getPrimaryPassenger", at = @At("RETURN"), cancellable = true)
    private void getPrimaryPassenger(CallbackInfoReturnable<LivingEntity> cir) {
        if (cir.getReturnValue() == null) {
            LivingEntity actualRider = PossessionRidingHelper.getRider((Entity) (Object) this, cir.getReturnValue());
            if (actualRider != null) {
                cir.setReturnValue(actualRider);
            }
        }
    }

    @Override
    protected Vec3d requiem$travelStart(Vec3d movementInput) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (JumpingMountEvents.FIND_ENTITY_JUMP.invoker().findJumpingMount(self) instanceof ExternalJumpingMount jumpingMount) {
            jumpingMount.attemptJump();
        }

        return movementInput;
    }

    @Override
    protected @Nullable LivingEntity requiem$enablePossessionRiding(Entity instance, Operation<LivingEntity> original) {
        return PossessionRidingHelper.getRider(instance, original.call(instance));
    }

    @Override
    protected void requiem$getRiddenSpeed(PlayerEntity player, CallbackInfoReturnable<Float> cir) {
        if (MobTravelRidingCallback.EVENT.invoker().canBeControlled((MobEntity) (Object) this, player)) {
            cir.setReturnValue((float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
        }
    }

    @Override
    protected void requiem$tickControlled(PlayerEntity player, Vec3d input, CallbackInfo ci) {
        this.setRotation(player.getYaw(), player.getPitch() * 0.5F);
        this.prevYaw = this.bodyYaw = this.headYaw = this.getYaw();
    }
}

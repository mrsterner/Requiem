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
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.vaquero.impl.mixin.PossessionRidingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin {
    @Shadow
    public abstract double getAttributeValue(EntityAttribute attribute);

    @Shadow
    public float bodyYaw;

    @Shadow
    public float headYaw;

    @WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getPrimaryPassenger()Lnet/minecraft/entity/LivingEntity;"))
    private @Nullable LivingEntity possessionRiding(LivingEntity instance, Operation<LivingEntity> original) {
        return PossessionRidingHelper.getRider(instance, original.call(instance));
    }

    @WrapOperation(method = "travelControlled", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getControlledMovementInput(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d getControlledMovement(LivingEntity instance, PlayerEntity player, Vec3d input, Operation<Vec3d> original) {
        return PossessionRidingHelper.getControlledMovement(instance, player, original.call(instance, player, input));
    }

    @ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true)
    protected Vec3d requiem$travelStart(Vec3d movementInput) {
        // overridden in MobEntityMixin
        return movementInput;
    }

    @Inject(method = "travelControlled", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tickControlled(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;)V", shift = At.Shift.AFTER))
    protected void requiem$tickControlled(PlayerEntity player, Vec3d input, CallbackInfo ci) {
        // Overridden in MobEntityMixin
    }

    @Inject(method = "getControlledMovementInput", at = @At("RETURN"), cancellable = true)
    private void getControlledMovement(PlayerEntity player, Vec3d input, CallbackInfoReturnable<Vec3d> cir) {
        if (cir.getReturnValue() == null) {
            Vec3d updated = PossessionRidingHelper.getControlledMovement((LivingEntity) (Object) this, player, null);
            if (updated != null) {
                cir.setReturnValue(updated);
            }
        }
    }

    @SuppressWarnings("CancellableInjectionUsage")
    @Inject(method = "getRiddenSpeed", at = @At("RETURN"), cancellable = true)
    protected void requiem$getRiddenSpeed(PlayerEntity player, CallbackInfoReturnable<Float> cir) {
        // Overridden in MobEntityMixin
    }
}

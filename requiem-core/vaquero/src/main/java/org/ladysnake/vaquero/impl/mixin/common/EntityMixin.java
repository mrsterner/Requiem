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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract World getWorld();

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    public float prevYaw;

    @Shadow
    public abstract float getYaw();

    // Overridden in MobEntityMixin
    @WrapOperation(method = {"isLogicalSideForUpdatingMovement", "getControlledVehicle"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getPrimaryPassenger()Lnet/minecraft/entity/LivingEntity;"))
    protected @Nullable LivingEntity requiem$enablePossessionRiding(Entity instance, Operation<LivingEntity> original) {
        return original.call(instance);
    }
}

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
package org.ladysnake.vaquero.impl.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.JumpingMount;
import org.ladysnake.vaquero.api.ExternalJumpingMount;
import org.ladysnake.vaquero.api.events.JumpingMountEvents;
import org.ladysnake.vaquero.impl.mixin.common.LivingEntityAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "getJumpingMount", at = @At("RETURN"), cancellable = true)
    private void hackJumpingMount(CallbackInfoReturnable<JumpingMount> cir) {
        if (cir.getReturnValue() == null && this.getVehicle() == null) {
            JumpingMount jumpingMount = JumpingMountEvents.FIND_PLAYER_JUMP.invoker().findJumpingMount(this);
            if (jumpingMount != null && jumpingMount.canJump()) {
                cir.setReturnValue(jumpingMount);
            }
        }
    }

    @ModifyVariable(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/JumpingMount;getJumpCooldown()I"), allow = 1)
    private JumpingMount swapJumpingMount(JumpingMount mount) {
        if (mount instanceof ExternalJumpingMount) {
            // prevent normal jumps
            ((LivingEntityAccessor) this).setJumpingCooldown(10);
        }
        return mount;
    }
}

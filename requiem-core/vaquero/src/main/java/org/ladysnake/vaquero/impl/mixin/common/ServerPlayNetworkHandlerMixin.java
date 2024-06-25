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
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.vaquero.api.events.JumpingMountEvents;
import org.ladysnake.vaquero.impl.mixin.PossessionRidingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @WrapOperation(method = "onVehicleMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getPrimaryPassenger()Lnet/minecraft/entity/LivingEntity;"))
    private LivingEntity enableRiding(Entity instance, Operation<LivingEntity> original) {
        return PossessionRidingHelper.getRider(instance, original.call(instance));
    }

    @Inject(method = "onClientCommand", at = @At("HEAD"))
    private void swapJumpingMount(ClientCommandC2SPacket packet, CallbackInfo ci) {
        if (packet.getMode() == ClientCommandC2SPacket.Mode.START_RIDING_JUMP || packet.getMode() == ClientCommandC2SPacket.Mode.STOP_RIDING_JUMP) {
            if (!(this.player.getVehicle() instanceof JumpingMount)) {
                JumpingMount jumpingMount = JumpingMountEvents.FIND_ENTITY_JUMP.invoker().findJumpingMount(player);
                if (jumpingMount != null) {
                    if (packet.getMode() == ClientCommandC2SPacket.Mode.START_RIDING_JUMP) {
                        int i = packet.getMountJumpHeight();
                        if (jumpingMount.canJump() && i > 0) {
                            jumpingMount.startJumping(i);
                        }
                    } else {
                        jumpingMount.stopJumping();
                    }
                }
            }
        }
    }
}

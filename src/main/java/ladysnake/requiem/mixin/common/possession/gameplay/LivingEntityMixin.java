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
package ladysnake.requiem.mixin.common.possession.gameplay;

import ladysnake.requiem.api.v1.internal.ProtoPossessable;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.common.advancement.criterion.RequiemCriteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements Possessable {
    private @Nullable UUID requiem$previousPossessorUuid;

    @Inject(method = "damage",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;increaseStat(Lnet/minecraft/util/Identifier;I)V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/PlayerHurtEntityCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;FFZ)V")),
        at = @At(value = "JUMP", opcode = Opcodes.IFEQ),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        allow = 1
    )
    private void triggerCriterion(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, float dealt, boolean blocked, float attackAngle, boolean playDamageEffects, @Nullable Entity attacker, boolean didDamage) {
        if (attacker != null) {
            PlayerEntity possessor = ((ProtoPossessable) attacker).getPossessor();
            if (possessor instanceof ServerPlayerEntity) {
                RequiemCriteria.POSSESSED_HIT_ENTITY.handle(((ServerPlayerEntity) possessor), attacker,  (Entity) (Object) this, source, dealt, amount, blocked);
            }
        }
    }

    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;drop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)V", shift = At.Shift.AFTER))
    private void onDeath(DamageSource deathCause, CallbackInfo ci) {
        if (!this.isBeingPossessed() && this.requiem$previousPossessorUuid != null) {
            PlayerEntity previousPossessor = this.getWorld().getPlayerByUuid(this.requiem$previousPossessorUuid);

            if (previousPossessor != null) {
                RequiemCriteria.DEATH_AFTER_POSSESSION.handle((ServerPlayerEntity) previousPossessor, (Entity) (Object) this, deathCause);
            }
        }
    }

    @Inject(method = "sleep", at = @At("RETURN"))
    protected void requiem$sleep(BlockPos pos, CallbackInfo ci) {
        // NO-OP
    }

    @Inject(method = "wakeUp", at = @At("RETURN"))
    protected void requiem$wakeUp(CallbackInfo ci) {
        // NO-OP
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageSource;getAttacker()Lnet/minecraft/entity/Entity;"))
    public void requiem$damaged(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        //Overridden
    }

    @Inject(method = "pushAway", at = @At("RETURN"))
    public void requiem$pushed(Entity entity, CallbackInfo ci) {
        //Overridden
    }

}

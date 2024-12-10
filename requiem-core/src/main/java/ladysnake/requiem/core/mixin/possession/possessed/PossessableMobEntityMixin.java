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
package ladysnake.requiem.core.mixin.possession.possessed;

import ladysnake.requiem.api.v1.event.minecraft.MobConversionCallback;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MobEntity.class)
public abstract class PossessableMobEntityMixin extends PossessableLivingEntityMixin implements Possessable {

    @Shadow
    public abstract void setAttacking(boolean boolean_1);

    @Shadow
    public abstract boolean isAttacking();

    @Shadow
    protected abstract void mobTick();

    @Unique
    private int attackingCountdown;

    public PossessableMobEntityMixin(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void requiem$mobTick() {
        // for some reason minecraft uses "mob tick" instead of "mobTick", so we do the same
        this.getWorld().getProfiler().push("mob tick");
        this.mobTick();
        this.getWorld().getProfiler().pop();
    }

    @Inject(method = "setAttacking", at = @At("RETURN"))
    private void resetAttackMode(boolean attacking, CallbackInfo ci) {
        if (attacking && this.isBeingPossessed()) {
            attackingCountdown = 100;
        }
    }

    @Inject(method = "tickMovement", at = @At("RETURN"))
    private void resetAttackMode(CallbackInfo ci) {
        if (this.isAttacking() && !this.isUsingItem() && this.isBeingPossessed()) {
            this.attackingCountdown--;
            if (this.attackingCountdown == 0) {
                this.setAttacking(false);
            }
        }
    }

    @Inject(method = "getArmorItems", at = @At("HEAD"), cancellable = true)
    private void getArmorItems(CallbackInfoReturnable<Iterable<ItemStack>> cir) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null) {
            cir.setReturnValue(possessor.getArmorItems());
        }
    }

    @Inject(method = "getHandItems", at = @At("HEAD"), cancellable = true)
    private void getItemsHand(CallbackInfoReturnable<Iterable<ItemStack>> cir) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null) {
            cir.setReturnValue(possessor.getHandItems());
        }
    }

    @Inject(method = "getEquippedStack", at = @At("HEAD"), cancellable = true)
    private void getEquippedStack(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null) {
            cir.setReturnValue(possessor.getEquippedStack(slot));
        }
    }

    @Inject(method = "equipStack", at = @At("HEAD"), cancellable = true)
    private void setEquippedStack(EquipmentSlot slot, ItemStack item, CallbackInfo ci) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null && !getWorld().isClient) {
            possessor.equipStack(slot, item);
            ci.cancel();
        }
    }

    @Inject(method = "canPickupItem", at = @At("HEAD"), cancellable = true)
    private void cannotPickupItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (this.isBeingPossessed()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canGather", at = @At("HEAD"), cancellable = true)
    private void cannotGather(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (this.isBeingPossessed()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "convertTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private <T extends MobEntity> void possessConvertedZombie(EntityType<T> type, boolean bl, CallbackInfoReturnable<T> ci, T converted) {
        MobConversionCallback.EVENT.invoker().onMobConverted((MobEntity) (Object) this, converted);
    }
/* TODO
    @Inject(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;getHoldingEntity()Lnet/minecraft/entity/Entity;"), cancellable = true)
    private void detachFromPossessedEntity(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        Entity holdingEntity = this.getHoldingEntity();
        // reminder that in most cases the entity is unleashed and the player is not possessing anything
        if (holdingEntity != null && holdingEntity == PossessionComponent.getHost(player)) {
            this.detachLeash(true, !player.getAbilities().creativeMode);
            cir.setReturnValue(ActionResult.success(this.getWorld().isClient));
        }
    }

    @ModifyVariable(method = "attachLeash", at = @At("HEAD"), argsOnly = true)
    private Entity attachToPossessedEntity(Entity original) {
        MobEntity host = PossessionComponent.getHost(original);
        if (host != null) {
            return host;
        }
        return original;
    }

 */
}

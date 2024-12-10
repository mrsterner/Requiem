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

import net.minecraft.item.BowItem;
import net.minecraft.item.RangedWeaponItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BowItem.class)
public abstract class BowItemMixin extends RangedWeaponItem {

    public BowItemMixin(Settings settings) {
        super(settings);
    }
/*
    @ModifyExpressionValue(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getProjectileType(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"))
    private boolean giveSkeletonInfinity(boolean infinity, ItemStack item, World world, LivingEntity user, int charge) {
        //TODO move to playerEntity getProjectile
        MobEntity possessed = PossessionComponent.getHost(user);
        if (item.contains(VanillaRequiemPlugin.INFINITY_SHOT_TAG) && Boolean.TRUE.equals(item.get(VanillaRequiemPlugin.INFINITY_SHOT_TAG))) {
            item.remove(VanillaRequiemPlugin.INFINITY_SHOT_TAG);
            return true;
        } else if (possessed instanceof AbstractSkeletonEntity) {
            return infinity || world.getRandom().nextFloat() < 0.8f;
        }
        return infinity;
    }

    @WrapOperation(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArrowItem;createArrow(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;"))
    private PersistentProjectileEntity useSkeletonArrow(ArrowItem instance, World world, ItemStack stack, LivingEntity shooter, Operation<PersistentProjectileEntity> original) {
        LivingEntity possessed = PossessionComponent.getHost(shooter);
        if (possessed instanceof ArrowShooter) {
            return ((ArrowShooter)possessed).requiem$invokeCreateArrow(((ProjectileEntityAccessor)original.call(instance, world, stack, shooter)).requiem$invokeAsItemStack(), 1f);
        }
        return original.call(instance, world, stack, shooter);
    }

 */
}

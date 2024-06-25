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
package org.ladysnake.vaquero.impl.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.vaquero.api.events.MobTravelRidingCallback;

public final class PossessionRidingHelper {
    public static @Nullable LivingEntity getRider(Entity instance, @Nullable LivingEntity vanillaRider) {
        if (vanillaRider == null) {
            if (instance instanceof MobEntity mob && instance.getFirstPassenger() instanceof LivingEntity livingPassenger) {
                if (MobTravelRidingCallback.EVENT.invoker().canBeControlled(mob, livingPassenger)) {
                    return livingPassenger;
                }
            }
        }
        return vanillaRider;
    }

    @Contract("_, _, !null -> !null")
    public static @Nullable Vec3d getControlledMovement(LivingEntity vehicle, PlayerEntity rider, @Nullable Vec3d input) {
        if (vehicle instanceof MobEntity mob && MobTravelRidingCallback.EVENT.invoker().canBeControlled(mob, rider)) {
            return new Vec3d(rider.sidewaysSpeed, rider.upwardSpeed, rider.forwardSpeed);
        }
        return input;
    }
}

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
package org.ladysnake.vaquero.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.registry.tag.EntityTypeTags;
import org.apiguardian.api.API;
import org.ladysnake.vaquero.impl.VaqueroEntityTypeTags;

@API(status = API.Status.EXPERIMENTAL, since = "2.0.0")
public enum MobRidingType {
    DEFAULT, MOUNT, RIDE;

    public boolean canMount() {
        return this != DEFAULT;
    }

    public boolean canSteer() {
        return this == RIDE;
    }

    public static MobRidingType get(Entity entity, LivingEntity possessed) {
        if (entity instanceof SpiderEntity) {
            return possessed instanceof SkeletonEntity ? MOUNT : DEFAULT;
        } else if (entity instanceof RavagerEntity) {
            return possessed.getType().isIn(EntityTypeTags.RAIDERS) ? RIDE : DEFAULT;
        } else if (entity instanceof ChickenEntity) {
            return possessed.getType().isIn(VaqueroEntityTypeTags.ZOMBIES) && possessed.isBaby() ? RIDE : DEFAULT;
        } else if (entity instanceof StriderEntity) {
            return possessed.getType() == EntityType.ZOMBIFIED_PIGLIN || possessed.getType().isIn(VaqueroEntityTypeTags.PIGLINS) ? RIDE : DEFAULT;
        }

        return DEFAULT;
    }
}

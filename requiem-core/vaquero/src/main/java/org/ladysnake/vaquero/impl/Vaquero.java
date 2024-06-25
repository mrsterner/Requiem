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
package org.ladysnake.vaquero.impl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.ladysnake.vaquero.api.ExternalJumpingMount;
import org.ladysnake.vaquero.api.events.JumpingMountEvents;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public final class Vaquero implements ModInitializer {
    public static Identifier id(String path) {
        return new Identifier("vaquero", path);
    }

    @Override
    public void onInitialize(ModContainer mod) {
        JumpingMountEvents.FIND_ENTITY_JUMP.register(ExternalJumpingMount.KEY::getNullable);
        JumpingMountEvents.FIND_PLAYER_JUMP.register(player -> {
            Entity controlledVehicle = player.getControlledVehicle();
            return controlledVehicle instanceof LivingEntity living ? JumpingMountEvents.FIND_ENTITY_JUMP.invoker().findJumpingMount(living) : null;
        });
    }
}

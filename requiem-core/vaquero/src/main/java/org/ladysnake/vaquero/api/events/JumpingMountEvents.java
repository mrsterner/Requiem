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
package org.ladysnake.vaquero.api.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class JumpingMountEvents {
    /**
     * Finds the {@link JumpingMount} associated with an entity
     */
    public static final Event<FindEntityJumpingMountCallback> FIND_ENTITY_JUMP = EventFactory.createArrayBacked(FindEntityJumpingMountCallback.class, callbacks -> entity -> {
        for (FindEntityJumpingMountCallback callback : callbacks) {
            JumpingMount mount = callback.findJumpingMount(entity);
            if (mount != null) {
                return mount;
            }
        }
        return null;
    });

    /**
     * Finds the {@link JumpingMount} controlled by the given player
     */
    public static final Event<FindPlayerJumpingMountCallback> FIND_PLAYER_JUMP = EventFactory.createArrayBacked(FindPlayerJumpingMountCallback.class, callbacks -> entity -> {
        for (FindPlayerJumpingMountCallback callback : callbacks) {
            JumpingMount mount = callback.findJumpingMount(entity);
            if (mount != null) {
                return mount;
            }
        }
        return null;
    });

    @FunctionalInterface
    public interface FindEntityJumpingMountCallback {
        @Nullable
        JumpingMount findJumpingMount(LivingEntity entity);
    }

    @FunctionalInterface
    public interface FindPlayerJumpingMountCallback {
        @Nullable
        JumpingMount findJumpingMount(PlayerEntity entity);
    }
}

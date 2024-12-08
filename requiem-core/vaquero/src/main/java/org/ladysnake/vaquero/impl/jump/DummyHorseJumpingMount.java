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
package org.ladysnake.vaquero.impl.jump;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class DummyHorseJumpingMount extends DummyJumpingMount {
    public DummyHorseJumpingMount(AbstractHorseEntity mob, SoundEvent stepSound, Function<LivingEntity, @Nullable PlayerEntity> getPlayer) {
        super(mob, -1, stepSound, getPlayer);
    }

    @Override
    protected double getBaseJumpingStrength() {
        return ((AbstractHorseEntity) this.mob).getJumpBoostVelocityModifier();
    }
}

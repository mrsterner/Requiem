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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Currently unused because setting noDrag means you have to revert it when dissociating and lazy
 * <br/>
 * But it allows doing epic jumps as a goat
 */
public class DummyNoDragJumpingMount extends DummyJumpingMount {
    public DummyNoDragJumpingMount(LivingEntity mob, SoundEvent stepSound, Function<LivingEntity, @Nullable PlayerEntity> getPlayer) {
        super(mob, 1, stepSound, getPlayer);
    }

    @Override
    protected void beginClientJump(PlayerEntity possessor) {
        super.beginClientJump(possessor);
        possessor.setNoDrag(true);
    }

    @Override
    protected void finishClientJump(PlayerEntity possessor) {
        super.finishClientJump(possessor);
        possessor.setNoDrag(false);
    }

    @Override
    public void startJumping(int height) {
        this.mob.setNoDrag(true);
        super.startJumping(height);
    }

    @Override
    public void stopJumping() {
        super.stopJumping();
        this.mob.setNoDrag(false);
    }
}

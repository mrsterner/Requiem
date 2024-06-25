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

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentFactory;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.apiguardian.api.API;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.vaquero.impl.jump.DummyJumpingMount;

import java.util.function.Function;

/**
 * A fake {@link JumpingMount} that is not implemented by a rideable entity.
 *
 * <p>Can be attached to various entities as a component
 */
@API(status = API.Status.EXPERIMENTAL, since = "2.0.0")
public interface ExternalJumpingMount extends JumpingMount, Component {
    ComponentKey<ExternalJumpingMount> KEY = ComponentRegistry.getOrCreate(new Identifier("requiem", "charged_jump"), ExternalJumpingMount.class);

    static <E extends LivingEntity> ComponentFactory<E, ExternalJumpingMount> simple(float baseJumpStrength, SoundEvent stepSound, Function<LivingEntity, @Nullable PlayerEntity> getPlayer) {
        return e -> new DummyJumpingMount(e, baseJumpStrength, stepSound, getPlayer);
    }

    /**
     * @return {@code true} if the rider should be able to initiate long jumps with this mount
     */
    @Override
    boolean canJump();

    /**
     * Called when a player releases the jump key clientside
     */
    @Override
    void setJumpStrength(int strength);

    /**
     * Called when the long jump starts serverside
     */
    @Override
    void startJumping(int height);

    /**
     * Called when the long jump stops serverside
     */
    @Override
    void stopJumping();

    /**
     * Called every tick in {@link MobEntity#travel(Vec3d)}.
     * Should start a jump if {@link #setJumpStrength(int)} has previously been called.
     */
    void attemptJump();
}

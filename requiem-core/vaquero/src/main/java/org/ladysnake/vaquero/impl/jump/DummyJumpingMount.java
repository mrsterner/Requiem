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

import com.google.common.base.Preconditions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.TransientComponent;
import org.ladysnake.vaquero.api.ExternalJumpingMount;
import org.ladysnake.vaquero.impl.mixin.common.EntityAccessor;

import java.util.function.Function;

/**
 * A fake JumpingMount that allows players possessing a mob to long jump by themselves
 */
public class DummyJumpingMount implements ExternalJumpingMount, TransientComponent {
    protected final LivingEntity mob;
    private final SoundEvent stepSound;
    private float jumpStrength;
    /**@see HorseBaseEntity#isInAir()*/
    private boolean inAir;
    private final double baseJumpStrength;
    private final Function<LivingEntity, @Nullable PlayerEntity> getPlayer;

    public DummyJumpingMount(LivingEntity mob, double baseJumpStrength, SoundEvent stepSound, Function<LivingEntity, @Nullable PlayerEntity> getPlayer) {
        this.mob = mob;
        this.baseJumpStrength = baseJumpStrength;
        this.stepSound = stepSound;
        this.getPlayer = getPlayer;
    }

    @Override
    public void setJumpStrength(int strength) {
        if (strength < 0) {
            strength = 0;
        } else {
            PlayerEntity possessor = this.getPlayer.apply(this.mob);
            if (possessor != null) {
                possessor.setJumping(true);
            }
        }

        if (strength >= 90) {
            this.jumpStrength = 1.0F;
        } else {
            this.jumpStrength = 0.4F + 0.4F * (float)strength / 90.0F;
        }
    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public void startJumping(int height) {
        this.mob.setPose(EntityPose.LONG_JUMPING);
    }

    @Override
    public void stopJumping() {
        this.mob.getWorld().playSoundFromEntity(null, this.mob, this.stepSound, SoundCategory.NEUTRAL, 2.0F, 1.0F);
        this.mob.setPose(EntityPose.STANDING);
    }

    @Override
    public void attemptJump() {
        PlayerEntity player = getPlayer.apply(this.mob);

        if (player != null && player.isOnGround()) {
            if (!this.inAir && this.jumpStrength > 0.0F) {
                double naturalStrength = getBaseJumpingStrength();
                double baseJumpVelocity = naturalStrength * this.jumpStrength * ((EntityAccessor) this.mob).requiem$invokeGetJumpVelocityMultiplier();
                double jumpVelocity = baseJumpVelocity + this.mob.getJumpBoostVelocityModifier();
                Vec3d baseVelocity = player.getVelocity();
                player.setVelocity(baseVelocity.x, jumpVelocity, baseVelocity.z);
                this.inAir = true;
                player.velocityDirty = true;
                if (player.forwardSpeed > 0.0F) {
                    float vx = MathHelper.sin(player.getYaw() * (float) (Math.PI / 180.0));
                    float vz = MathHelper.cos(player.getYaw() * (float) (Math.PI / 180.0));
                    player.setVelocity(player.getVelocity().add(-0.4F * vx * this.jumpStrength, 0.0, 0.4F * vz * this.jumpStrength));
                }

                this.beginClientJump(player);
            } else if (this.inAir) {
                this.finishClientJump(player);
            }
        }
    }

    protected double getBaseJumpingStrength() {
        return baseJumpStrength;
    }

    protected void beginClientJump(PlayerEntity possessor) {
        this.mob.setPose(EntityPose.LONG_JUMPING);
        this.jumpStrength = 0.0F;
    }

    protected void finishClientJump(PlayerEntity possessor) {
        Preconditions.checkState(this.mob.getWorld().isClient, "endJump should only be called clientside");

        this.jumpStrength = 0.0F;
        this.inAir = false;

        // Apparently this packet never gets sent under normal conditions in vanilla
        MinecraftClient.getInstance().player.networkHandler.sendPacket(new ClientCommandC2SPacket(this.mob, ClientCommandC2SPacket.Mode.STOP_RIDING_JUMP));
    }
}

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
package ladysnake.requiem.core.movement;

import com.google.gson.Gson;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.entity.MovementConfig;
import ladysnake.requiem.api.v1.entity.movement.SwimMode;
import ladysnake.requiem.api.v1.entity.movement.WalkMode;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.apiguardian.api.API;

import static ladysnake.requiem.api.v1.util.MoreCodecs.TRISTATE_CODEC;
import static net.fabricmc.fabric.api.util.TriState.*;

/**
 * A {@link MovementConfig} that can be easily manipulated by {@link Gson} and equivalent.
 */
public class SerializableMovementConfig implements MovementConfig {
    public static final SerializableMovementConfig SOUL = new SerializableMovementConfig(MovementMode.ENABLED, SwimMode.ENABLED, WalkMode.NORMAL, TriState.FALSE, TriState.FALSE,false, true, 0, 1F, 1F, 1F, 0.1F);

    private MovementMode flightMode;
    private SwimMode swimMode;
    private WalkMode walkMode;
    private TriState sinksInWater;
    private TriState flopsOnLand;
    private boolean climbsWalls;
    private boolean phasesThroughWalls;
    private float gravity;
    private float fallSpeedModifier;
    private float waterSpeedModifier;
    private float landedSpeedModifier;
    private float inertia;

    @CalledThroughReflection
    @API(status = API.Status.INTERNAL)
    public SerializableMovementConfig() {
        this(MovementMode.UNSPECIFIED, SwimMode.UNSPECIFIED, WalkMode.UNSPECIFIED, TriState.DEFAULT, TriState.DEFAULT, false, false, 0, 1f, 1f, 1f, 0);
    }

    @API(status = API.Status.INTERNAL)
    public SerializableMovementConfig(MovementMode flightMode, SwimMode swimMode, WalkMode walkMode, TriState sinksInWater, TriState flopsOnLand, boolean climbsWalls, boolean phasesThroughWalls, float gravity, float fallSpeedModifier, float landedSpeedModifier, float waterSpeedModifier, float inertia) {
        this.flightMode = flightMode;
        this.swimMode = swimMode;
        this.walkMode = walkMode;
        this.sinksInWater = sinksInWater;
        this.flopsOnLand = flopsOnLand;
        this.climbsWalls = climbsWalls;
        this.phasesThroughWalls = phasesThroughWalls;
        this.gravity = gravity;
        this.fallSpeedModifier = fallSpeedModifier;
        this.landedSpeedModifier = landedSpeedModifier;
        this.waterSpeedModifier = waterSpeedModifier;
        this.inertia = inertia;
    }

    public void toPacket(PacketByteBuf buf) {
        PACKET_CODEC.encode(buf, this);
    }

    public void fromPacket(PacketByteBuf buf) {
        PACKET_CODEC.decode(buf);
    }

    public static final Codec<SerializableMovementConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        MovementMode.CODEC.optionalFieldOf("flight_mode", MovementMode.UNSPECIFIED).forGetter(config -> config.flightMode),
        SwimMode.CODEC.optionalFieldOf("swim_mode", SwimMode.UNSPECIFIED).forGetter(config -> config.swimMode),
        WalkMode.CODEC.optionalFieldOf("walk_mode", WalkMode.UNSPECIFIED).forGetter(config -> config.walkMode),
        TRISTATE_CODEC.optionalFieldOf("sinks_in_water", TriState.DEFAULT).forGetter(config -> config.sinksInWater),
        TRISTATE_CODEC.optionalFieldOf("flops_on_land", TriState.DEFAULT).forGetter(config -> config.flopsOnLand),
        Codec.BOOL.optionalFieldOf("climbs_walls", false).forGetter(config -> config.climbsWalls),
        Codec.BOOL.optionalFieldOf("phases_through_walls", false).forGetter(config -> config.phasesThroughWalls),
        Codec.FLOAT.optionalFieldOf("gravity", 0f).forGetter(config -> config.gravity),
        Codec.FLOAT.optionalFieldOf("fall_speed_modifier", 1f).forGetter(config -> config.fallSpeedModifier),
        Codec.FLOAT.optionalFieldOf("landed_speed_modifier", 1f).forGetter(config -> config.landedSpeedModifier),
        Codec.FLOAT.optionalFieldOf("water_speed_modifier", 1f).forGetter(config -> config.waterSpeedModifier),
        Codec.FLOAT.optionalFieldOf("inertia", 0f).forGetter(config -> config.inertia)
    ).apply(instance, SerializableMovementConfig::new));

    public static final PacketCodec<ByteBuf, SerializableMovementConfig> PACKET_CODEC = PacketCodecs.unlimitedCodec(CODEC);

    @Override
    public MovementMode getFlightMode() {
        return flightMode;
    }

    @Override
    public float getAddedGravity() {
        return gravity;
    }

    @Override
    public float getFallSpeedModifier() {
        return fallSpeedModifier;
    }

    @Override
    public float getLandedSpeedModifier() {
        return landedSpeedModifier;
    }

    @Override
    public float getWaterSpeedModifier() {
        return waterSpeedModifier;
    }

    @Override
    public float getInertia() {
        return inertia;
    }

    @Override
    public SwimMode getSwimMode() {
        return swimMode;
    }

    @Override
    public WalkMode getWalkMode() {
        return walkMode;
    }

    @Override
    public TriState shouldSinkInWater() {
        return sinksInWater;
    }

    @Override
    public TriState shouldFlopOnLand() {
        return flopsOnLand;
    }

    @Override
    public boolean canClimbWalls() {
        return this.climbsWalls;
    }

    @Override
    public boolean canPhaseThroughWalls() {
        return this.phasesThroughWalls;
    }
}

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
package ladysnake.requiem.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ladysnake.requiem.api.v1.record.EntityPointer;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.DistancePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
public record EntityRefPredicate(
    DistancePredicate distance,
    LocationPredicate location,
    EntityPredicate entity
) {
    public static final Codec<EntityRefPredicate> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
                DistancePredicate.CODEC.optionalFieldOf("distance", new DistancePredicate(
                    NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY,
                    NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY
                )).forGetter(EntityRefPredicate::distance),
                LocationPredicate.CODEC.optionalFieldOf("location", new LocationPredicate.Builder().build()).forGetter(EntityRefPredicate::location),
                EntityPredicate.CODEC.optionalFieldOf("entity", EntityPredicate.Builder.create().build()).forGetter(EntityRefPredicate::entity)
            )
            .apply(instance, EntityRefPredicate::new)
    );

    public boolean test(ServerWorld world, @Nullable Vec3d origin, @Nullable EntityPointer entityPointer) {
        if (entityPointer == null) {
            return false;
        }

        Vec3d location = entityPointer.pos();
        if (origin == null) {
            if (distance != null && !distance.equals(new DistancePredicate(
                NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY,
                NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY
            ))) {
                return false;
            }
        } else if (!distance.test(origin.getX(), origin.getY(), origin.getZ(), location.x, location.y, location.z)) {
            return false;
        }

        /*TODO
        if (!location.(world, location.x, location.y, location.z)) {
            return false;
        }

         */

        @Nullable Entity entity = entityPointer.resolve(world.getServer()).orElse(null);
        return this.entity.test(world, origin, entity);
    }
}

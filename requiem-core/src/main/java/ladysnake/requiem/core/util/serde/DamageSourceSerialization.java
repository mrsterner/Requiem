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
package ladysnake.requiem.core.util.serde;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.Optional;

public final class DamageSourceSerialization {
    public static NbtCompound toTag(DamageSource damage) {
        NbtCompound tag = new NbtCompound();
        tag.putString("id", damage.getTypeHolder().getKey().orElseThrow().getValue().toString());
        if (damage.getSource() != null) {
            tag.putUuid("sourceUuid", damage.getSource().getUuid());
        }
        if (damage.getAttacker() != null) {
            tag.putUuid("attackerUuid", damage.getAttacker().getUuid());
        }
        return tag;
    }

    public static DamageSource fromTag(NbtCompound tag, ServerWorld world) {
        String id = tag.getString("id");
        final Entity source;
        final Entity attacker;
        source = tag.containsUuid("sourceUuid") ? world.getEntity(tag.getUuid("sourceUuid")) : null;
        attacker = tag.containsUuid("attackerUuid") ? world.getEntity(tag.getUuid("attackerUuid")) : null;
        return Optional.ofNullable(Identifier.tryParse(id)).map(i ->
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, i)
        ).map(key ->
            world.getDamageSources().create(key, source, attacker)
        ).orElse(world.getDamageSources().generic());
    }
}

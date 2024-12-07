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
package ladysnake.requiem.common.entity.effect;

import ladysnake.requiem.api.v1.internal.StatusEffectReapplicator;
import ladysnake.requiem.api.v1.remnant.StickyStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class StatusEffectReapplicatorImpl implements StatusEffectReapplicator {
    private final Collection<StatusEffectInstance> reappliedEffects = new ArrayList<>();
    private final LivingEntity holder;

    public StatusEffectReapplicatorImpl(LivingEntity holder) {
        this.holder = holder;
    }

    @Override
    public void onStatusEffectRemoved(StatusEffectInstance effect) {
        if (!this.holder.getWorld().isClient && StickyStatusEffect.shouldStick(effect, this.holder)) {
            reappliedEffects.add(new StatusEffectInstance(effect));
        }
    }

    @Override
    public void definitivelyClear() {
        this.reappliedEffects.clear();
    }

    @Override
    public void definitivelyClear(StatusEffect effect) {
        this.reappliedEffects.removeIf(e -> e.getEffectType() == effect);
    }

    @Override
    public void serverTick() {
        for (Iterator<StatusEffectInstance> iterator = reappliedEffects.iterator(); iterator.hasNext(); ) {
            this.holder.addStatusEffect(iterator.next());
            iterator.remove();
        }
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        // Nothing to deserialize for now - data should not last longer than a tick
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        // Nothing to serialize for now - data should not last longer than a tick
    }
}

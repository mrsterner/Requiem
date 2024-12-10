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
package ladysnake.requiem.common.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class OnDeathAfterPossessionCriterion extends AbstractCriterion<OnDeathAfterPossessionCriterion.Conditions> {

    public void handle(ServerPlayerEntity player, Entity entity, DamageSource deathCause) {
        this.trigger(player, (conditions) -> conditions.test(player, entity, deathCause));
    }

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public record Conditions(
        Optional<LootContextPredicate> player,
        Optional<DamageSourcePredicate> killingBlow,
        Boolean seppukku) implements AbstractCriterion.Conditions {

        public static final Codec<OnDeathAfterPossessionCriterion.Conditions> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(OnDeathAfterPossessionCriterion.Conditions::player),
                    DamageSourcePredicate.CODEC.optionalFieldOf("killing_blow").forGetter(OnDeathAfterPossessionCriterion.Conditions::killingBlow),
                    Codec.BOOL.fieldOf("seppukku").forGetter(OnDeathAfterPossessionCriterion.Conditions::seppukku)
                )
                .apply(instance, OnDeathAfterPossessionCriterion.Conditions::new)
        );

        public boolean test(ServerPlayerEntity player, Entity entity, DamageSource killingBlow) {
            boolean killingBlowTest = this.killingBlow
                .map(kb -> kb.test(player, killingBlow))
                .orElse(true);

            boolean playerTest = this.player
                .map(p -> p.test(EntityPredicate.createAdvancementEntityLootContext(player, entity)))
                .orElse(true);

            boolean seppukkuTest = this.seppukku == null || this.seppukku == (killingBlow.getAttacker() == entity);

            return killingBlowTest && playerTest && seppukkuTest;
        }
    }
}

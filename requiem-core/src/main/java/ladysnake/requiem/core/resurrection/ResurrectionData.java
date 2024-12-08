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
package ladysnake.requiem.core.resurrection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ladysnake.requiem.api.v1.event.requiem.ConsumableItemEvents;
import ladysnake.requiem.core.RequiemCoreNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public record ResurrectionData(
    int priority,
    @Nullable EntityPredicate playerPredicate,
    @Nullable EntityPredicate possessedPredicate,
    @Nullable DamageSourcePredicate damageSourcePredicate,
    @Nullable ItemPredicate consumable,
    List<BiPredicate<ServerPlayerEntity, DamageSource>> specials,
    EntityType<?> entityType,
    @Nullable NbtCompound entityNbt
) implements Comparable<ResurrectionData> {

    private static final Map<String, BiPredicate<ServerPlayerEntity, DamageSource>> SPECIAL_PREDICATES = Util.make(new HashMap<>(), m -> {
        m.put("head_in_sand", (player, killingBlow) -> {
            float eyeHeight = player.getEyeHeight(player.getPose());
            return player.getWorld().getBlockState(player.getBlockPos().add(0, (int) eyeHeight, 0)).isIn(BlockTags.SAND);
        });
    });

    public static final Codec<ResurrectionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("priority", 100).forGetter(ResurrectionData::priority),
        EntityPredicate.CODEC.optionalFieldOf("player").forGetter(data -> Optional.ofNullable(data.playerPredicate())),
        EntityPredicate.CODEC.optionalFieldOf("possessed").forGetter(data -> Optional.ofNullable(data.possessedPredicate())),
        DamageSourcePredicate.CODEC.optionalFieldOf("killing_blow").forGetter(data -> Optional.ofNullable(data.damageSourcePredicate())),
        ItemPredicate.CODEC.optionalFieldOf("consumable").forGetter(data -> Optional.ofNullable(data.consumable())),
        Codec.STRING.listOf().optionalFieldOf("special_conditions", List.of()).xmap(
            conditions -> {
                List<BiPredicate<ServerPlayerEntity, DamageSource>> mappedConditions = new ArrayList<>();
                for (String condition : conditions) {
                    BiPredicate<ServerPlayerEntity, DamageSource> predicate = SPECIAL_PREDICATES.get(condition);
                    if (predicate != null) {
                        mappedConditions.add(predicate);
                    }
                }
                return mappedConditions;
            },
            specials -> {
                List<String> keys = new ArrayList<>();
                for (Map.Entry<String, BiPredicate<ServerPlayerEntity, DamageSource>> entry : SPECIAL_PREDICATES.entrySet()) {
                    if (specials.contains(entry.getValue())) {
                        keys.add(entry.getKey());
                    }
                }
                return keys;
            }
        ).forGetter(ResurrectionData::specials),
        Registries.ENTITY_TYPE.getCodec().fieldOf("entity").forGetter(ResurrectionData::entityType),
        NbtCompound.CODEC.optionalFieldOf("nbt").forGetter(data -> Optional.ofNullable(data.entityNbt()))
    ).apply(instance, (priority, playerPredicate, possessedPredicate, killingBlow, consumable, specials, entityType, nbt) ->
        new ResurrectionData(priority,
            playerPredicate.orElse(null),
            possessedPredicate.orElse(null),
            killingBlow.orElse(null),
            consumable.orElse(null),
            specials,
            entityType,
            nbt.orElse(null))
    ));

    public boolean matches(ServerPlayerEntity player, @Nullable LivingEntity possessed, DamageSource killingBlow) {
        if (killingBlow.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        if (damageSourcePredicate != null && !damageSourcePredicate.test(player, killingBlow)) return false;
        if (playerPredicate != null && !playerPredicate.test(player, player)) return false;
        if (possessedPredicate != null && (possessed == null || !possessedPredicate.test(player, possessed))) return false;
        for (var specialCondition : specials) {
            if (!specialCondition.test(player, killingBlow)) return false;
        }
        return tryUseConsumable(player, possessed == null ? player : possessed);
    }

    @Nullable
    public Entity createEntity(World world) {
        Entity entity = entityType.create(world);
        if (entity != null && entityNbt != null) {
            entity.readNbt(entityNbt.copy());
        }
        return entity;
    }

    @Override
    public int compareTo(@NotNull ResurrectionData o) {
        return Integer.compare(o.priority, this.priority);
    }

    private boolean tryUseConsumable(ServerPlayerEntity player, LivingEntity user) {
        if (consumable == null) return true;

        Predicate<ItemStack> action = new Predicate<>() {
            private boolean found;

            @Override
            public boolean test(ItemStack stack) {
                if (found) throw new IllegalStateException("Consumable already found!");
                if (consumable.test(stack)) {
                    ItemStack totem = stack.copy();
                    stack.decrement(1);
                    player.incrementStat(Stats.USED.getOrCreateStat(totem.getItem()));
                    RequiemCoreNetworking.sendItemConsumptionPacket(player, totem);
                    found = true;
                    return true;
                }
                return false;
            }
        };

        for (Hand hand : Hand.values()) {
            if (action.test(user.getStackInHand(hand))) return true;
        }

        return ConsumableItemEvents.SEARCH.invoker().findConsumables(player, action);
    }
}

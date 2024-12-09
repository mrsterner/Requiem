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
package ladysnake.requiem.common.possession.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.util.MoreCodecs;
import ladysnake.requiem.common.RequiemRegistries;
import ladysnake.requiem.core.data.LazyEntityPredicate;
import ladysnake.requiem.core.util.serde.PolymorphicCodecBuilder;
import ladysnake.requiem.mixin.common.access.TextAccessor;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record PossessionItemOverrideWrapper(
    int priority,
    boolean enabled,
    Optional<Text> tooltip,
    EntityPredicate mob,
    Optional<PossessionItemOverride> override
) implements Comparable<PossessionItemOverrideWrapper> {

    public static final MapCodec<PossessionItemOverrideWrapper> CODEC_V1 = codecV1(MoreCodecs.DYNAMIC_JSON);

    // The compressed NBT codec used by PacketByteBuf#encode fails on nulls, so we cannot use regular JSON objects
    public static final MapCodec<PossessionItemOverrideWrapper> NETWORK_CODEC = codecV1(MoreCodecs.STRING_JSON);

    private static MapCodec<PossessionItemOverrideWrapper> codecV1(Codec<JsonElement> jsonCodec) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("priority", 100).forGetter(PossessionItemOverrideWrapper::priority),
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(PossessionItemOverrideWrapper::enabled),
            TextCodecs.CODEC.optionalFieldOf("tooltip").forGetter(o -> o.tooltip),
            EntityPredicate.CODEC.fieldOf("mob").forGetter(o -> o.mob),
            overrideCodecV1(jsonCodec).optionalFieldOf("override").forGetter(w -> w.override)
        ).apply(instance, PossessionItemOverrideWrapper::new));
    }

    private static Codec<PossessionItemOverride> overrideCodecV1(Codec<JsonElement> jsonCodec) {
        return PolymorphicCodecBuilder.create("type", Identifier.CODEC, PossessionItemOverride::getType)
            .with(OldPossessionItemOverride.ID, OldPossessionItemOverride.mapCodec(jsonCodec))
            .with(DietItemOverride.ID, DietItemOverride.codec(jsonCodec))
            .with(HealingItemOverride.ID, HealingItemOverride.codec(jsonCodec))
            .with(CureItemOverride.ID, CureItemOverride.codec(jsonCodec))
            .build();
    }

    public static Optional<TypedActionResult<ItemStack>> tryUseOverride(World world, PlayerEntity player, ItemStack heldStack, Hand hand) {
        OverridableItemStack.get(heldStack).requiem$clearOverriddenUseTime();

        MobEntity possessedEntity = PossessionComponent.getHost(player);
        if (possessedEntity != null) {
            return PossessionItemOverrideWrapper.findOverride(world, player, possessedEntity, heldStack)
                .map(override -> override.use(player, possessedEntity, heldStack, world, hand))
                .filter(res -> res.getResult() != ActionResult.PASS);
        }

        return Optional.empty();
    }

    public static Optional<TypedActionResult<ItemStack>> tryFinishUsingOverride(World world, PlayerEntity player, ItemStack heldStack, Hand hand) {
        OverridableItemStack.get(heldStack).requiem$clearOverriddenUseTime();

        MobEntity possessedEntity = PossessionComponent.getHost(player);
        if (possessedEntity != null) {
            return PossessionItemOverrideWrapper.findOverride(world, player, possessedEntity, heldStack)
                .map(override -> override.finishUsing(player, possessedEntity, heldStack, world, hand))
                .filter(res -> res.getResult() != ActionResult.PASS);
        }

        return Optional.empty();
    }

    public static Optional<InstancedItemOverride> findOverride(World world, PlayerEntity player, MobEntity possessedEntity, ItemStack heldStack) {
        Optional<InstancedItemOverride> fallback = Optional.empty();
        for (PossessionItemOverrideWrapper wrapper : world.getRegistryManager().get(RequiemRegistries.MOB_ITEM_OVERRIDE_KEY).stream().sorted().toList()) {
            Optional<InstancedItemOverride> tested = wrapper.test(player, possessedEntity, heldStack);
            if (tested.isPresent()) {
                if (tested.get().shortCircuits()) {
                    return tested;
                } else if (fallback.isEmpty()) {
                    fallback = tested;
                }
            }
        }
        return fallback;
    }

    public static List<Text> buildTooltip(World world, PlayerEntity player, MobEntity possessedEntity, ItemStack heldStack) {
        List<Text> lines = new ArrayList<>();
        for (PossessionItemOverrideWrapper wrapper : world.getRegistryManager().get(RequiemRegistries.MOB_ITEM_OVERRIDE_KEY).stream().sorted().toList()) {
            Optional<InstancedItemOverride> tested = wrapper.test(player, possessedEntity, heldStack);
            if (tested.isPresent()) {
                InstancedItemOverride override = tested.get();
                if (override.shortCircuits()) {
                    return wrapper.tooltip.flatMap((t) -> override.tweakTooltip(Text.of(t))).map(Collections::singletonList).orElse(Collections.emptyList());
                } else {
                    wrapper.tooltip.flatMap((t) -> override.tweakTooltip(Text.of(t))).ifPresent(lines::add);
                }
            }
        }
        return lines;
    }

    public Optional<InstancedItemOverride> test(PlayerEntity player, MobEntity host, ItemStack stack) {
        return this.override.flatMap(possessionItemOverride -> possessionItemOverride.test(player, host, stack));
    }

    public PossessionItemOverrideWrapper initNow() {

        this.override.ifPresent(PossessionItemOverride::initNow);
        return this;
    }

    @Override
    public int compareTo(@NotNull PossessionItemOverrideWrapper o) {
        return o.priority - this.priority;
    }
}

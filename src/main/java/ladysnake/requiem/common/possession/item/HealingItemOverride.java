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
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.util.MoreCodecs;
import ladysnake.requiem.common.VanillaRequiemPlugin;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Optional;
/*
public record HealingItemOverride(
    LazyItemPredicate item,
    int useTime,
    int cooldown,
    HealingItemOverride.Usage usage
) implements PossessionItemOverride, InstancedItemOverride {
    public static final Identifier ID = Requiem.id("healing");

    public static Codec<HealingItemOverride> codec(Codec<JsonElement> jsonCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
            LazyItemPredicate.codec(jsonCodec).fieldOf("item").forGetter(HealingItemOverride::item),
            Codec.INT.optionalFieldOf("use_time", 0).forGetter(HealingItemOverride::useTime),
            Codec.INT.optionalFieldOf("cooldown", 0).forGetter(HealingItemOverride::cooldown),
            MoreCodecs.enumeration(Usage.class).fieldOf("usage").forGetter(HealingItemOverride::usage)
        ).apply(instance, HealingItemOverride::new));
    }

    @Override
    public void initNow() {
        this.item.initNow();
    }

    @Override
    public Identifier getType() {
        return ID;
    }

    @Override
    public Optional<InstancedItemOverride> test(PlayerEntity player, MobEntity possessed, ItemStack stack) {
        if (this.item.test(player.getWorld(), stack)) {
            if (possessed.getHealth() < possessed.getMaxHealth()) {
                return Optional.of(this);
            } else {
                return Optional.of(OverrideFailure.get(false));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean shortCircuits() {
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(PlayerEntity player, MobEntity possessedEntity, ItemStack heldStack, World world, Hand hand) {
        if (this.useTime <= 0) {
            return this.finishUsing(player, possessedEntity, heldStack, world, hand);
        } else {
            OverridableItemStack.overrideMaxUseTime(heldStack, this.useTime);
            player.setCurrentHand(hand);
            return TypedActionResult.success(heldStack);
        }
    }

    @Override
    public TypedActionResult<ItemStack> finishUsing(PlayerEntity user, MobEntity possessedEntity, ItemStack heldStack, World world, Hand activeHand) {
        Item item = heldStack.getItem();
        TypedActionResult<ItemStack> ret = usage.consume(user, possessedEntity, heldStack, world, activeHand);
        if (this.cooldown > 0) user.getItemCooldownManager().set(item, this.cooldown);
        return ret;
    }

    public enum Usage implements OverrideFilter {
        EAT_TO_HEAL {
            @Override
            public TypedActionResult<ItemStack> consume(PlayerEntity player, MobEntity possessedEntity, ItemStack heldStack, World world, Hand hand) {
                return VanillaRequiemPlugin.healWithFood(player, possessedEntity, heldStack, world, hand);
            }
        },
        REPLACE_BONE {
            @Override
            public TypedActionResult<ItemStack> consume(PlayerEntity player, MobEntity possessedEntity, ItemStack heldStack, World world, Hand hand) {
                return VanillaRequiemPlugin.replaceBone(player, possessedEntity, heldStack, world, hand);
            }
        }
    }
}

 */

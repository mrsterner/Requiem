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
package ladysnake.requiem.compat;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemApi;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.event.requiem.PossessionStartCallback;
import ladysnake.requiem.api.v1.event.requiem.RemnantStateChangeCallback;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import moriyashiine.bewitchment.api.BewitchmentAPI;
import moriyashiine.bewitchment.api.component.TransformationComponent;
import moriyashiine.bewitchment.api.registry.RitualFunction;
import moriyashiine.bewitchment.api.registry.Transformation;
import moriyashiine.bewitchment.common.item.TaglockItem;
import moriyashiine.bewitchment.common.registry.BWComponents;
import moriyashiine.bewitchment.common.registry.BWRegistries;
import moriyashiine.bewitchment.common.registry.BWTransformations;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class BewitchmentCompat implements RequiemPlugin {

    public static final ComponentKey<TransformationComponent> TRANSFORMATION_KEY = BWComponents.TRANSFORMATION_COMPONENT;

    @SuppressWarnings("unchecked")
    public static final ComponentKey<ComponentDataHolder<TransformationComponent>> HOLDER_KEY =
        ComponentRegistry.getOrCreate(Requiem.id("bewitchment_holder"), ((Class<ComponentDataHolder<TransformationComponent>>) (Class<?>) ComponentDataHolder.class));

    public static final RitualFunction DECAY = new DecayRitualFunction();

    @CalledThroughReflection
    public static void init() {
        RequiemApi.registerPlugin(new BewitchmentCompat());
        Registry.register(BWRegistries.RITUAL_FUNCTION, new Identifier("dark_rites", "decay"), DECAY);

        RemnantStateChangeCallback.EVENT.register((player, state, cause) -> {
            if (!player.getWorld().isClient) {
                if (state.isVagrant()) {
                    HOLDER_KEY.get(player).storeDataFrom(player, !cause.isCharacterSwitch());
                    TransformationComponent transformationComponent = TRANSFORMATION_KEY.get(player);

                    transformationComponent.setTransformation(BWTransformations.HUMAN);
                } else if (!cause.isCharacterSwitch()) {
                    HOLDER_KEY.get(player).restoreDataToPlayer(player, true);
                }

                TRANSFORMATION_KEY.sync(player);
            }
        });
        RequiemCompatibilityManager.registerShellDataCallbacks(BewitchmentCompat.HOLDER_KEY);
    }

    @Override
    public void onRequiemInitialize() {
        PossessionStartCallback.EVENT.register(new Identifier("dark_rites", "allow_familiars"), (target, possessor, simulate) -> {
            NbtCompound entityTag = new NbtCompound();
            target.saveSelfNbt(entityTag);

            if (entityTag.contains("Owner") && possessor.getUuid().equals(entityTag.getUuid("Owner"))) {
                if (BWComponents.FAMILIAR_COMPONENT.get(target).isFamiliar()) {
                    return PossessionStartCallback.Result.ALLOW;
                }
            }
            return PossessionStartCallback.Result.PASS;
        });
    }

    static class DecayRitualFunction extends RitualFunction {

        public DecayRitualFunction() {
            super(ParticleTypes.ASH, null);
        }

        @Override
        public void start(ServerWorld world, BlockPos glyphPos, BlockPos effectivePos, Inventory inventory, boolean catFamiliar) {
            ItemStack taglock = null;
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getStack(i);
                if (inventory.getStack(i).getItem() instanceof TaglockItem) {
                    taglock = stack;
                    break;
                }
            }

            if (taglock != null) {
                LivingEntity livingEntity = BewitchmentAPI.getTaglockOwner(world, taglock);

                if (livingEntity instanceof RequiemPlayer player) {
                    RemnantComponent remnant = RemnantComponent.get((PlayerEntity) player);

                    if (remnant.getRemnantType().isDemon() && !player.asPossessor().isPossessionOngoing()) {
                        MobEntity body;

                        if (inventory.count(Items.BONE) > 0) {
                            body = EntityType.SKELETON.create(livingEntity.getWorld());
                        } else {
                            body = EntityType.ZOMBIE.create(livingEntity.getWorld());
                        }

                        if (body != null) {
                            body.copyPositionAndRotation(livingEntity);
                            livingEntity.getWorld().spawnEntity(body);

                            remnant.setVagrant(true);
                            player.asPossessor().startPossessing(body);
                        }
                    }
                }
            }

            super.start(world, glyphPos, effectivePos, inventory, catFamiliar);
        }
    }
    /*
     public static final ComponentKey<VampireComponent> VAMPIRE_KEY = VampireComponent.Companion.getEntityKey();

    @SuppressWarnings("unchecked")
    public static final ComponentKey<ComponentDataHolder<VampireComponent>> HOLDER_KEY =
        ComponentRegistry.getOrCreate(Requiem.id("haema_holder"), ((Class<ComponentDataHolder<VampireComponent>>) (Class<?>) ComponentDataHolder.class));

    @CalledThroughReflection
    public static void init() {
        RemnantStateChangeCallback.EVENT.register((player, state, cause) -> {
            if (!player.world.isClient) {
                if (state.isVagrant()) {
                    HOLDER_KEY.get(player).storeDataFrom(player, !cause.isCharacterSwitch());
                    VampireComponent vampireComponent = VAMPIRE_KEY.get(player);
                    vampireComponent.setPermanentVampire(false);
                    vampireComponent.setVampire(false);
                } else if (!cause.isCharacterSwitch()) {
                    HOLDER_KEY.get(player).restoreDataToPlayer(player, true);
                }

                VAMPIRE_KEY.sync(player);
            }
        });
        RequiemCompatibilityManager.registerShellDataCallbacks(HaemaCompat.HOLDER_KEY);
    }
     */
}

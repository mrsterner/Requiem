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
package ladysnake.requiem;

import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.requiem.api.v1.event.minecraft.SyncServerResourcesCallback;
import ladysnake.requiem.api.v1.remnant.SoulbindingRegistry;
import ladysnake.requiem.api.v1.util.SubDataManagerHelper;
import ladysnake.requiem.common.ApiInitializer;
import ladysnake.requiem.common.RequiemConfig;
import ladysnake.requiem.common.RequiemRecordTypes;
import ladysnake.requiem.common.RequiemRegistries;
import ladysnake.requiem.common.VanillaRequiemPlugin;
import ladysnake.requiem.common.advancement.RequiemStats;
import ladysnake.requiem.common.advancement.criterion.RequiemCriteria;
import ladysnake.requiem.common.block.RequiemBlockEntities;
import ladysnake.requiem.common.block.RequiemBlocks;
import ladysnake.requiem.common.command.RemnantArgumentType;
import ladysnake.requiem.common.command.RequiemCommand;
import ladysnake.requiem.common.dialogue.RemnantChoiceDialogueAction;
import ladysnake.requiem.common.enchantment.RequiemEnchantments;
import ladysnake.requiem.common.entity.RequiemEntities;
import ladysnake.requiem.common.entity.RequiemEntityAttributes;
import ladysnake.requiem.common.entity.RequiemTrackedDataHandlers;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.common.gamerule.RequiemGamerules;
import ladysnake.requiem.common.item.RequiemItems;
import ladysnake.requiem.common.loot.RequiemLootTables;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.common.network.ServerMessageHandling;
import ladysnake.requiem.common.particle.RequiemParticleTypes;
import ladysnake.requiem.common.screen.RequiemScreenHandlers;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.common.structure.RequiemStructures;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.compat.RequiemCompatibilityManager;
import ladysnake.requiem.core.remnant.VagrantInteractionRegistryImpl;
import ladysnake.requiem.core.resurrection.ResurrectionDataLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ladysnake.blabber.Blabber;

import java.util.function.UnaryOperator;

public final class Requiem implements ModInitializer {
    public static final String MOD_ID = "requiem";
    public static final Logger LOGGER = LogManager.getLogger("Requiem");

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ApiInitializer.init();
        RequiemCriteria.init();
        RequiemBlocks.init();
        RequiemBlockEntities.init();
        RequiemTrackedDataHandlers.init();
        RequiemEnchantments.init();
        RequiemEntities.init();
        RequiemEntityAttributes.init();
        RequiemEntityTypeTags.init();
        RequiemGamerules.init();
        RequiemItems.init();
        RequiemLootTables.init();
        RequiemParticleTypes.init();
        RequiemRecordTypes.init();
        RequiemRegistries.init();
        RequiemScreenHandlers.init();
        RequiemSoundEvents.init();
        RequiemStats.init();
        RequiemStatusEffects.init();
        RequiemStructures.init();
        ServerMessageHandling.init();
        ApiInitializer.discoverEntryPoints();
        Blabber.registerAction(id("remnant_choice"), RemnantChoiceDialogueAction.CODEC);
        CommandRegistrationCallback.EVENT.register((dispatcher, ctx, dedicated) -> RequiemCommand.register(dispatcher));


        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ResurrectionDataLoader());
        SyncServerResourcesCallback.EVENT.register(player -> RequiemNetworking.sendTo(player, RequiemNetworking.createDataSyncMessage(SubDataManagerHelper.getServerHelper())));
        ApiInitializer.setPluginCallback(this::registerPlugin);
        RequiemCompatibilityManager.init();

        VanillaRequiemPlugin.INFINITY_SHOT_TAG.getCodec();

        ArgumentTypeRegistry.registerArgumentType(
            id("remnant"),
            RemnantArgumentType.class,
            ConstantArgumentSerializer.of(RemnantArgumentType::remnantType)
        );
    }

    private void registerPlugin(RequiemPlugin plugin) {
        plugin.onRequiemInitialize();
        plugin.registerRemnantStates(RequiemRegistries.REMNANT_STATES);
        plugin.registerMobAbilities(MobAbilityRegistry.instance());
        plugin.registerSoulBindings(SoulbindingRegistry.instance());
        plugin.registerVagrantInteractions(VagrantInteractionRegistryImpl.INSTANCE);
        plugin.registerPossessionItemActions(RequiemRegistries.MOB_ACTIONS);
    }

    public static <T> ComponentType<T> registerData(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, id, (builderOperator.apply(ComponentType.builder())).build());
    }
}

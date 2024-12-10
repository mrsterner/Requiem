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

/*
public final class OriginsCompat {
    public static final ComponentKey<OriginComponent> ORIGIN_KEY = ModComponents.ORIGIN;
    public static final ComponentKey<PowerHolderComponent> APOLI_POWER_KEY = PowerHolderComponent.KEY;
    public static final SerializableDataType<RemnantType> REMNANT_TYPE = SerializableDataType.registry(RemnantType.class, RequiemRegistries.REMNANT_STATES);

    // The factory for the origins power that lets player decide whether they want to be a remnant or not
    public static final Identifier FACTORY_ID = Requiem.id("soul_type");
    public static final PowerFactory<OriginsRemnantPower> REMNANT_POWER_FACTORY = new PowerFactory<>(
        FACTORY_ID,
        new SerializableData()
            .add("value", REMNANT_TYPE)
            .add("key", ApoliDataTypes.KEY, new Active.Key()),
        instance -> {
            RemnantType remnantType = instance.get("value");
            return (type, entity) -> {
                OriginsRemnantPower power = new OriginsRemnantPower(type, entity, remnantType);
                power.setKey(instance.get("key"));
                return power;
            };
        }
    );

    // The factory for the origins player condition that locks access based on the requiem:startingRemnantType gamerule
    public static final Identifier GAMERULE_CONDITION_ID = Requiem.id("start_remnant_gamerule");
    public static final ConditionFactory<Entity> GAMERULE_CONDITION_FACTORY = new ConditionFactory<>(
        GAMERULE_CONDITION_ID,
        new SerializableData().add("value", SerializableDataType.list(SerializableDataType.enumValue(StartingRemnantType.class)), Collections.singletonList(StartingRemnantType.CHOOSE)),
        (instance, entity) -> {
            StartingRemnantType startingRemnantType = RequiemSyncedGamerules.get(entity.getWorld()).getStartingRemnantType();
            return ((List<?>) instance.get("value")).contains(startingRemnantType);
        }
    );
    public static final ComponentKey<OriginDataHolder> ORIGIN_HOLDER_KEY =
        ComponentRegistry.getOrCreate(Requiem.id("origin_holder"), OriginDataHolder.class);
    @SuppressWarnings("unchecked")
    public static final ComponentKey<ComponentDataHolder<PowerHolderComponent>> APOLI_HOLDER_KEY =
        ComponentRegistry.getOrCreate(Requiem.id("apoli_power_holder"), ((Class<ComponentDataHolder<PowerHolderComponent>>) (Class<?>) ComponentDataHolder.class));

    public static final Identifier SOUL_TYPE_LAYER_ID = Requiem.id("soul_type");
    public static final Identifier ORIGIN_MANAGER_RESOURCE_ID =  Identifier.of("origins", "origins");
    public static final Identifier ORIGIN_LAYERS_RESOURCE_ID = Identifier.of("origins", "origin_layers");
    public static final Identifier VAGRANT_ORIGIN_ID = Requiem.id("vagrant");

    private static void applyVagrantOrigin(PlayerEntity player) {
        OriginComponent originComponent = ModComponents.ORIGIN.get(player);

        originSubstitution:
        for (OriginLayer originLayer : OriginLayers.getLayers()) {
            if (originLayer.isEnabled() && !SOUL_TYPE_LAYER_ID.equals(originLayer.getIdentifier())) {
                Origin origin = originComponent.getOrigin(originLayer);

                if (origin != null) {
                    // Do not replace soul-affecting origins
                    for (PowerType<?> powerType : origin.getPowerTypes()) {
                        if (powerType.getFactory().getFactory() == REMNANT_POWER_FACTORY) continue originSubstitution;
                    }
                }

                originComponent.setOrigin(originLayer, OriginRegistry.get(VAGRANT_ORIGIN_ID));
            }
        }
        OriginComponent.sync(player);
    }

    @CalledThroughReflection
    public static void init() {
        Registry.register(ApoliRegistries.POWER_FACTORY, FACTORY_ID, REMNANT_POWER_FACTORY);
        Registry.register(ApoliRegistries.ENTITY_CONDITION, GAMERULE_CONDITION_ID, GAMERULE_CONDITION_FACTORY);
        RemnantStateChangeCallback.EVENT.register((player, state, cause) -> {
            if (!player.getWorld().isClient) {
                boolean transferData = !cause.isCharacterSwitch();
                if (state.isVagrant()) {
                    ORIGIN_HOLDER_KEY.get(player).storeDataFrom(player, transferData);
                    APOLI_HOLDER_KEY.get(player).storeDataFrom(player, transferData);
                    applyVagrantOrigin(player);
                } else if (transferData) {
                    APOLI_HOLDER_KEY.get(player).restoreDataToPlayer(player, true);
                    ORIGIN_HOLDER_KEY.get(player).restoreDataToPlayer(player, true);
                }
            }
        });
        // Fix for Requiem#571, ensure layers are loaded after origins, ensuring in turn that origins are loaded during the callback
        //ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener();
        //TODO ResourceLoader.get(ResourceType.SERVER_DATA).addReloaderOrdering(ORIGIN_MANAGER_RESOURCE_ID, ORIGIN_LAYERS_RESOURCE_ID);
        OriginDataLoadedCallback.EVENT.register(client -> OriginRegistry.get(VAGRANT_ORIGIN_ID).setSpecial());
        RequiemCompatibilityManager.registerShellDataCallbacks(OriginsCompat.ORIGIN_HOLDER_KEY);
        RequiemCompatibilityManager.registerShellDataCallbacks(OriginsCompat.APOLI_HOLDER_KEY);
    }

    public static class OriginDataHolder extends ComponentDataHolder<OriginComponent> {
        public OriginDataHolder(ComponentKey<OriginComponent> originKey, ComponentKey<OriginDataHolder> selfKey) {
            super(originKey, selfKey);
        }
    }
}

 */

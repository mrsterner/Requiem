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

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.event.requiem.PlayerShellEvents;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.entity.PlayerShellEntity;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

import java.util.Optional;
import java.util.function.Consumer;

public final class RequiemCompatibilityManager {

    public static void init() {
        try {
            load("eldritch_mobs", EldritchMobsCompat.class);
            load("malum", MalumCompat.class);
            load("the_bumblezone", BumblezoneCompat.class);
            load("trinkets", TrinketsCompat.class);
        } catch (Throwable t) {
            Requiem.LOGGER.error("[Requiem] Failed to load compatibility hooks", t);
        }
    }

    public static void load(String modId, Class<?> action) {
        try {
            if (FabricLoader.getInstance().isModLoaded(modId)) {
                action.getMethod("init").invoke(null);
            }
        } catch (Throwable t) {
            Requiem.LOGGER.error("[Requiem] Failed to load compatibility hooks for {}", modId, t);
        }
    }

    @CalledThroughReflection
    public static void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {

    }

    static <T extends Entity> void findEntityType(Identifier id, Consumer<EntityType<T>> action) {
        @SuppressWarnings("unchecked") Optional<EntityType<T>> maybe = (Optional<EntityType<T>>) (Optional<?>) Registries.ENTITY_TYPE.getOrEmpty(id);
        if (maybe.isPresent()) {
            action.accept(maybe.get());
        } else {
            RegistryEntryAddedCallback.event(Registries.ENTITY_TYPE).register((rawId, id1, object) -> {
                if (id.equals(id1)) {
                    @SuppressWarnings("unchecked") EntityType<T> t = (EntityType<T>) object;
                    action.accept(t);
                }
            });
        }
    }

    public static <C extends Component> void registerShellDataCallbacks(ComponentKey<? extends ComponentDataHolder<C>> holderKey) {
        PlayerShellEvents.DATA_TRANSFER.register((from, to, merge) -> {
            if (RemnantComponent.isVagrant(from)) {    // can happen with /requiem shell create
                holderKey.get(from).restoreDataToPlayer(to, false, from.getRegistryManager());
            } else {
                ComponentDataHolder<C> holder = holderKey.get(from); // who we get it from does not really matter
                holder.copyDataBetween(from, to, from.getRegistryManager());
            }
        });
        PlayerShellEvents.RESET_IDENTITY.register((player, previousIdentity) -> holderKey.get(player).restoreDataToPlayer(player, false, player.getRegistryManager()));
    }
}

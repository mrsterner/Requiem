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
package ladysnake.requiem.common.particle;

import ladysnake.requiem.Requiem;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class RequiemParticleTypes {
    public static final SimpleParticleType ATTRITION = FabricParticleTypes.simple(false);
    public static final SimpleParticleType ATTUNED = FabricParticleTypes.simple(false);
    public static final SimpleParticleType CURE = FabricParticleTypes.simple(false);
    /*
    public static final ParticleType<RequiemEntityParticleEffect> ENTITY_DUST = new ParticleType<>(false, RequiemEntityParticleEffect.PARAMETERS_FACTORY) {
        @Override
        public Codec<RequiemEntityParticleEffect> getCodec() {
            return RequiemEntityParticleEffect.codec(this);
        }
    };

     */
    public static final SimpleParticleType GHOST = FabricParticleTypes.simple(true);
    /*
    public static final ParticleType<WispTrailParticleEffect> SOUL_TRAIL = new ParticleType<>(true, WispTrailParticleEffect.PARAMETERS_FACTORY) {
        @Override
        public Codec<WispTrailParticleEffect> getCodec() {
            return WispTrailParticleEffect.CODEC;
        }
    };

     */
    public static final SimpleParticleType OBELISK_SOUL = FabricParticleTypes.simple(false);
    public static final SimpleParticleType PENANCE = FabricParticleTypes.simple(false);

    public static void init() {
        Registry.register(Registries.PARTICLE_TYPE, Requiem.id("attrition"), ATTRITION);
        Registry.register(Registries.PARTICLE_TYPE, Requiem.id("attuned"), ATTUNED);
        Registry.register(Registries.PARTICLE_TYPE, Requiem.id("cure"), CURE);
        //Registry.register(Registries.PARTICLE_TYPE, Requiem.id("entity_dust"), ENTITY_DUST);
        Registry.register(Registries.PARTICLE_TYPE, Requiem.id("ghost"), GHOST);
        //Registry.register(Registries.PARTICLE_TYPE, Requiem.id("soul_trail"), SOUL_TRAIL);
        Registry.register(Registries.PARTICLE_TYPE, Requiem.id("obelisk_soul"), OBELISK_SOUL);
        Registry.register(Registries.PARTICLE_TYPE, Requiem.id("penance"), PENANCE);
    }
}

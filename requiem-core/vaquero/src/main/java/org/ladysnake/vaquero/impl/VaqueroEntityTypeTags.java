/*
 * Requiem
 * Copyright (C) 2017-2024 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package org.ladysnake.vaquero.impl;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public final class VaqueroEntityTypeTags {
    public static final TagKey<EntityType<?>> ZOMBIES = register("zombies");
    public static final TagKey<EntityType<?>> PIGLINS = register("piglins");

    public static TagKey<EntityType<?>> register(String name) {
        return TagKey.of(RegistryKeys.ENTITY_TYPE, Vaquero.id(name));
    }
}

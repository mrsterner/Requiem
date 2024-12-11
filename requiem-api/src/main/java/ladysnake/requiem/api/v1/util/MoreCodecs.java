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
package ladysnake.requiem.api.v1.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.util.TriState;

import java.util.Locale;

import static net.fabricmc.fabric.api.util.TriState.*;

public final class MoreCodecs {
    private static final Gson GSON = new Gson();
    public static final Codec<JsonElement> STRING_JSON = Codec.STRING.xmap(
        str -> GSON.fromJson(str, JsonElement.class),
        GSON::toJson
    );

    public static final Codec<JsonElement> DYNAMIC_JSON = Codec.PASSTHROUGH.comapFlatMap(
        dynamic -> DataResult.success(dynamic.convert(JsonOps.INSTANCE).getValue()),
        json -> new Dynamic<>(JsonOps.INSTANCE, json)
    );

    public static <E extends Enum<E>> Codec<E> enumeration(Class<E> enumType) {
        return Codec.STRING.xmap(s -> Enum.valueOf(enumType, s.toUpperCase(Locale.ROOT)), Enum::name);
    }

    private static TriState fromString(String value) {
        return switch (value.toLowerCase()) {
            case "true" -> TRUE;
            case "false" -> FALSE;
            case "default" -> DEFAULT;
            default -> throw new IllegalArgumentException("Invalid TriState value: " + value);
        };
    }

    public static final Codec<TriState> TRISTATE_CODEC = Codec.STRING.xmap(
        MoreCodecs::fromString, TriState::toString
    );
}

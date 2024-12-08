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
package ladysnake.requiem.core.util.serde;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
/*
public final class PolymorphicCodecBuilder<K, S> {
    private final Codec<K> keyCodec;
    private final Function<S, K> keyExtractor;
    private final Map<K, Codec<? extends S>> codecs;
    private final String keyName;

    private PolymorphicCodecBuilder(String keyName, Codec<K> keyCodec, Function<S, K> keyExtractor) {
        this.keyName = keyName;
        this.keyCodec = keyCodec;
        this.keyExtractor = keyExtractor;
        this.codecs = new HashMap<>();
    }

    public static <K, S> PolymorphicCodecBuilder<K, S> create(String keyName, Codec<K> keyElementCodec, Function<S, K> keyExtractor) {
        return new PolymorphicCodecBuilder<>(keyName, keyElementCodec, keyExtractor);
    }

    public PolymorphicCodecBuilder<K, S> with(K key, Codec<? extends S> codec) {
        this.codecs.put(key, codec);
        return this;
    }

    public Codec<S> build() {
        // This method returns a Codec<S> by dispatching based on the key codec and the registered codecs.
        return this.keyCodec.dispatch(this.keyName, this.keyExtractor, key -> {
            Codec<? extends S> codec = this.codecs.get(key);
            if (codec == null) {
                return DataResult.error(() -> "No codec found for key: " + key);
            }
            return DataResult.success((MapCodec<S>) MapCodec.of(codec));
        });
    }
}

 */

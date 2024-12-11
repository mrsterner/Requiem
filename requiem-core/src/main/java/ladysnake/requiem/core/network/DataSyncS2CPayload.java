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
package ladysnake.requiem.core.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ladysnake.requiem.core.RequiemCore;
import ladysnake.requiem.core.movement.MovementAltererManager;
import ladysnake.requiem.core.movement.SerializableMovementConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import java.util.Map;

public record DataSyncS2CPayload(Map<EntityType<?>, SerializableMovementConfig> configs) implements CustomPayload {

    public static Id<DataSyncS2CPayload> ID = new Id<>(RequiemCore.id("data_sync"));

    public static final Codec<DataSyncS2CPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(Registries.ENTITY_TYPE.getCodec(), SerializableMovementConfig.CODEC).fieldOf("configs").forGetter(payload -> payload.configs)
    ).apply(instance, DataSyncS2CPayload::new));

    public static final PacketCodec<? super PacketByteBuf, DataSyncS2CPayload> STREAM_CODEC = PacketCodecs.unlimitedCodec(CODEC);


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handle(DataSyncS2CPayload payload, ClientPlayNetworking.Context ctx) {
        ctx.client().execute(() -> {
            MovementAltererManager.entityMovementConfigs.clear();
            MovementAltererManager.entityMovementConfigs.putAll(payload.configs);
        });
    }
}

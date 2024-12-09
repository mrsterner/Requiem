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
package ladysnake.requiem.core;

import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.core.network.ConsumeResurrectionItemS2CPayload;
import ladysnake.requiem.core.network.HuggingWallC2SPayload;
import ladysnake.requiem.core.network.UseDirectAbilityC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import static io.netty.buffer.Unpooled.buffer;

public final class RequiemCoreNetworking {

    public static void sendAbilityUseMessage(AbilityType type, Entity entity) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeEnumConstant(type);
        buf.writeVarInt(entity.getId());
        ClientPlayNetworking.send(new UseDirectAbilityC2SPayload(buf));
    }

    public static void sendHugWallMessage(boolean hugging) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeBoolean(hugging);
        ClientPlayNetworking.send(new HuggingWallC2SPayload(buf));
    }

    public static void sendItemConsumptionPacket(Entity user, ItemStack stack) {
        RegistryByteBuf buf = new RegistryByteBuf(PacketByteBufs.create() , user.getRegistryManager());
        buf.writeVarInt(user.getId());
        ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, stack);
        sendToAllTrackingIncluding(user, new ConsumeResurrectionItemS2CPayload(buf));
    }
    public static <T extends CustomPayload> void sendToAllTrackingIncluding(Entity tracked, T payload) {
        if (tracked.getWorld() instanceof ServerWorld) {
            for (ServerPlayerEntity player : PlayerLookup.tracking(tracked)) {
                ServerPlayNetworking.send(player, payload);
            }
            if (tracked instanceof ServerPlayerEntity player) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    public static void sendToAllTrackingIncluding(Entity tracked, Packet<?> message) {
        if (tracked.getWorld() instanceof ServerWorld) {
            for (ServerPlayerEntity player : PlayerLookup.tracking(tracked)) {
                player.networkHandler.sendPacket(message);
            }
            if (tracked instanceof ServerPlayerEntity player) {
                player.networkHandler.sendPacket(message);
            }
        }
    }
}

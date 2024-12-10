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

import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.core.RequiemCore;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class ConsumeResurrectionItemS2CPayload implements CustomPayload {

    public static Id<ConsumeResurrectionItemS2CPayload> ID = new Id<>(RequiemCore.id("consume_resurrection_item"));
    public static final PacketCodec<? super RegistryByteBuf, ConsumeResurrectionItemS2CPayload> STREAM_CODEC = CustomPayload.codecOf(ConsumeResurrectionItemS2CPayload::write, ConsumeResurrectionItemS2CPayload::new);
    private final int entityId;
    private final ItemStack itemStack;

    private void write(RegistryByteBuf buf) {
        buf.writeInt(entityId);
        ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, itemStack);
    }

    public ConsumeResurrectionItemS2CPayload(RegistryByteBuf buf) {
        this.entityId = buf.readInt();
        this.itemStack = ItemStack.OPTIONAL_PACKET_CODEC.decode(buf);
    }

    public <T extends CustomPayload> void handle(T payload, ClientPlayNetworking.Context context) {
        World world = context.player().getWorld();
        var mc = context.client();
        Entity entity = world.getEntityById(entityId);
        if (entity != null) {
            world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_TOTEM_USE, entity.getSoundCategory(), 1.0F, 1.0F, false);
            if (entity == mc.player || ((Possessable)entity).getPossessor() == mc.player) {
                mc.gameRenderer.showFloatingItem(itemStack);
            }
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

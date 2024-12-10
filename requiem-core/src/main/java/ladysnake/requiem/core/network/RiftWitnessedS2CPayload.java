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

import ladysnake.requiem.core.RequiemCore;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;

public class RiftWitnessedS2CPayload implements CustomPayload {

    public static Id<RiftWitnessedS2CPayload> ID = new Id<>(RequiemCore.id("rift_witnessed"));
    public static final PacketCodec<? super PacketByteBuf, RiftWitnessedS2CPayload> STREAM_CODEC = CustomPayload.codecOf(RiftWitnessedS2CPayload::write, RiftWitnessedS2CPayload::new);
    public final Text riftName;

    private void write(PacketByteBuf buf) {
        buf.writeString(riftName.toString());
    }

    public RiftWitnessedS2CPayload(PacketByteBuf buf) {
       riftName = Text.of(buf.readString());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
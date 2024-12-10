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
package ladysnake.requiem.core.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.impl.screenhandler.Networking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import org.ladysnake.blabber.impl.common.DialogueScreenHandlerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Networking.OpenScreenPayload.class)
public abstract class OpenScreenPayloadMixin<D> implements CustomPayload {

    @Shadow
    @Final
    private PacketCodec<RegistryByteBuf, D> innerCodec;

    @Shadow
    @Final
    private D data;

    @WrapWithCondition(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V", ordinal = 1))
    private boolean cinema(PacketCodec instance, Object o, Object oo, @Local RegistryByteBuf buf) {
        if (data instanceof DialogueScreenHandlerFactory.DialogueOpeningData diaData && false) {
            System.out.println("0: " + diaData);
            System.out.println("1: " + diaData.availableChoices());
            System.out.println("2: " + diaData.interlocutorId());
            System.out.println("3: " + diaData.dialogue().getCurrentText());
            System.out.println("4: " + diaData.dialogue().getStartAction());
            System.out.println("Encoding dialogue data...");
            if (data == null) {
                System.err.println("Data is null, cannot encode!");
                return false;
            }
            try {
                System.out.println("Buffer before encoding: " + buf);
                innerCodec.encode(buf, data);
                System.out.println("Encoding successful! Buffer after encoding: " + buf);
            } catch (Exception e) {
                System.err.println("Encoding failed at: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }
}

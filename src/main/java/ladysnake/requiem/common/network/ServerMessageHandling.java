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
package ladysnake.requiem.common.network;

import ladysnake.requiem.api.v1.event.requiem.InitiateFractureCallback;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.core.network.EtherealFractureC2SPayload;
import ladysnake.requiem.core.network.HuggingWallC2SPayload;
import ladysnake.requiem.core.network.OpenCraftingScreenC2SPayload;
import ladysnake.requiem.core.network.UseDirectAbilityC2SPayload;
import ladysnake.requiem.core.network.UseIndirectDirectAbilityC2SPayload;
import ladysnake.requiem.core.network.UseRiftC2SPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.MobEntity;

public final class ServerMessageHandling {

    public static void init() {

        PayloadTypeRegistry.playC2S().register(UseDirectAbilityC2SPayload.ID, UseDirectAbilityC2SPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UseDirectAbilityC2SPayload.ID, (payload, ctx) -> {
            ctx.server().execute(() ->
                payload.handle(payload, ctx)
            );
        });

        PayloadTypeRegistry.playC2S().register(UseIndirectDirectAbilityC2SPayload.ID, UseIndirectDirectAbilityC2SPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UseIndirectDirectAbilityC2SPayload.ID,  (payload, ctx) -> {
            payload.handle(payload, ctx);
        });

        PayloadTypeRegistry.playC2S().register(EtherealFractureC2SPayload.ID, EtherealFractureC2SPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(EtherealFractureC2SPayload.ID, (payload, ctx) -> {
            ctx.server().execute(() ->  InitiateFractureCallback.EVENT.invoker().performFracture(ctx.player()));
        });

        PayloadTypeRegistry.playC2S().register(HuggingWallC2SPayload.ID, HuggingWallC2SPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(HuggingWallC2SPayload.ID, (payload, ctx) -> {
            // Possible failure points: the player may not actually be against a block, or it may not have the right movement
            // we do not handle those right now, as movement is entirely done clientside
            ctx.server().execute(() -> payload.handle(payload, ctx));
        });

        PayloadTypeRegistry.playC2S().register(OpenCraftingScreenC2SPayload.ID, OpenCraftingScreenC2SPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(OpenCraftingScreenC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                MobEntity possessed = PossessionComponent.get(context.player()).getHost();
                if (possessed != null && possessed.getType().isIn(RequiemEntityTypeTags.SUPERCRAFTERS)) {
                    context.player().openHandledScreen(Blocks.CRAFTING_TABLE.getDefaultState().createScreenHandlerFactory(context.player().getWorld(), context.player().getBlockPos()));
                }
            });
        });

        PayloadTypeRegistry.playC2S().register(UseRiftC2SPayload.ID, UseRiftC2SPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UseRiftC2SPayload.ID, (payload, ctx) -> {
            ctx.server().execute(() -> {
                payload.handle(payload, ctx);
            });
        });


    }
}

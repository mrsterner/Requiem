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
package ladysnake.requiem.mixin.common.event;

import ladysnake.requiem.api.v1.event.minecraft.PlayerRespawnCallback;
import ladysnake.requiem.api.v1.event.minecraft.PrepareRespawnCallback;
import ladysnake.requiem.api.v1.event.minecraft.SyncServerResourcesCallback;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    private static final ThreadLocal<ServerWorld> REQUIEM$RESPAWN_WORLD = new ThreadLocal<>();

    @Shadow @Final private List<ServerPlayerEntity> players;

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "onPlayerConnect", at = @At(value = "NEW", target = "(Ljava/util/Collection;)Lnet/minecraft/network/packet/s2c/play/SynchronizeRecipesS2CPacket;"))
    private void synchronizeServerData(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        SyncServerResourcesCallback.EVENT.invoker().onServerSync(player);
    }

    @Inject(method = "onDataPacksReloaded", at = @At(value = "NEW", target = "(Ljava/util/Map;)Lnet/minecraft/network/packet/s2c/common/SynchronizeTagsS2CPacket;"))
    private void synchronizeServerData(CallbackInfo ci) {
        for (ServerPlayerEntity player : this.players) {
            SyncServerResourcesCallback.EVENT.invoker().onServerSync(player);
        }
    }

    @ModifyVariable(
        method = "respawnPlayer",
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/TeleportTarget;missingRespawnBlock()Z", ordinal = 1)),
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;networkHandler:Lnet/minecraft/server/network/ServerPlayNetworkHandler;",
            ordinal = 1
        ),
        ordinal = 1
    )
    private ServerPlayerEntity firePrepareRespawnEvent(ServerPlayerEntity clone, ServerPlayerEntity original, boolean returnFromEnd) {
        PrepareRespawnCallback.EVENT.invoker().prepareRespawn(original, clone, returnFromEnd);
        return clone;
    }

/*
    @ModifyVariable(
        method = "respawnPlayer",
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/TeleportTarget;missingRespawnBlock()Z")),
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;networkHandler:Lnet/minecraft/server/network/ServerPlayNetworkHandler;",
            ordinal = 1,
            shift = AFTER
        ),
        ordinal = 1
    )
    private ServerWorld fixRespawnWorld(ServerWorld serverWorld) {
        // Return the custom world set in the first modification
        return REQUIEM$RESPAWN_WORLD.get();
    }

 */


    @Inject(method = "respawnPlayer", at = @At("RETURN"))
    private void firePlayerRespawnEvent(
        ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir
    ) {
        PlayerRespawnCallback.EVENT.invoker().onPlayerRespawn(cir.getReturnValue());
    }
}

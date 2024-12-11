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
package ladysnake.requiem.client.network;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.api.v1.util.SubDataManager;
import ladysnake.requiem.api.v1.util.SubDataManagerHelper;
import ladysnake.requiem.client.RequiemClient;
import ladysnake.requiem.client.RequiemFx;
import ladysnake.requiem.client.gui.RiftWitnessedToast;
import ladysnake.requiem.common.block.obelisk.RunestoneBlockEntity;
import ladysnake.requiem.common.particle.RequiemParticleTypes;
import ladysnake.requiem.common.remnant.RemnantTypes;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.core.network.AnchorDamageS2CPayload;
import ladysnake.requiem.core.network.BodyCureS2CPayload;
import ladysnake.requiem.core.network.ConsumeResurrectionItemS2CPayload;
import ladysnake.requiem.core.network.DataSyncS2CPayload;
import ladysnake.requiem.core.network.EtherealAnimationS2CPayload;
import ladysnake.requiem.core.network.ObeliskPowerUpgradeS2CPayload;
import ladysnake.requiem.core.network.OpusUseS2CPayload;
import ladysnake.requiem.core.network.RiftWitnessedS2CPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClientMessageHandler {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final RequiemClient rc;

    public ClientMessageHandler(RequiemClient requiemClient) {
        this.rc = requiemClient;
    }

    public void init() {

        PayloadTypeRegistry.playS2C().register(AnchorDamageS2CPayload.ID, AnchorDamageS2CPayload.STREAM_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(AnchorDamageS2CPayload.ID, (payload, ctx) -> {
            var dead = payload.dead;
            ctx.client().execute(() -> {
                RequiemClient.instance().fxRenderer().playEtherealPulseAnimation(
                    dead ? 4 : 1, RequiemFx.ETHEREAL_DAMAGE_COLOR[0], RequiemFx.ETHEREAL_DAMAGE_COLOR[1], RequiemFx.ETHEREAL_DAMAGE_COLOR[2]
                );
            });
        });

        PayloadTypeRegistry.playS2C().register(BodyCureS2CPayload.ID, BodyCureS2CPayload.STREAM_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(BodyCureS2CPayload.ID, (payload, ctx) -> {

            ctx.client().execute(() -> {
                World world = ctx.player().getWorld();
                Entity entity = world.getEntityById(payload.entityId);
                if (entity != null) {
                    for(int i = 0; i < 40; ++i) {
                        double vx = entity.getWorld().random.nextGaussian() * 0.05D;
                        double vy = entity.getWorld().random.nextGaussian() * 0.05D;
                        double vz = entity.getWorld().random.nextGaussian() * 0.05D;
                        entity.getWorld().addParticle(RequiemParticleTypes.CURE, entity.getParticleX(0.5D), entity.getRandomBodyY(), entity.getParticleZ(0.5D), vx, vy, vz);
                    }
                }
            });
        });

        PayloadTypeRegistry.playS2C().register(ConsumeResurrectionItemS2CPayload.ID, ConsumeResurrectionItemS2CPayload.STREAM_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(ConsumeResurrectionItemS2CPayload.ID, (payload, ctx) -> {
            ctx.client().execute(() -> {
                payload.handle(payload, ctx);
            });
        });

        PayloadTypeRegistry.playS2C().register(DataSyncS2CPayload.ID, DataSyncS2CPayload.STREAM_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(DataSyncS2CPayload.ID,  (payload, ctx) -> {
            // We intentionally do not use the context's task queue directly
            // First, we make each sub data manager process its data, then we apply it synchronously with the task queue

            Map<Identifier, SubDataManager<?>> map = SubDataManagerHelper.getClientHelper().streamDataManagers().collect(Collectors.toMap(IdentifiableResourceReloadListener::getFabricId, Function.identity()));
            int nbManagers = payload.nbManagers;
            for (int i = 0; i < nbManagers; i++) {
                Identifier id = payload.id;
                SubDataManager<?> manager = Objects.requireNonNull(map.get(id), "Unknown sub data manager " + id);
                Requiem.LOGGER.info("[Requiem] Received data for {}", manager.getFabricId());
                //TODO syncSubDataManager(payload, manager, ctx.client());
            }
        });


        PayloadTypeRegistry.playS2C().register(EtherealAnimationS2CPayload.ID, EtherealAnimationS2CPayload.STREAM_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(EtherealAnimationS2CPayload.ID,  (payload, ctx) -> {
            ctx.client().execute(() -> {
                MinecraftClient mc = this.mc;
                assert mc.player != null;
                mc.player.getWorld().playSound(mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), RequiemSoundEvents.EFFECT_DISSOCIATE, SoundCategory.PLAYERS, 2, 0.6f);
                this.rc.fxRenderer().beginEtherealAnimation();
            });
        });

        PayloadTypeRegistry.playS2C().register(OpusUseS2CPayload.ID, OpusUseS2CPayload.STREAM_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(OpusUseS2CPayload.ID,  (payload, ctx) -> {
            RemnantType remnantType = RemnantTypes.get(payload.remnantId);
            boolean cure = !remnantType.isDemon();

            ctx.client().execute(() -> {
                PlayerEntity player = Objects.requireNonNull(ctx.client().player);
                if (payload.showBook) {
                    mc.particleManager.addEmitter(player, ParticleTypes.PORTAL, 120);
                    mc.gameRenderer.showFloatingItem(remnantType.getConversionBook(player));
                }
                if (cure) {
                    this.rc.fxRenderer().playEtherealPulseAnimation(16, 0.0f, 0.8f, 0.6f);
                } else {
                    this.rc.fxRenderer().playEtherealPulseAnimation(16, 1.0f, 0.25f, 0.27f);
                }
            });
        });

        PayloadTypeRegistry.playS2C().register(ObeliskPowerUpgradeS2CPayload.ID, ObeliskPowerUpgradeS2CPayload.STREAM_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(ObeliskPowerUpgradeS2CPayload.ID,  (payload, ctx) -> {
            ctx.client().execute(() -> RunestoneBlockEntity.updateObeliskPower(ctx.client().world, payload.controllerPos, payload.coreWidth, payload.coreHeight, payload.powerRate));
        });

        PayloadTypeRegistry.playS2C().register(RiftWitnessedS2CPayload.ID, RiftWitnessedS2CPayload.STREAM_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(RiftWitnessedS2CPayload.ID, (payload, ctx) -> {
            ctx.client().execute(() -> ctx.client().getToastManager().add(new RiftWitnessedToast(payload.riftName)));
        });

    }

    private static <T> void syncSubDataManager(PacketByteBuf buffer, SubDataManager<T> subManager, ThreadExecutor<?> taskQueue) {
        T data = subManager.loadFromPacket(buffer);
        taskQueue.execute(() -> {subManager.apply(data);});
    }
 }

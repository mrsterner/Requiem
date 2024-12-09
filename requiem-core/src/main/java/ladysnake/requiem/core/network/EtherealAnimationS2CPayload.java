package ladysnake.requiem.core.network;

import ladysnake.requiem.core.RequiemCore;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.world.World;

public class EtherealAnimationS2CPayload implements CustomPayload {

    public static Id<EtherealAnimationS2CPayload> ID = new Id<>(RequiemCore.id("ethereal_animation"));
    public static final PacketCodec<? super PacketByteBuf, EtherealAnimationS2CPayload> STREAM_CODEC = CustomPayload.codecOf(EtherealAnimationS2CPayload::write, EtherealAnimationS2CPayload::new);


    private void write(PacketByteBuf buf) {
    }

    public EtherealAnimationS2CPayload(PacketByteBuf buf) {
    }

    public <T extends CustomPayload> void handle(T payload, ClientPlayNetworking.Context context) {

    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

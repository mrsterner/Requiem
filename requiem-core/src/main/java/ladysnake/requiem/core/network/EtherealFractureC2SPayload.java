package ladysnake.requiem.core.network;

import ladysnake.requiem.core.RequiemCore;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public class EtherealFractureC2SPayload implements CustomPayload {

    public static Id<EtherealFractureC2SPayload> ID = new Id<>(RequiemCore.id("ethereal_fracture"));
    public static final PacketCodec<? super PacketByteBuf, EtherealFractureC2SPayload> STREAM_CODEC = CustomPayload.codecOf(EtherealFractureC2SPayload::write, EtherealFractureC2SPayload::new);

    private void write(PacketByteBuf buf) {
    }

    public EtherealFractureC2SPayload(PacketByteBuf buf) {
    }

    public <T extends CustomPayload> void handle(T payload, ServerPlayNetworking.Context context) {

    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

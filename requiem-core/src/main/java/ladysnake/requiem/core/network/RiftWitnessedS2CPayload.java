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

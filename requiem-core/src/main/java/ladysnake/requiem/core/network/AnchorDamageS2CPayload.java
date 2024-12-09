package ladysnake.requiem.core.network;

import ladysnake.requiem.core.RequiemCore;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.world.World;

public class AnchorDamageS2CPayload implements CustomPayload {

    public static Id<AnchorDamageS2CPayload> ID = new Id<>(RequiemCore.id("anchor_damage"));
    public static final PacketCodec<? super PacketByteBuf, AnchorDamageS2CPayload> STREAM_CODEC = CustomPayload.codecOf(AnchorDamageS2CPayload::write, AnchorDamageS2CPayload::new);
    public final boolean dead;

    private void write(PacketByteBuf buf) {
        buf.writeBoolean(dead);
    }

    public AnchorDamageS2CPayload(PacketByteBuf buf) {
        dead = buf.readBoolean();
    }


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

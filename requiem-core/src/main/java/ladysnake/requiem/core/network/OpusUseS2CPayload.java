package ladysnake.requiem.core.network;

import ladysnake.requiem.core.RequiemCore;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;

import java.util.Objects;

public class OpusUseS2CPayload implements CustomPayload {

    public static Id<OpusUseS2CPayload> ID = new Id<>(RequiemCore.id("opus_use"));
    public static final PacketCodec<? super PacketByteBuf, OpusUseS2CPayload> STREAM_CODEC = CustomPayload.codecOf(OpusUseS2CPayload::write, OpusUseS2CPayload::new);
    public final int remnantId;
    public final boolean showBook;

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(remnantId);
        buf.writeBoolean(showBook);
    }

    public OpusUseS2CPayload(PacketByteBuf buf) {
        remnantId = buf.readVarInt();
        showBook = buf.readBoolean();
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

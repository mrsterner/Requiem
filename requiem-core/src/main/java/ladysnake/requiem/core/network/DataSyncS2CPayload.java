package ladysnake.requiem.core.network;

import ladysnake.requiem.core.RequiemCore;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class DataSyncS2CPayload implements CustomPayload {

    public static Id<DataSyncS2CPayload> ID = new Id<>(RequiemCore.id("data_sync"));
    public static final PacketCodec<? super PacketByteBuf, DataSyncS2CPayload> STREAM_CODEC = CustomPayload.codecOf(DataSyncS2CPayload::write, DataSyncS2CPayload::new);
    public final int nbManagers;
    public final Identifier id;

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(nbManagers);
        buf.writeIdentifier(id);
    }

    public DataSyncS2CPayload(PacketByteBuf buf) {
        this.nbManagers = buf.readVarInt();
        this.id = buf.readIdentifier();
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

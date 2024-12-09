package ladysnake.requiem.core.network;

import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.core.RequiemCore;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public class HuggingWallC2SPayload implements CustomPayload {

    public static Id<HuggingWallC2SPayload> ID = new Id<>(RequiemCore.id("hugging_wall"));
    public static final PacketCodec<? super PacketByteBuf, HuggingWallC2SPayload> STREAM_CODEC = CustomPayload.codecOf(HuggingWallC2SPayload::write, HuggingWallC2SPayload::new);
    private final boolean huggingWall;

    private void write(PacketByteBuf buf) {
        buf.writeBoolean(this.huggingWall);
    }

    public HuggingWallC2SPayload(PacketByteBuf buf) {
        this.huggingWall = buf.readBoolean();
    }

    public <T extends CustomPayload> void handle(T payload, ServerPlayNetworking.Context context) {
        MovementAlterer.get(context.player()).hugWall(huggingWall);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

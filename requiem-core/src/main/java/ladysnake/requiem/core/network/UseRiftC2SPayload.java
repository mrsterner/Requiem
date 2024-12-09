package ladysnake.requiem.core.network;

import ladysnake.requiem.api.v1.block.ObeliskDescriptor;
import ladysnake.requiem.core.RequiemCore;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public class UseRiftC2SPayload implements CustomPayload {

    public static Id<UseRiftC2SPayload> ID = new Id<>(RequiemCore.id("use_rift"));
    public static final PacketCodec<? super PacketByteBuf, UseRiftC2SPayload> STREAM_CODEC = CustomPayload.codecOf(UseRiftC2SPayload::write, UseRiftC2SPayload::new);

    private final ObeliskDescriptor target;

    private void write(PacketByteBuf buf) {
        buf.encode(NbtOps.INSTANCE, ObeliskDescriptor.CODEC, target);
    }

    public UseRiftC2SPayload(PacketByteBuf buf) {
        target = buf.decode(NbtOps.INSTANCE, ObeliskDescriptor.CODEC);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handle(UseRiftC2SPayload payload, ServerPlayNetworking.Context ctx) {
        /*TODO
        if (ctx.player().currentScreenHandler instanceof RiftScreenHandler riftScreenHandler) {
            riftScreenHandler.useRift(player, target);
        }

         */
    }
}

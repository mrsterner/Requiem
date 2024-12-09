package ladysnake.requiem.core.network;

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.core.RequiemCore;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public class OpenCraftingScreenC2SPayload implements CustomPayload {

    public static Id<OpenCraftingScreenC2SPayload> ID = new Id<>(RequiemCore.id("open_crafting_screen"));
    public static final PacketCodec<? super PacketByteBuf, OpenCraftingScreenC2SPayload> STREAM_CODEC = CustomPayload.codecOf(OpenCraftingScreenC2SPayload::write, OpenCraftingScreenC2SPayload::new);

    private void write(PacketByteBuf buf) {
    }

    public OpenCraftingScreenC2SPayload(PacketByteBuf buf) {
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

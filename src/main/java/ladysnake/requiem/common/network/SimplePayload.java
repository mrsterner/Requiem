package ladysnake.requiem.common.network;

import ladysnake.requiem.Requiem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class SimplePayload implements CustomPayload {

    public static CustomPayload.Id<SimplePayload> ID = new CustomPayload.Id<>(Requiem.id("simple"));

    public SimplePayload(Identifier id, PacketByteBuf buf) {

    }


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

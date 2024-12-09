package ladysnake.requiem.core.network;

import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import ladysnake.requiem.core.RequiemCore;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public class UseIndirectDirectAbilityC2SPayload implements CustomPayload {

    public static Id<UseIndirectDirectAbilityC2SPayload> ID = new Id<>(RequiemCore.id("indirect_ability"));
    public static final PacketCodec<? super PacketByteBuf, UseIndirectDirectAbilityC2SPayload> STREAM_CODEC = CustomPayload.codecOf(UseIndirectDirectAbilityC2SPayload::write, UseIndirectDirectAbilityC2SPayload::new);

    private final AbilityType type;

    private void write(PacketByteBuf buf) {
        buf.writeEnumConstant(type);
    }

    public UseIndirectDirectAbilityC2SPayload(PacketByteBuf buf) {
        this.type = buf.readEnumConstant(AbilityType.class);
    }

    public <T extends CustomPayload> void handle(T payload, ServerPlayNetworking.Context context) {
        var player = context.player();
        MobAbilityController.get(player).useIndirect(type);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

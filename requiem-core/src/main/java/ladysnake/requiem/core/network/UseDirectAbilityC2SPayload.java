package ladysnake.requiem.core.network;

import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import ladysnake.requiem.core.RequiemCore;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public class UseDirectAbilityC2SPayload implements CustomPayload {

    public static CustomPayload.Id<UseDirectAbilityC2SPayload> ID = new CustomPayload.Id<>(RequiemCore.id("direct_ability"));
    public static final PacketCodec<? super PacketByteBuf, UseDirectAbilityC2SPayload> STREAM_CODEC = CustomPayload.codecOf(UseDirectAbilityC2SPayload::write, UseDirectAbilityC2SPayload::new);

    private final AbilityType type;
    private final int entityId;

    private void write(PacketByteBuf buf) {
        buf.writeEnumConstant(type);
        buf.writeVarInt(entityId);
    }

    public UseDirectAbilityC2SPayload(PacketByteBuf buf) {
        this.type = buf.readEnumConstant(AbilityType.class);
        this.entityId = buf.readVarInt();
    }

    public <T extends CustomPayload> void handle(T payload, ServerPlayNetworking.Context context) {
        var player = context.player();
        MobAbilityController abilityController = MobAbilityController.get(player);
        Entity targetedEntity = player.getWorld().getEntityById(entityId);

        // allow a slightly longer reach in case of lag
        if (targetedEntity != null && (abilityController.getRange(type) + 3) > targetedEntity.distanceTo(player)) {
            abilityController.useDirect(type, targetedEntity);
        }

        // sync abilities in case the server disagrees with the client's guess
        MobAbilityController.KEY.sync(player);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

package ladysnake.requiem.core.network;

import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.core.RequiemCore;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class ConsumeResurrectionItemS2CPayload implements CustomPayload {

    public static Id<ConsumeResurrectionItemS2CPayload> ID = new Id<>(RequiemCore.id("consume_resurrection_item"));
    public static final PacketCodec<? super RegistryByteBuf, ConsumeResurrectionItemS2CPayload> STREAM_CODEC = CustomPayload.codecOf(ConsumeResurrectionItemS2CPayload::write, ConsumeResurrectionItemS2CPayload::new);
    private final int entityId;
    private final ItemStack itemStack;

    private void write(RegistryByteBuf buf) {
        buf.writeInt(entityId);
        ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, itemStack);
    }

    public ConsumeResurrectionItemS2CPayload(RegistryByteBuf buf) {
        this.entityId = buf.readInt();
        this.itemStack = ItemStack.OPTIONAL_PACKET_CODEC.decode(buf);
    }

    public <T extends CustomPayload> void handle(T payload, ClientPlayNetworking.Context context) {
        World world = context.player().getWorld();
        var mc = context.client();
        Entity entity = world.getEntityById(entityId);
        if (entity != null) {
            world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_TOTEM_USE, entity.getSoundCategory(), 1.0F, 1.0F, false);
            if (entity == mc.player || ((Possessable)entity).getPossessor() == mc.player) {
                mc.gameRenderer.showFloatingItem(itemStack);
            }
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

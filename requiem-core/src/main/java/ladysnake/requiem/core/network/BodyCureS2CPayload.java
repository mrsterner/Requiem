package ladysnake.requiem.core.network;

import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.core.RequiemCore;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class BodyCureS2CPayload implements CustomPayload {

    public static Id<BodyCureS2CPayload> ID = new Id<>(RequiemCore.id("body_cure"));
    public static final PacketCodec<? super PacketByteBuf, BodyCureS2CPayload> STREAM_CODEC = CustomPayload.codecOf(BodyCureS2CPayload::write, BodyCureS2CPayload::new);
    private final int entityId;

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(entityId);
    }

    public BodyCureS2CPayload(PacketByteBuf buf) {
        this.entityId = buf.readVarInt();
    }

    public <T extends CustomPayload> void handle(T payload, ClientPlayNetworking.Context context) {
        World world = context.player().getWorld();
        Entity entity = world.getEntityById(entityId);
        if (entity != null) {
            for(int i = 0; i < 40; ++i) {
                double vx = entity.getWorld().random.nextGaussian() * 0.05D;
                double vy = entity.getWorld().random.nextGaussian() * 0.05D;
                double vz = entity.getWorld().random.nextGaussian() * 0.05D;
                //TODO entity.getWorld().addParticle(RequiemParticleTypes.CURE, entity.getParticleX(0.5D), entity.getRandomBodyY(), entity.getParticleZ(0.5D), vx, vy, vz);
            }
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

package ladysnake.requiem.common.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleType;

public class WispTrailParticleType extends ParticleType<WispTrailParticleData> {

    public WispTrailParticleType(boolean alwaysShow) {
        super(alwaysShow);
    }

    @Override
    public MapCodec<WispTrailParticleData> getCodec() {
        return WispTrailParticleData.CODEC;
    }

    @Override
    public PacketCodec<? super RegistryByteBuf, WispTrailParticleData> getPacketCodec() {
        return WispTrailParticleData.PACKET_CODEC;
    }
}

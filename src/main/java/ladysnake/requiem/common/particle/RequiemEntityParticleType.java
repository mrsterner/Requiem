package ladysnake.requiem.common.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleType;

public class RequiemEntityParticleType extends ParticleType<RequiemEntityParticleData> {

    public RequiemEntityParticleType(boolean alwaysShow) {
        super(alwaysShow);
    }

    @Override
    public MapCodec<RequiemEntityParticleData> getCodec() {
        return RequiemEntityParticleData.CODEC;
    }

    @Override
    public PacketCodec<? super RegistryByteBuf, RequiemEntityParticleData> getPacketCodec() {
        return RequiemEntityParticleData.PACKET_CODEC;
    }
}

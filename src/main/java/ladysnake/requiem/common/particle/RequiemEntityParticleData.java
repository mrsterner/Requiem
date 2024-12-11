package ladysnake.requiem.common.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public record RequiemEntityParticleData(int srcId, int dstId) implements ParticleEffect {

    public static final MapCodec<RequiemEntityParticleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.INT.fieldOf("srcId").forGetter(RequiemEntityParticleData::srcId),
        Codec.INT.fieldOf("dstId").forGetter(RequiemEntityParticleData::dstId)
    ).apply(instance, RequiemEntityParticleData::new));

    public static final PacketCodec<RegistryByteBuf, RequiemEntityParticleData> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT, RequiemEntityParticleData::srcId,
        PacketCodecs.VAR_INT, RequiemEntityParticleData::dstId,
        RequiemEntityParticleData::new
    );

    @Override
    public ParticleType<?> getType() {
        return RequiemParticleTypes.ENTITY_DUST;
    }
}

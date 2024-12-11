package ladysnake.requiem.common.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public record WispTrailParticleData(float red, float green, float blue, float redEvolution, float greenEvolution, float blueEvolution) implements ParticleEffect {

    public static final MapCodec<WispTrailParticleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.FLOAT.fieldOf("r").forGetter(WispTrailParticleData::red),
        Codec.FLOAT.fieldOf("g").forGetter(WispTrailParticleData::green),
        Codec.FLOAT.fieldOf("b").forGetter(WispTrailParticleData::blue),
        Codec.FLOAT.fieldOf("re").forGetter(WispTrailParticleData::redEvolution),
        Codec.FLOAT.fieldOf("ge").forGetter(WispTrailParticleData::greenEvolution),
        Codec.FLOAT.fieldOf("be").forGetter(WispTrailParticleData::blueEvolution)
    ).apply(instance, WispTrailParticleData::new));

    public static final PacketCodec<RegistryByteBuf, WispTrailParticleData> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.FLOAT, WispTrailParticleData::red,
        PacketCodecs.FLOAT, WispTrailParticleData::green,
        PacketCodecs.FLOAT, WispTrailParticleData::blue,
        PacketCodecs.FLOAT, WispTrailParticleData::redEvolution,
        PacketCodecs.FLOAT, WispTrailParticleData::greenEvolution,
        PacketCodecs.FLOAT, WispTrailParticleData::blueEvolution,
        WispTrailParticleData::new
    );


    @Override
    public ParticleType<?> getType() {
        return RequiemParticleTypes.SOUL_TRAIL;
    }
}

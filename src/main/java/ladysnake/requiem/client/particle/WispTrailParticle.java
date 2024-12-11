package ladysnake.requiem.client.particle;

import ladysnake.requiem.common.particle.WispTrailParticleData;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;

public class WispTrailParticle extends SpriteBillboardParticle {

    private final float redEvolution;
    private final float greenEvolution;
    private final float blueEvolution;

    private WispTrailParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, WispTrailParticleData wispTrailData, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.red = wispTrailData.red();
        this.green = wispTrailData.green();
        this.blue = wispTrailData.blue();
        this.redEvolution = wispTrailData.redEvolution();
        this.greenEvolution = wispTrailData.greenEvolution();
        this.blueEvolution = wispTrailData.blueEvolution();
        this.maxAge = 10 + this.random.nextInt(10);
        this.scale *= 0.25f + new Random().nextFloat() * 0.50f;
        this.setSpriteForAge(spriteProvider);
        this.velocityY = 0.1;
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        // fade and die
        if (this.age++ >= this.maxAge) {
            alpha -= 0.05f;
        }
        if (alpha < 0f || this.scale <= 0f) {
            this.markDead();
        }

//        float redEv = -0.03f;
//        float greenEv = 0.0f;
//        float blueEv = -0.01f;
//        red = MathHelper.clamp(red+redEv, 0, 1);
//        green = MathHelper.clamp(green+greenEv, 0, 1);
//        blue = MathHelper.clamp(blue+blueEv, 0, 1);

        red = MathHelper.clamp(red + redEvolution, 0, 1);
        green = MathHelper.clamp(green + greenEvolution, 0, 1);
        blue = MathHelper.clamp(blue + blueEvolution, 0, 1);

        this.velocityY -= 0.001;
        this.velocityX = 0;
        this.velocityZ = 0;
        this.scale = Math.max(0, this.scale - 0.005f);
        this.move(velocityX, velocityY, velocityZ);
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3d vec3d = camera.getPos();
        float f = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - vec3d.getX());
        float g = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - vec3d.getY());
        float h = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - vec3d.getZ());
        Quaternionf quaternion2;
        if (this.angle == 0.0F) {
            quaternion2 = camera.getRotation();
        } else {
            quaternion2 = new Quaternionf(camera.getRotation());
            float i = MathHelper.lerp(tickDelta, this.prevAngle, this.angle);
            quaternion2.mul(RotationAxis.POSITIVE_Z.rotation(i));
        }

        Vector3f[] Vec3fs = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float j = this.getSize(tickDelta);

        for (int k = 0; k < 4; ++k) {
            Vector3f Vec3f2 = Vec3fs[k];
            Vec3f2.rotate(quaternion2);
            Vec3f2.mul(j);
            Vec3f2.add(f, g, h);
        }

        float minU = this.getMinU();
        float maxU = this.getMaxU();
        float minV = this.getMinV();
        float maxV = this.getMaxV();
        int l = 15728880;

        vertexConsumer.vertex(Vec3fs[0].x(), Vec3fs[0].y(), Vec3fs[0].z()).texture(maxU, maxV).color(red, green, blue, alpha).light(l);
        vertexConsumer.vertex(Vec3fs[1].x(), Vec3fs[1].y(), Vec3fs[1].z()).texture(maxU, minV).color(red, green, blue, alpha).light(l);
        vertexConsumer.vertex(Vec3fs[2].x(), Vec3fs[2].y(), Vec3fs[2].z()).texture(minU, minV).color(red, green, blue, alpha).light(l);
        vertexConsumer.vertex(Vec3fs[3].x(), Vec3fs[3].y(), Vec3fs[3].z()).texture(minU, maxV).color(red, green, blue, alpha).light(l);
    }


    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Fabctory implements ParticleFactory<WispTrailParticleData> {
        private final SpriteProvider spriteSet;

        public Fabctory(SpriteProvider spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(WispTrailParticleData parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            Vec3d pos = new Vec3d(x, y, z);
            return new WispTrailParticle(world, x, y, z, velocityX, velocityY, velocityZ, parameters, this.spriteSet);
        }
    }

}

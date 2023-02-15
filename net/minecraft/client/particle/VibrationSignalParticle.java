/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class VibrationSignalParticle
extends TextureSheetParticle {
    private final PositionSource target;
    private float rot;
    private float rotO;
    private float pitch;
    private float pitchO;

    VibrationSignalParticle(ClientLevel clientLevel, double d, double e, double f, PositionSource positionSource, int i) {
        super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
        this.quadSize = 0.3f;
        this.target = positionSource;
        this.lifetime = i;
        Optional<Vec3> optional = positionSource.getPosition(clientLevel);
        if (optional.isPresent()) {
            Vec3 vec3 = optional.get();
            double g = d - vec3.x();
            double h = e - vec3.y();
            double j = f - vec3.z();
            this.rotO = this.rot = (float)Mth.atan2(g, j);
            this.pitchO = this.pitch = (float)Mth.atan2(h, Math.sqrt(g * g + j * j));
        }
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        float g = Mth.sin(((float)this.age + f - (float)Math.PI * 2) * 0.05f) * 2.0f;
        float h = Mth.lerp(f, this.rotO, this.rot);
        float i = Mth.lerp(f, this.pitchO, this.pitch) + 1.5707964f;
        this.renderSignal(vertexConsumer, camera, f, quaternionf -> quaternionf.rotateY(h).rotateX(-i).rotateY(g));
        this.renderSignal(vertexConsumer, camera, f, quaternionf -> quaternionf.rotateY((float)(-Math.PI) + h).rotateX(i).rotateY(g));
    }

    private void renderSignal(VertexConsumer vertexConsumer, Camera camera, float f, Consumer<Quaternionf> consumer) {
        Vec3 vec3 = camera.getPosition();
        float g = (float)(Mth.lerp((double)f, this.xo, this.x) - vec3.x());
        float h = (float)(Mth.lerp((double)f, this.yo, this.y) - vec3.y());
        float i = (float)(Mth.lerp((double)f, this.zo, this.z) - vec3.z());
        Vector3f vector3f = new Vector3f(0.5f, 0.5f, 0.5f).normalize();
        Quaternionf quaternionf = new Quaternionf().setAngleAxis(0.0f, vector3f.x(), vector3f.y(), vector3f.z());
        consumer.accept(quaternionf);
        Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0f, -1.0f, 0.0f), new Vector3f(-1.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(1.0f, -1.0f, 0.0f)};
        float j = this.getQuadSize(f);
        for (int k = 0; k < 4; ++k) {
            Vector3f vector3f2 = vector3fs[k];
            vector3f2.rotate(quaternionf);
            vector3f2.mul(j);
            vector3f2.add(g, h, i);
        }
        float l = this.getU0();
        float m = this.getU1();
        float n = this.getV0();
        float o = this.getV1();
        int p = this.getLightColor(f);
        vertexConsumer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).uv(m, o).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(p).endVertex();
        vertexConsumer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).uv(m, n).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(p).endVertex();
        vertexConsumer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).uv(l, n).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(p).endVertex();
        vertexConsumer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z()).uv(l, o).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(p).endVertex();
    }

    @Override
    public int getLightColor(float f) {
        return 240;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        Optional<Vec3> optional = this.target.getPosition(this.level);
        if (optional.isEmpty()) {
            this.remove();
            return;
        }
        int i = this.lifetime - this.age;
        double d = 1.0 / (double)i;
        Vec3 vec3 = optional.get();
        this.x = Mth.lerp(d, this.x, vec3.x());
        this.y = Mth.lerp(d, this.y, vec3.y());
        this.z = Mth.lerp(d, this.z, vec3.z());
        double e = this.x - vec3.x();
        double f = this.y - vec3.y();
        double g = this.z - vec3.z();
        this.rotO = this.rot;
        this.rot = (float)Mth.atan2(e, g);
        this.pitchO = this.pitch;
        this.pitch = (float)Mth.atan2(f, Math.sqrt(e * e + g * g));
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<VibrationParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(VibrationParticleOption vibrationParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            VibrationSignalParticle vibrationSignalParticle = new VibrationSignalParticle(clientLevel, d, e, f, vibrationParticleOption.getDestination(), vibrationParticleOption.getArrivalInTicks());
            vibrationSignalParticle.pickSprite(this.sprite);
            vibrationSignalParticle.setAlpha(1.0f);
            return vibrationSignalParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((VibrationParticleOption)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }
}


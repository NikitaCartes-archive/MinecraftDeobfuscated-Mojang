/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public abstract class SingleQuadParticle
extends Particle {
    protected float quadSize;

    protected SingleQuadParticle(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
        this.quadSize = 0.1f * (this.random.nextFloat() * 0.5f + 0.5f) * 2.0f;
    }

    protected SingleQuadParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
        super(clientLevel, d, e, f, g, h, i);
        this.quadSize = 0.1f * (this.random.nextFloat() * 0.5f + 0.5f) * 2.0f;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        Quaternionf quaternionf;
        Vec3 vec3 = camera.getPosition();
        float g = (float)(Mth.lerp((double)f, this.xo, this.x) - vec3.x());
        float h = (float)(Mth.lerp((double)f, this.yo, this.y) - vec3.y());
        float i = (float)(Mth.lerp((double)f, this.zo, this.z) - vec3.z());
        if (this.roll == 0.0f) {
            quaternionf = camera.rotation();
        } else {
            quaternionf = new Quaternionf(camera.rotation());
            quaternionf.rotateZ(Mth.lerp(f, this.oRoll, this.roll));
        }
        Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0f, -1.0f, 0.0f), new Vector3f(-1.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(1.0f, -1.0f, 0.0f)};
        float j = this.getQuadSize(f);
        for (int k = 0; k < 4; ++k) {
            Vector3f vector3f = vector3fs[k];
            vector3f.rotate(quaternionf);
            vector3f.mul(j);
            vector3f.add(g, h, i);
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

    public float getQuadSize(float f) {
        return this.quadSize;
    }

    @Override
    public Particle scale(float f) {
        this.quadSize *= f;
        return super.scale(f);
    }

    protected abstract float getU0();

    protected abstract float getU1();

    protected abstract float getV0();

    protected abstract float getV1();
}


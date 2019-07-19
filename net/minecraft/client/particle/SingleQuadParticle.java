/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public abstract class SingleQuadParticle
extends Particle {
    protected float quadSize;

    protected SingleQuadParticle(Level level, double d, double e, double f) {
        super(level, d, e, f);
        this.quadSize = 0.1f * (this.random.nextFloat() * 0.5f + 0.5f) * 2.0f;
    }

    protected SingleQuadParticle(Level level, double d, double e, double f, double g, double h, double i) {
        super(level, d, e, f, g, h, i);
        this.quadSize = 0.1f * (this.random.nextFloat() * 0.5f + 0.5f) * 2.0f;
    }

    @Override
    public void render(BufferBuilder bufferBuilder, Camera camera, float f, float g, float h, float i, float j, float k) {
        float l = this.getQuadSize(f);
        float m = this.getU0();
        float n = this.getU1();
        float o = this.getV0();
        float p = this.getV1();
        float q = (float)(Mth.lerp((double)f, this.xo, this.x) - xOff);
        float r = (float)(Mth.lerp((double)f, this.yo, this.y) - yOff);
        float s = (float)(Mth.lerp((double)f, this.zo, this.z) - zOff);
        int t = this.getLightColor(f);
        int u = t >> 16 & 0xFFFF;
        int v = t & 0xFFFF;
        Vec3[] vec3s = new Vec3[]{new Vec3(-g * l - j * l, -h * l, -i * l - k * l), new Vec3(-g * l + j * l, h * l, -i * l + k * l), new Vec3(g * l + j * l, h * l, i * l + k * l), new Vec3(g * l - j * l, -h * l, i * l - k * l)};
        if (this.roll != 0.0f) {
            float w = Mth.lerp(f, this.oRoll, this.roll);
            float x = Mth.cos(w * 0.5f);
            float y = (float)((double)Mth.sin(w * 0.5f) * camera.getLookVector().x);
            float z = (float)((double)Mth.sin(w * 0.5f) * camera.getLookVector().y);
            float aa = (float)((double)Mth.sin(w * 0.5f) * camera.getLookVector().z);
            Vec3 vec3 = new Vec3(y, z, aa);
            for (int ab = 0; ab < 4; ++ab) {
                vec3s[ab] = vec3.scale(2.0 * vec3s[ab].dot(vec3)).add(vec3s[ab].scale((double)(x * x) - vec3.dot(vec3))).add(vec3.cross(vec3s[ab]).scale(2.0f * x));
            }
        }
        bufferBuilder.vertex((double)q + vec3s[0].x, (double)r + vec3s[0].y, (double)s + vec3s[0].z).uv(n, p).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(u, v).endVertex();
        bufferBuilder.vertex((double)q + vec3s[1].x, (double)r + vec3s[1].y, (double)s + vec3s[1].z).uv(n, o).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(u, v).endVertex();
        bufferBuilder.vertex((double)q + vec3s[2].x, (double)r + vec3s[2].y, (double)s + vec3s[2].z).uv(m, o).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(u, v).endVertex();
        bufferBuilder.vertex((double)q + vec3s[3].x, (double)r + vec3s[3].y, (double)s + vec3s[3].z).uv(m, p).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(u, v).endVertex();
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


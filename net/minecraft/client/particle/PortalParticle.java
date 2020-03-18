/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;

@Environment(value=EnvType.CLIENT)
public class PortalParticle
extends TextureSheetParticle {
    private final double xStart;
    private final double yStart;
    private final double zStart;

    protected PortalParticle(Level level, double d, double e, double f, double g, double h, double i) {
        super(level, d, e, f);
        this.xd = g;
        this.yd = h;
        this.zd = i;
        this.x = d;
        this.y = e;
        this.z = f;
        this.xStart = this.x;
        this.yStart = this.y;
        this.zStart = this.z;
        this.quadSize = 0.1f * (this.random.nextFloat() * 0.2f + 0.5f);
        float j = this.random.nextFloat() * 0.6f + 0.4f;
        this.rCol = j * 0.9f;
        this.gCol = j * 0.3f;
        this.bCol = j;
        this.lifetime = (int)(Math.random() * 10.0) + 40;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void move(double d, double e, double f) {
        this.setBoundingBox(this.getBoundingBox().move(d, e, f));
        this.setLocationFromBoundingbox();
    }

    @Override
    public float getQuadSize(float f) {
        float g = ((float)this.age + f) / (float)this.lifetime;
        g = 1.0f - g;
        g *= g;
        g = 1.0f - g;
        return this.quadSize * g;
    }

    @Override
    public int getLightColor(float f) {
        int i = super.getLightColor(f);
        float g = (float)this.age / (float)this.lifetime;
        g *= g;
        g *= g;
        int j = i & 0xFF;
        int k = i >> 16 & 0xFF;
        if ((k += (int)(g * 15.0f * 16.0f)) > 240) {
            k = 240;
        }
        return j | k << 16;
    }

    @Override
    public void tick() {
        float f;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        float g = f = (float)this.age / (float)this.lifetime;
        f = -f + f * f * 2.0f;
        f = 1.0f - f;
        this.x = this.xStart + this.xd * (double)f;
        this.y = this.yStart + this.yd * (double)f + (double)(1.0f - g);
        this.z = this.zStart + this.zd * (double)f;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
            PortalParticle portalParticle = new PortalParticle(level, d, e, f, g, h, i);
            portalParticle.pickSprite(this.sprite);
            return portalParticle;
        }
    }
}


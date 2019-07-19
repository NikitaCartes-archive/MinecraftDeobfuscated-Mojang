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
public class ExplodeParticle
extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected ExplodeParticle(Level level, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
        super(level, d, e, f);
        float j;
        this.sprites = spriteSet;
        this.xd = g + (Math.random() * 2.0 - 1.0) * (double)0.05f;
        this.yd = h + (Math.random() * 2.0 - 1.0) * (double)0.05f;
        this.zd = i + (Math.random() * 2.0 - 1.0) * (double)0.05f;
        this.rCol = j = this.random.nextFloat() * 0.3f + 0.7f;
        this.gCol = j;
        this.bCol = j;
        this.quadSize = 0.1f * (this.random.nextFloat() * this.random.nextFloat() * 6.0f + 1.0f);
        this.lifetime = (int)(16.0 / ((double)this.random.nextFloat() * 0.8 + 0.2)) + 2;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
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
        this.setSpriteFromAge(this.sprites);
        this.yd += 0.004;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= (double)0.9f;
        this.yd *= (double)0.9f;
        this.zd *= (double)0.9f;
        if (this.onGround) {
            this.xd *= (double)0.7f;
            this.zd *= (double)0.7f;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
            return new ExplodeParticle(level, d, e, f, g, h, i, this.sprites);
        }
    }
}


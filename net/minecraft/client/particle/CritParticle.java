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
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

@Environment(value=EnvType.CLIENT)
public class CritParticle
extends TextureSheetParticle {
    private CritParticle(Level level, double d, double e, double f, double g, double h, double i) {
        super(level, d, e, f, 0.0, 0.0, 0.0);
        float j;
        this.xd *= (double)0.1f;
        this.yd *= (double)0.1f;
        this.zd *= (double)0.1f;
        this.xd += g * 0.4;
        this.yd += h * 0.4;
        this.zd += i * 0.4;
        this.rCol = j = (float)(Math.random() * (double)0.3f + (double)0.6f);
        this.gCol = j;
        this.bCol = j;
        this.quadSize *= 0.75f;
        this.lifetime = Math.max((int)(6.0 / (Math.random() * 0.8 + 0.6)), 1);
        this.hasPhysics = false;
        this.tick();
    }

    @Override
    public float getQuadSize(float f) {
        return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0f, 0.0f, 1.0f);
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
        this.move(this.xd, this.yd, this.zd);
        this.gCol = (float)((double)this.gCol * 0.96);
        this.bCol = (float)((double)this.bCol * 0.9);
        this.xd *= (double)0.7f;
        this.yd *= (double)0.7f;
        this.zd *= (double)0.7f;
        this.yd -= (double)0.02f;
        if (this.onGround) {
            this.xd *= (double)0.7f;
            this.zd *= (double)0.7f;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Environment(value=EnvType.CLIENT)
    public static class DamageIndicatorProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public DamageIndicatorProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
            CritParticle critParticle = new CritParticle(level, d, e, f, g, h + 1.0, i);
            critParticle.setLifetime(20);
            critParticle.pickSprite(this.sprite);
            return critParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class MagicProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public MagicProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
            CritParticle critParticle = new CritParticle(level, d, e, f, g, h, i);
            critParticle.rCol *= 0.3f;
            critParticle.gCol *= 0.8f;
            critParticle.pickSprite(this.sprite);
            return critParticle;
        }
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
            CritParticle critParticle = new CritParticle(level, d, e, f, g, h, i);
            critParticle.pickSprite(this.sprite);
            return critParticle;
        }
    }
}


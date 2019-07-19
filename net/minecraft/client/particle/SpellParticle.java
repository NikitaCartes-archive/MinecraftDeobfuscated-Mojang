/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import java.util.Random;
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
public class SpellParticle
extends TextureSheetParticle {
    private static final Random RANDOM = new Random();
    private final SpriteSet sprites;

    private SpellParticle(Level level, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
        super(level, d, e, f, 0.5 - RANDOM.nextDouble(), h, 0.5 - RANDOM.nextDouble());
        this.sprites = spriteSet;
        this.yd *= (double)0.2f;
        if (g == 0.0 && i == 0.0) {
            this.xd *= (double)0.1f;
            this.zd *= (double)0.1f;
        }
        this.quadSize *= 0.75f;
        this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.setSpriteFromAge(spriteSet);
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
        this.setSpriteFromAge(this.sprites);
        this.yd += 0.004;
        this.move(this.xd, this.yd, this.zd);
        if (this.y == this.yo) {
            this.xd *= 1.1;
            this.zd *= 1.1;
        }
        this.xd *= (double)0.96f;
        this.yd *= (double)0.96f;
        this.zd *= (double)0.96f;
        if (this.onGround) {
            this.xd *= (double)0.7f;
            this.zd *= (double)0.7f;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class InstantProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public InstantProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
            return new SpellParticle(level, d, e, f, g, h, i, this.sprite);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class WitchProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WitchProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
            SpellParticle spellParticle = new SpellParticle(level, d, e, f, g, h, i, this.sprite);
            float j = level.random.nextFloat() * 0.5f + 0.35f;
            spellParticle.setColor(1.0f * j, 0.0f * j, 1.0f * j);
            return spellParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class AmbientMobProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public AmbientMobProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
            SpellParticle particle = new SpellParticle(level, d, e, f, g, h, i, this.sprite);
            particle.setAlpha(0.15f);
            particle.setColor((float)g, (float)h, (float)i);
            return particle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class MobProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public MobProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
            SpellParticle particle = new SpellParticle(level, d, e, f, g, h, i, this.sprite);
            particle.setColor((float)g, (float)h, (float)i);
            return particle;
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
            return new SpellParticle(level, d, e, f, g, h, i, this.sprite);
        }
    }
}


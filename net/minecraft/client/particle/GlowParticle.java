/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class GlowParticle
extends TextureSheetParticle {
    static final Random RANDOM = new Random();
    private final SpriteSet sprites;

    GlowParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
        super(clientLevel, d, e, f, g, h, i);
        this.friction = 0.96f;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = spriteSet;
        this.quadSize *= 0.75f;
        this.hasPhysics = false;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float f) {
        float g = ((float)this.age + f) / (float)this.lifetime;
        g = Mth.clamp(g, 0.0f, 1.0f);
        int i = super.getLightColor(f);
        int j = i & 0xFF;
        int k = i >> 16 & 0xFF;
        if ((j += (int)(g * 15.0f * 16.0f)) > 240) {
            j = 240;
        }
        return j | k << 16;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @Environment(value=EnvType.CLIENT)
    public static class ScrapeProvider
    implements ParticleProvider<SimpleParticleType> {
        private final double SPEED_FACTOR = 0.01;
        private final SpriteSet sprite;

        public ScrapeProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            GlowParticle glowParticle = new GlowParticle(clientLevel, d, e, f, 0.0, 0.0, 0.0, this.sprite);
            if (clientLevel.random.nextBoolean()) {
                glowParticle.setColor(0.29f, 0.58f, 0.51f);
            } else {
                glowParticle.setColor(0.43f, 0.77f, 0.62f);
            }
            glowParticle.setParticleSpeed(g * 0.01, h * 0.01, i * 0.01);
            int j = 10;
            int k = 40;
            glowParticle.setLifetime(clientLevel.random.nextInt(30) + 10);
            return glowParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ElectricSparkProvider
    implements ParticleProvider<SimpleParticleType> {
        private final double SPEED_FACTOR = 0.25;
        private final SpriteSet sprite;

        public ElectricSparkProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            GlowParticle glowParticle = new GlowParticle(clientLevel, d, e, f, 0.0, 0.0, 0.0, this.sprite);
            glowParticle.setColor(1.0f, 0.9f, 1.0f);
            glowParticle.setParticleSpeed(g * 0.25, h * 0.25, i * 0.25);
            int j = 2;
            int k = 4;
            glowParticle.setLifetime(clientLevel.random.nextInt(2) + 2);
            return glowParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class WaxOffProvider
    implements ParticleProvider<SimpleParticleType> {
        private final double SPEED_FACTOR = 0.01;
        private final SpriteSet sprite;

        public WaxOffProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            GlowParticle glowParticle = new GlowParticle(clientLevel, d, e, f, 0.0, 0.0, 0.0, this.sprite);
            glowParticle.setColor(1.0f, 0.9f, 1.0f);
            glowParticle.setParticleSpeed(g * 0.01 / 2.0, h * 0.01, i * 0.01 / 2.0);
            int j = 10;
            int k = 40;
            glowParticle.setLifetime(clientLevel.random.nextInt(30) + 10);
            return glowParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class WaxOnProvider
    implements ParticleProvider<SimpleParticleType> {
        private final double SPEED_FACTOR = 0.01;
        private final SpriteSet sprite;

        public WaxOnProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            GlowParticle glowParticle = new GlowParticle(clientLevel, d, e, f, 0.0, 0.0, 0.0, this.sprite);
            glowParticle.setColor(0.91f, 0.55f, 0.08f);
            glowParticle.setParticleSpeed(g * 0.01 / 2.0, h * 0.01, i * 0.01 / 2.0);
            int j = 10;
            int k = 40;
            glowParticle.setLifetime(clientLevel.random.nextInt(30) + 10);
            return glowParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class GlowSquidProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public GlowSquidProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            GlowParticle glowParticle = new GlowParticle(clientLevel, d, e, f, 0.5 - RANDOM.nextDouble(), h, 0.5 - RANDOM.nextDouble(), this.sprite);
            if (clientLevel.random.nextBoolean()) {
                glowParticle.setColor(0.6f, 1.0f, 0.8f);
            } else {
                glowParticle.setColor(0.08f, 0.4f, 0.4f);
            }
            glowParticle.yd *= (double)0.2f;
            if (g == 0.0 && i == 0.0) {
                glowParticle.xd *= (double)0.1f;
                glowParticle.zd *= (double)0.1f;
            }
            glowParticle.setLifetime((int)(8.0 / (clientLevel.random.nextDouble() * 0.8 + 0.2)));
            return glowParticle;
        }
    }
}


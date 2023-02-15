/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

@Environment(value=EnvType.CLIENT)
public class SuspendedParticle
extends TextureSheetParticle {
    SuspendedParticle(ClientLevel clientLevel, SpriteSet spriteSet, double d, double e, double f) {
        super(clientLevel, d, e - 0.125, f);
        this.setSize(0.01f, 0.01f);
        this.pickSprite(spriteSet);
        this.quadSize *= this.random.nextFloat() * 0.6f + 0.2f;
        this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.friction = 1.0f;
        this.gravity = 0.0f;
    }

    SuspendedParticle(ClientLevel clientLevel, SpriteSet spriteSet, double d, double e, double f, double g, double h, double i) {
        super(clientLevel, d, e - 0.125, f, g, h, i);
        this.setSize(0.01f, 0.01f);
        this.pickSprite(spriteSet);
        this.quadSize *= this.random.nextFloat() * 0.6f + 0.6f;
        this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.friction = 1.0f;
        this.gravity = 0.0f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Environment(value=EnvType.CLIENT)
    public static class WarpedSporeProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WarpedSporeProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            double j = (double)clientLevel.random.nextFloat() * -1.9 * (double)clientLevel.random.nextFloat() * 0.1;
            SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, this.sprite, d, e, f, 0.0, j, 0.0);
            suspendedParticle.setColor(0.1f, 0.1f, 0.3f);
            suspendedParticle.setSize(0.001f, 0.001f);
            return suspendedParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class CrimsonSporeProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public CrimsonSporeProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            RandomSource randomSource = clientLevel.random;
            double j = randomSource.nextGaussian() * (double)1.0E-6f;
            double k = randomSource.nextGaussian() * (double)1.0E-4f;
            double l = randomSource.nextGaussian() * (double)1.0E-6f;
            SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, this.sprite, d, e, f, j, k, l);
            suspendedParticle.setColor(0.9f, 0.4f, 0.5f);
            return suspendedParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class SporeBlossomAirProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public SporeBlossomAirProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, this.sprite, d, e, f, 0.0, -0.8f, 0.0){

                @Override
                public Optional<ParticleGroup> getParticleGroup() {
                    return Optional.of(ParticleGroup.SPORE_BLOSSOM);
                }
            };
            suspendedParticle.lifetime = Mth.randomBetweenInclusive(clientLevel.random, 500, 1000);
            suspendedParticle.gravity = 0.01f;
            suspendedParticle.setColor(0.32f, 0.5f, 0.22f);
            return suspendedParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class UnderwaterProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public UnderwaterProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, this.sprite, d, e, f);
            suspendedParticle.setColor(0.4f, 0.4f, 0.7f);
            return suspendedParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }
}


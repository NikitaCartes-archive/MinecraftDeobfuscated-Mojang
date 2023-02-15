/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BaseAshSmokeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

@Environment(value=EnvType.CLIENT)
public class WhiteAshParticle
extends BaseAshSmokeParticle {
    private static final int COLOR_RGB24 = 12235202;

    protected WhiteAshParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, float j, SpriteSet spriteSet) {
        super(clientLevel, d, e, f, 0.1f, -0.1f, 0.1f, g, h, i, j, spriteSet, 0.0f, 20, 0.0125f, false);
        this.rCol = 0.7294118f;
        this.gCol = 0.69411767f;
        this.bCol = 0.7607843f;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            RandomSource randomSource = clientLevel.random;
            double j = (double)randomSource.nextFloat() * -1.9 * (double)randomSource.nextFloat() * 0.1;
            double k = (double)randomSource.nextFloat() * -0.5 * (double)randomSource.nextFloat() * 0.1 * 5.0;
            double l = (double)randomSource.nextFloat() * -1.9 * (double)randomSource.nextFloat() * 0.1;
            return new WhiteAshParticle(clientLevel, d, e, f, j, k, l, 1.0f, this.sprites);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }
}


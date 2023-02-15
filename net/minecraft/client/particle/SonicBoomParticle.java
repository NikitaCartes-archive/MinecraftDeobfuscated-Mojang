/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(value=EnvType.CLIENT)
public class SonicBoomParticle
extends HugeExplosionParticle {
    protected SonicBoomParticle(ClientLevel clientLevel, double d, double e, double f, double g, SpriteSet spriteSet) {
        super(clientLevel, d, e, f, g, spriteSet);
        this.lifetime = 16;
        this.quadSize = 1.5f;
        this.setSpriteFromAge(spriteSet);
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
            return new SonicBoomParticle(clientLevel, d, e, f, g, this.sprites);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }
}


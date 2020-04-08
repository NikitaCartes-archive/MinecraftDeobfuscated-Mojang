/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.BaseAshSmokeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;

@Environment(value=EnvType.CLIENT)
public class WhiteAshParticle
extends BaseAshSmokeParticle {
    protected WhiteAshParticle(Level level, double d, double e, double f, double g, double h, double i, float j, SpriteSet spriteSet) {
        super(level, d, e, f, 0.1f, -0.1f, 0.1f, g, h, i, j, spriteSet, 0.0f, 20, -5.0E-4, false);
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
        public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
            return new WhiteAshParticle(level, d, e, f, g, h, i, 1.0f, this.sprites);
        }
    }
}


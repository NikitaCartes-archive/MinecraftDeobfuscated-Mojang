/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ExplodeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;

@Environment(value=EnvType.CLIENT)
public class SpitParticle
extends ExplodeParticle {
    private SpitParticle(Level level, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
        super(level, d, e, f, g, h, i, spriteSet);
        this.gravity = 0.5f;
    }

    @Override
    public void tick() {
        super.tick();
        this.yd -= 0.004 + 0.04 * (double)this.gravity;
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
            return new SpitParticle(level, d, e, f, g, h, i, this.sprites);
        }
    }
}


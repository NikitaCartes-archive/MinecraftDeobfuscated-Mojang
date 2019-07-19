/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;

@Environment(value=EnvType.CLIENT)
public class TotemParticle
extends SimpleAnimatedParticle {
    private TotemParticle(Level level, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
        super(level, d, e, f, spriteSet, -0.05f);
        this.xd = g;
        this.yd = h;
        this.zd = i;
        this.quadSize *= 0.75f;
        this.lifetime = 60 + this.random.nextInt(12);
        this.setSpriteFromAge(spriteSet);
        if (this.random.nextInt(4) == 0) {
            this.setColor(0.6f + this.random.nextFloat() * 0.2f, 0.6f + this.random.nextFloat() * 0.3f, this.random.nextFloat() * 0.2f);
        } else {
            this.setColor(0.1f + this.random.nextFloat() * 0.2f, 0.4f + this.random.nextFloat() * 0.3f, this.random.nextFloat() * 0.2f);
        }
        this.setBaseAirFriction(0.6f);
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
            return new TotemParticle(level, d, e, f, g, h, i, this.sprites);
        }
    }
}


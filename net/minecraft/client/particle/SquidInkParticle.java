/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;

@Environment(value=EnvType.CLIENT)
public class SquidInkParticle
extends SimpleAnimatedParticle {
    SquidInkParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, int j, SpriteSet spriteSet) {
        super(clientLevel, d, e, f, spriteSet, 0.0f);
        this.friction = 0.92f;
        this.quadSize = 0.5f;
        this.setAlpha(1.0f);
        this.setColor(FastColor.ARGB32.red(j), FastColor.ARGB32.green(j), FastColor.ARGB32.blue(j));
        this.lifetime = (int)((double)(this.quadSize * 12.0f) / (Math.random() * (double)0.8f + (double)0.2f));
        this.setSpriteFromAge(spriteSet);
        this.hasPhysics = false;
        this.xd = g;
        this.yd = h;
        this.zd = i;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSpriteFromAge(this.sprites);
            if (this.age > this.lifetime / 2) {
                this.setAlpha(1.0f - ((float)this.age - (float)(this.lifetime / 2)) / (float)this.lifetime);
            }
            if (this.level.getBlockState(new BlockPos(this.x, this.y, this.z)).isAir()) {
                this.yd -= (double)0.0074f;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class GlowInkProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public GlowInkProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return new SquidInkParticle(clientLevel, d, e, f, g, h, i, FastColor.ARGB32.color(255, 204, 31, 102), this.sprites);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, e, f, g, h, i);
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
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return new SquidInkParticle(clientLevel, d, e, f, g, h, i, FastColor.ARGB32.color(255, 255, 255, 255), this.sprites);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }
}


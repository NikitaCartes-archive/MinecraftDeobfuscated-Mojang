/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(value=EnvType.CLIENT)
public class SnowflakeParticle
extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected SnowflakeParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
        super(clientLevel, d, e, f);
        this.gravity = 0.225f;
        this.friction = 1.0f;
        this.sprites = spriteSet;
        this.xd = g + (Math.random() * 2.0 - 1.0) * (double)0.05f;
        this.yd = h + (Math.random() * 2.0 - 1.0) * (double)0.05f;
        this.zd = i + (Math.random() * 2.0 - 1.0) * (double)0.05f;
        this.quadSize = 0.1f * (this.random.nextFloat() * this.random.nextFloat() * 1.0f + 1.0f);
        this.lifetime = (int)(16.0 / ((double)this.random.nextFloat() * 0.8 + 0.2)) + 2;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        this.xd *= (double)0.95f;
        this.yd *= (double)0.9f;
        this.zd *= (double)0.95f;
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
            SnowflakeParticle snowflakeParticle = new SnowflakeParticle(clientLevel, d, e, f, g, h, i, this.sprites);
            snowflakeParticle.setColor(0.923f, 0.964f, 0.999f);
            return snowflakeParticle;
        }
    }
}


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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(value=EnvType.CLIENT)
public class HugeExplosionParticle
extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected HugeExplosionParticle(ClientLevel clientLevel, double d, double e, double f, double g, SpriteSet spriteSet) {
        super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
        float h;
        this.lifetime = 6 + this.random.nextInt(4);
        this.rCol = h = this.random.nextFloat() * 0.6f + 0.4f;
        this.gCol = h;
        this.bCol = h;
        this.quadSize = 2.0f * (1.0f - (float)g * 0.5f);
        this.sprites = spriteSet;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public int getLightColor(float f) {
        return 0xF000F0;
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
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
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
            return new HugeExplosionParticle(clientLevel, d, e, f, g, this.sprites);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }
}


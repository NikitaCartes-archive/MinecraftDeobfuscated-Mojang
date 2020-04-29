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
public class WakeParticle
extends TextureSheetParticle {
    private final SpriteSet sprites;

    private WakeParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
        super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
        this.sprites = spriteSet;
        this.xd *= (double)0.3f;
        this.yd = Math.random() * (double)0.2f + (double)0.1f;
        this.zd *= (double)0.3f;
        this.setSize(0.01f, 0.01f);
        this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
        this.setSpriteFromAge(spriteSet);
        this.gravity = 0.0f;
        this.xd = g;
        this.yd = h;
        this.zd = i;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        int i = 60 - this.lifetime;
        if (this.lifetime-- <= 0) {
            this.remove();
            return;
        }
        this.yd -= (double)this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= (double)0.98f;
        this.yd *= (double)0.98f;
        this.zd *= (double)0.98f;
        float f = (float)i * 0.001f;
        this.setSize(f, f);
        this.setSprite(this.sprites.get(i % 4, 4));
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
            return new WakeParticle(clientLevel, d, e, f, g, h, i, this.sprites);
        }
    }
}


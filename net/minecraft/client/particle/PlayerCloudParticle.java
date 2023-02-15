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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

@Environment(value=EnvType.CLIENT)
public class PlayerCloudParticle
extends TextureSheetParticle {
    private final SpriteSet sprites;

    PlayerCloudParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
        super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
        float k;
        this.friction = 0.96f;
        this.sprites = spriteSet;
        float j = 2.5f;
        this.xd *= (double)0.1f;
        this.yd *= (double)0.1f;
        this.zd *= (double)0.1f;
        this.xd += g;
        this.yd += h;
        this.zd += i;
        this.rCol = k = 1.0f - (float)(Math.random() * (double)0.3f);
        this.gCol = k;
        this.bCol = k;
        this.quadSize *= 1.875f;
        int l = (int)(8.0 / (Math.random() * 0.8 + 0.3));
        this.lifetime = (int)Math.max((float)l * 2.5f, 1.0f);
        this.hasPhysics = false;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float f) {
        return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0f, 0.0f, 1.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            double d;
            this.setSpriteFromAge(this.sprites);
            Player player = this.level.getNearestPlayer(this.x, this.y, this.z, 2.0, false);
            if (player != null && this.y > (d = player.getY())) {
                this.y += (d - this.y) * 0.2;
                this.yd += (player.getDeltaMovement().y - this.yd) * 0.2;
                this.setPos(this.x, this.y, this.z);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class SneezeProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public SneezeProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            PlayerCloudParticle particle = new PlayerCloudParticle(clientLevel, d, e, f, g, h, i, this.sprites);
            particle.setColor(200.0f, 50.0f, 120.0f);
            particle.setAlpha(0.4f);
            return particle;
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
            return new PlayerCloudParticle(clientLevel, d, e, f, g, h, i, this.sprites);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }
}


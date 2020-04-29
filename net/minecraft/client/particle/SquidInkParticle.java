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
import net.minecraft.core.particles.SimpleParticleType;

@Environment(value=EnvType.CLIENT)
public class SquidInkParticle
extends SimpleAnimatedParticle {
    private SquidInkParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
        super(clientLevel, d, e, f, spriteSet, 0.0f);
        this.quadSize = 0.5f;
        this.setAlpha(1.0f);
        this.setColor(0.0f, 0.0f, 0.0f);
        this.lifetime = (int)((double)(this.quadSize * 12.0f) / (Math.random() * (double)0.8f + (double)0.2f));
        this.setSpriteFromAge(spriteSet);
        this.hasPhysics = false;
        this.xd = g;
        this.yd = h;
        this.zd = i;
        this.setBaseAirFriction(0.0f);
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
        if (this.age > this.lifetime / 2) {
            this.setAlpha(1.0f - ((float)this.age - (float)(this.lifetime / 2)) / (float)this.lifetime);
        }
        this.move(this.xd, this.yd, this.zd);
        if (this.level.getBlockState(new BlockPos(this.x, this.y, this.z)).isAir()) {
            this.yd -= (double)0.008f;
        }
        this.xd *= (double)0.92f;
        this.yd *= (double)0.92f;
        this.zd *= (double)0.92f;
        if (this.onGround) {
            this.xd *= (double)0.7f;
            this.zd *= (double)0.7f;
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
            return new SquidInkParticle(clientLevel, d, e, f, g, h, i, this.sprites);
        }
    }
}


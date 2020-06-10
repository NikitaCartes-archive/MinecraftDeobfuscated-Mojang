/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import java.util.Random;
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
public class SuspendedParticle
extends TextureSheetParticle {
    private SuspendedParticle(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e - 0.125, f);
        this.rCol = 0.4f;
        this.gCol = 0.4f;
        this.bCol = 0.7f;
        this.setSize(0.01f, 0.01f);
        this.quadSize *= this.random.nextFloat() * 0.6f + 0.2f;
        this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
    }

    private SuspendedParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
        super(clientLevel, d, e - 0.125, f, g, h, i);
        this.setSize(0.01f, 0.01f);
        this.quadSize *= this.random.nextFloat() * 0.6f + 0.6f;
        this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
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
        if (this.lifetime-- <= 0) {
            this.remove();
            return;
        }
        this.move(this.xd, this.yd, this.zd);
    }

    @Environment(value=EnvType.CLIENT)
    public static class WarpedSporeProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WarpedSporeProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            double j = (double)clientLevel.random.nextFloat() * -1.9 * (double)clientLevel.random.nextFloat() * 0.1;
            SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, d, e, f, 0.0, j, 0.0);
            suspendedParticle.pickSprite(this.sprite);
            suspendedParticle.setColor(0.1f, 0.1f, 0.3f);
            suspendedParticle.setSize(0.001f, 0.001f);
            return suspendedParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class CrimsonSporeProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public CrimsonSporeProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            Random random = clientLevel.random;
            double j = random.nextGaussian() * (double)1.0E-6f;
            double k = random.nextGaussian() * (double)1.0E-4f;
            double l = random.nextGaussian() * (double)1.0E-6f;
            SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, d, e, f, j, k, l);
            suspendedParticle.pickSprite(this.sprite);
            suspendedParticle.setColor(0.9f, 0.4f, 0.5f);
            return suspendedParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class UnderwaterProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public UnderwaterProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, d, e, f);
            suspendedParticle.pickSprite(this.sprite);
            return suspendedParticle;
        }
    }
}


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
public class SuspendedTownParticle
extends TextureSheetParticle {
    SuspendedTownParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
        super(clientLevel, d, e, f, g, h, i);
        float j;
        this.rCol = j = this.random.nextFloat() * 0.1f + 0.2f;
        this.gCol = j;
        this.bCol = j;
        this.setSize(0.02f, 0.02f);
        this.quadSize *= this.random.nextFloat() * 0.6f + 0.5f;
        this.xd *= (double)0.02f;
        this.yd *= (double)0.02f;
        this.zd *= (double)0.02f;
        this.lifetime = (int)(20.0 / (Math.random() * 0.8 + 0.2));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void move(double d, double e, double f) {
        this.setBoundingBox(this.getBoundingBox().move(d, e, f));
        this.setLocationFromBoundingbox();
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
        this.xd *= 0.99;
        this.yd *= 0.99;
        this.zd *= 0.99;
    }

    @Environment(value=EnvType.CLIENT)
    public static class DolphinSpeedProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public DolphinSpeedProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            SuspendedTownParticle suspendedTownParticle = new SuspendedTownParticle(clientLevel, d, e, f, g, h, i);
            suspendedTownParticle.setColor(0.3f, 0.5f, 1.0f);
            suspendedTownParticle.pickSprite(this.sprite);
            suspendedTownParticle.setAlpha(1.0f - clientLevel.random.nextFloat() * 0.7f);
            suspendedTownParticle.setLifetime(suspendedTownParticle.getLifetime() / 2);
            return suspendedTownParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ComposterFillProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public ComposterFillProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            SuspendedTownParticle suspendedTownParticle = new SuspendedTownParticle(clientLevel, d, e, f, g, h, i);
            suspendedTownParticle.pickSprite(this.sprite);
            suspendedTownParticle.setColor(1.0f, 1.0f, 1.0f);
            suspendedTownParticle.setLifetime(3 + clientLevel.getRandom().nextInt(5));
            return suspendedTownParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class HappyVillagerProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public HappyVillagerProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            SuspendedTownParticle suspendedTownParticle = new SuspendedTownParticle(clientLevel, d, e, f, g, h, i);
            suspendedTownParticle.pickSprite(this.sprite);
            suspendedTownParticle.setColor(1.0f, 1.0f, 1.0f);
            return suspendedTownParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            SuspendedTownParticle suspendedTownParticle = new SuspendedTownParticle(clientLevel, d, e, f, g, h, i);
            suspendedTownParticle.pickSprite(this.sprite);
            return suspendedTownParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }
}


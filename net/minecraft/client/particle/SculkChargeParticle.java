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
import net.minecraft.core.particles.SculkChargeParticleOptions;

@Environment(value=EnvType.CLIENT)
public class SculkChargeParticle
extends TextureSheetParticle {
    private final SpriteSet sprites;

    SculkChargeParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
        super(clientLevel, d, e, f, g, h, i);
        this.friction = 0.96f;
        this.sprites = spriteSet;
        this.scale(1.5f);
        this.hasPhysics = false;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public int getLightColor(float f) {
        return 240;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @Environment(value=EnvType.CLIENT)
    public record Provider(SpriteSet sprite) implements ParticleProvider<SculkChargeParticleOptions>
    {
        @Override
        public Particle createParticle(SculkChargeParticleOptions sculkChargeParticleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            SculkChargeParticle sculkChargeParticle = new SculkChargeParticle(clientLevel, d, e, f, g, h, i, this.sprite);
            sculkChargeParticle.setAlpha(1.0f);
            sculkChargeParticle.setParticleSpeed(g, h, i);
            sculkChargeParticle.oRoll = sculkChargeParticleOptions.roll();
            sculkChargeParticle.roll = sculkChargeParticleOptions.roll();
            sculkChargeParticle.setLifetime(clientLevel.random.nextInt(12) + 8);
            return sculkChargeParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((SculkChargeParticleOptions)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }
}


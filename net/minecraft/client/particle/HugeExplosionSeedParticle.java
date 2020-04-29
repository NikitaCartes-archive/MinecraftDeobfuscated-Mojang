/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(value=EnvType.CLIENT)
public class HugeExplosionSeedParticle
extends NoRenderParticle {
    private int life;
    private final int lifeTime;

    private HugeExplosionSeedParticle(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
        this.lifeTime = 8;
    }

    @Override
    public void tick() {
        for (int i = 0; i < 6; ++i) {
            double d = this.x + (this.random.nextDouble() - this.random.nextDouble()) * 4.0;
            double e = this.y + (this.random.nextDouble() - this.random.nextDouble()) * 4.0;
            double f = this.z + (this.random.nextDouble() - this.random.nextDouble()) * 4.0;
            this.level.addParticle(ParticleTypes.EXPLOSION, d, e, f, (float)this.life / (float)this.lifeTime, 0.0, 0.0);
        }
        ++this.life;
        if (this.life == this.lifeTime) {
            this.remove();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return new HugeExplosionSeedParticle(clientLevel, d, e, f);
        }
    }
}


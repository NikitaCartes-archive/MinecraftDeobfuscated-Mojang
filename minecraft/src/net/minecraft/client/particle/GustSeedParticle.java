package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class GustSeedParticle extends NoRenderParticle {
	GustSeedParticle(ClientLevel clientLevel, double d, double e, double f) {
		super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
		this.lifetime = 7;
	}

	@Override
	public void tick() {
		for (int i = 0; i < 3; i++) {
			double d = this.x + (this.random.nextDouble() - this.random.nextDouble()) * 4.0;
			double e = this.y + (this.random.nextDouble() - this.random.nextDouble()) * 4.0;
			double f = this.z + (this.random.nextDouble() - this.random.nextDouble()) * 4.0;
			this.level.addParticle(ParticleTypes.GUST, d, e, f, (double)((float)this.age / (float)this.lifetime), 0.0, 0.0);
		}

		if (this.age++ == this.lifetime) {
			this.remove();
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new GustSeedParticle(clientLevel, d, e, f);
		}
	}
}

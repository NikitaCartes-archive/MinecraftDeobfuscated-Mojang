package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class GustSeedParticle extends NoRenderParticle {
	private final double scale;
	private final int tickDelayInBetween;

	GustSeedParticle(ClientLevel clientLevel, double d, double e, double f, double g, int i, int j) {
		super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
		this.scale = g;
		this.lifetime = i;
		this.tickDelayInBetween = j;
	}

	@Override
	public void tick() {
		if (this.age % (this.tickDelayInBetween + 1) == 0) {
			for (int i = 0; i < 3; i++) {
				double d = this.x + (this.random.nextDouble() - this.random.nextDouble()) * this.scale;
				double e = this.y + (this.random.nextDouble() - this.random.nextDouble()) * this.scale;
				double f = this.z + (this.random.nextDouble() - this.random.nextDouble()) * this.scale;
				this.level.addParticle(ParticleTypes.GUST, d, e, f, (double)((float)this.age / (float)this.lifetime), 0.0, 0.0);
			}
		}

		if (this.age++ == this.lifetime) {
			this.remove();
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final double scale;
		private final int lifetime;
		private final int tickDelayInBetween;

		public Provider(double d, int i, int j) {
			this.scale = d;
			this.lifetime = i;
			this.tickDelayInBetween = j;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new GustSeedParticle(clientLevel, d, e, f, this.scale, this.lifetime, this.tickDelayInBetween);
		}
	}
}

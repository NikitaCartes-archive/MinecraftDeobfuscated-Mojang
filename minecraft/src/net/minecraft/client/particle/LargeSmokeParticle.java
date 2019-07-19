package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class LargeSmokeParticle extends SmokeParticle {
	protected LargeSmokeParticle(Level level, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
		super(level, d, e, f, g, h, i, 2.5F, spriteSet);
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
			return new LargeSmokeParticle(level, d, e, f, g, h, i, this.sprites);
		}
	}
}

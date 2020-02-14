package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class AshParticle extends BaseAshSmokeParticle {
	protected AshParticle(Level level, double d, double e, double f, double g, double h, double i, float j, SpriteSet spriteSet) {
		super(level, d, e, f, 0.1F, -0.1F, 0.1F, g, h, i, j, spriteSet, 0.5F, 20, -0.004, false);
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
			return new AshParticle(level, d, e, f, g, h, i, 1.0F, this.sprites);
		}
	}
}

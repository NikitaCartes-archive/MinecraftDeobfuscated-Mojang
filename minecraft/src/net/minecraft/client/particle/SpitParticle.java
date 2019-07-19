package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class SpitParticle extends ExplodeParticle {
	private SpitParticle(Level level, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
		super(level, d, e, f, g, h, i, spriteSet);
		this.gravity = 0.5F;
	}

	@Override
	public void tick() {
		super.tick();
		this.yd = this.yd - (0.004 + 0.04 * (double)this.gravity);
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
			return new SpitParticle(level, d, e, f, g, h, i, this.sprites);
		}
	}
}

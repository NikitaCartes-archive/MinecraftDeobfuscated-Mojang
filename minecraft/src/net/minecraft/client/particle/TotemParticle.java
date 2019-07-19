package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class TotemParticle extends SimpleAnimatedParticle {
	private TotemParticle(Level level, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
		super(level, d, e, f, spriteSet, -0.05F);
		this.xd = g;
		this.yd = h;
		this.zd = i;
		this.quadSize *= 0.75F;
		this.lifetime = 60 + this.random.nextInt(12);
		this.setSpriteFromAge(spriteSet);
		if (this.random.nextInt(4) == 0) {
			this.setColor(0.6F + this.random.nextFloat() * 0.2F, 0.6F + this.random.nextFloat() * 0.3F, this.random.nextFloat() * 0.2F);
		} else {
			this.setColor(0.1F + this.random.nextFloat() * 0.2F, 0.4F + this.random.nextFloat() * 0.3F, this.random.nextFloat() * 0.2F);
		}

		this.setBaseAirFriction(0.6F);
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
			return new TotemParticle(level, d, e, f, g, h, i, this.sprites);
		}
	}
}

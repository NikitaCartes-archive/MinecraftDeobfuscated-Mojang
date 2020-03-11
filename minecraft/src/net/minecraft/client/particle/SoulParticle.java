package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class SoulParticle extends RisingParticle {
	private final SpriteSet sprites;

	private SoulParticle(Level level, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
		super(level, d, e, f, g, h, i);
		this.sprites = spriteSet;
		this.scale(1.5F);
		this.setSpriteFromAge(spriteSet);
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

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
			SoulParticle soulParticle = new SoulParticle(level, d, e, f, g, h, i, this.sprite);
			soulParticle.setAlpha(1.0F);
			soulParticle.pickSprite(this.sprite);
			return soulParticle;
		}
	}
}

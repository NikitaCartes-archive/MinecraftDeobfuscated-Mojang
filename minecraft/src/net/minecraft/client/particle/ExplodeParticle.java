package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class ExplodeParticle extends TextureSheetParticle {
	private final SpriteSet sprites;

	protected ExplodeParticle(Level level, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
		super(level, d, e, f);
		this.sprites = spriteSet;
		this.xd = g + (Math.random() * 2.0 - 1.0) * 0.05F;
		this.yd = h + (Math.random() * 2.0 - 1.0) * 0.05F;
		this.zd = i + (Math.random() * 2.0 - 1.0) * 0.05F;
		float j = this.random.nextFloat() * 0.3F + 0.7F;
		this.rCol = j;
		this.gCol = j;
		this.bCol = j;
		this.quadSize = 0.1F * (this.random.nextFloat() * this.random.nextFloat() * 6.0F + 1.0F);
		this.lifetime = (int)(16.0 / ((double)this.random.nextFloat() * 0.8 + 0.2)) + 2;
		this.setSpriteFromAge(spriteSet);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			this.setSpriteFromAge(this.sprites);
			this.yd += 0.004;
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.9F;
			this.yd *= 0.9F;
			this.zd *= 0.9F;
			if (this.onGround) {
				this.xd *= 0.7F;
				this.zd *= 0.7F;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
			return new ExplodeParticle(level, d, e, f, g, h, i, this.sprites);
		}
	}
}

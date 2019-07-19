package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class SmokeParticle extends TextureSheetParticle {
	private final SpriteSet sprites;

	protected SmokeParticle(Level level, double d, double e, double f, double g, double h, double i, float j, SpriteSet spriteSet) {
		super(level, d, e, f, 0.0, 0.0, 0.0);
		this.sprites = spriteSet;
		this.xd *= 0.1F;
		this.yd *= 0.1F;
		this.zd *= 0.1F;
		this.xd += g;
		this.yd += h;
		this.zd += i;
		float k = (float)(Math.random() * 0.3F);
		this.rCol = k;
		this.gCol = k;
		this.bCol = k;
		this.quadSize *= 0.75F * j;
		this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
		this.lifetime = (int)((float)this.lifetime * j);
		this.lifetime = Math.max(this.lifetime, 1);
		this.setSpriteFromAge(spriteSet);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public float getQuadSize(float f) {
		return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
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
			if (this.y == this.yo) {
				this.xd *= 1.1;
				this.zd *= 1.1;
			}

			this.xd *= 0.96F;
			this.yd *= 0.96F;
			this.zd *= 0.96F;
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
			return new SmokeParticle(level, d, e, f, g, h, i, 1.0F, this.sprites);
		}
	}
}

package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class FlameParticle extends TextureSheetParticle {
	private FlameParticle(Level level, double d, double e, double f, double g, double h, double i) {
		super(level, d, e, f, g, h, i);
		this.xd = this.xd * 0.01F + g;
		this.yd = this.yd * 0.01F + h;
		this.zd = this.zd * 0.01F + i;
		this.x = this.x + (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
		this.y = this.y + (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
		this.z = this.z + (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
		this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2)) + 4;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public void move(double d, double e, double f) {
		this.setBoundingBox(this.getBoundingBox().move(d, e, f));
		this.setLocationFromBoundingbox();
	}

	@Override
	public float getQuadSize(float f) {
		float g = ((float)this.age + f) / (float)this.lifetime;
		return this.quadSize * (1.0F - g * g * 0.5F);
	}

	@Override
	public int getLightColor(float f) {
		float g = ((float)this.age + f) / (float)this.lifetime;
		g = Mth.clamp(g, 0.0F, 1.0F);
		int i = super.getLightColor(f);
		int j = i & 0xFF;
		int k = i >> 16 & 0xFF;
		j += (int)(g * 15.0F * 16.0F);
		if (j > 240) {
			j = 240;
		}

		return j | k << 16;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			this.move(this.xd, this.yd, this.zd);
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
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
			FlameParticle flameParticle = new FlameParticle(level, d, e, f, g, h, i);
			flameParticle.pickSprite(this.sprite);
			return flameParticle;
		}
	}
}

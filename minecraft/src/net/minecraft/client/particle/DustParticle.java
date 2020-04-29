package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class DustParticle extends TextureSheetParticle {
	private final SpriteSet sprites;

	private DustParticle(
		ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, DustParticleOptions dustParticleOptions, SpriteSet spriteSet
	) {
		super(clientLevel, d, e, f, g, h, i);
		this.sprites = spriteSet;
		this.xd *= 0.1F;
		this.yd *= 0.1F;
		this.zd *= 0.1F;
		float j = (float)Math.random() * 0.4F + 0.6F;
		this.rCol = ((float)(Math.random() * 0.2F) + 0.8F) * dustParticleOptions.getR() * j;
		this.gCol = ((float)(Math.random() * 0.2F) + 0.8F) * dustParticleOptions.getG() * j;
		this.bCol = ((float)(Math.random() * 0.2F) + 0.8F) * dustParticleOptions.getB() * j;
		this.quadSize = this.quadSize * 0.75F * dustParticleOptions.getScale();
		int k = (int)(8.0 / (Math.random() * 0.8 + 0.2));
		this.lifetime = (int)Math.max((float)k * dustParticleOptions.getScale(), 1.0F);
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
	public static class Provider implements ParticleProvider<DustParticleOptions> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(DustParticleOptions dustParticleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new DustParticle(clientLevel, d, e, f, g, h, i, dustParticleOptions, this.sprites);
		}
	}
}

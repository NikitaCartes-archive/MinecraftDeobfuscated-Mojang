package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class LavaParticle extends TextureSheetParticle {
	private LavaParticle(ClientLevel clientLevel, double d, double e, double f) {
		super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
		this.xd *= 0.8F;
		this.yd *= 0.8F;
		this.zd *= 0.8F;
		this.yd = (double)(this.random.nextFloat() * 0.4F + 0.05F);
		this.quadSize = this.quadSize * (this.random.nextFloat() * 2.0F + 0.2F);
		this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public int getLightColor(float f) {
		int i = super.getLightColor(f);
		int j = 240;
		int k = i >> 16 & 0xFF;
		return 240 | k << 16;
	}

	@Override
	public float getQuadSize(float f) {
		float g = ((float)this.age + f) / (float)this.lifetime;
		return this.quadSize * (1.0F - g * g);
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		float f = (float)this.age / (float)this.lifetime;
		if (this.random.nextFloat() > f) {
			this.level.addParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, this.xd, this.yd, this.zd);
		}

		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			this.yd -= 0.03;
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.999F;
			this.yd *= 0.999F;
			this.zd *= 0.999F;
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

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			LavaParticle lavaParticle = new LavaParticle(clientLevel, d, e, f);
			lavaParticle.pickSprite(this.sprite);
			return lavaParticle;
		}
	}
}

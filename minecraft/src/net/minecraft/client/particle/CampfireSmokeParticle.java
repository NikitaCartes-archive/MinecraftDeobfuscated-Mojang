package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class CampfireSmokeParticle extends TextureSheetParticle {
	private CampfireSmokeParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, boolean bl) {
		super(clientLevel, d, e, f);
		this.scale(3.0F);
		this.setSize(0.25F, 0.25F);
		if (bl) {
			this.lifetime = this.random.nextInt(50) + 280;
		} else {
			this.lifetime = this.random.nextInt(50) + 80;
		}

		this.gravity = 3.0E-6F;
		this.xd = g;
		this.yd = h + (double)(this.random.nextFloat() / 500.0F);
		this.zd = i;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ < this.lifetime && !(this.alpha <= 0.0F)) {
			this.xd = this.xd + (double)(this.random.nextFloat() / 5000.0F * (float)(this.random.nextBoolean() ? 1 : -1));
			this.zd = this.zd + (double)(this.random.nextFloat() / 5000.0F * (float)(this.random.nextBoolean() ? 1 : -1));
			this.yd = this.yd - (double)this.gravity;
			this.move(this.xd, this.yd, this.zd);
			if (this.age >= this.lifetime - 60 && this.alpha > 0.01F) {
				this.alpha -= 0.015F;
			}
		} else {
			this.remove();
		}
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Environment(EnvType.CLIENT)
	public static class CosyProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public CosyProvider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			CampfireSmokeParticle campfireSmokeParticle = new CampfireSmokeParticle(clientLevel, d, e, f, g, h, i, false);
			campfireSmokeParticle.setAlpha(0.9F);
			campfireSmokeParticle.pickSprite(this.sprites);
			return campfireSmokeParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class SignalProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public SignalProvider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			CampfireSmokeParticle campfireSmokeParticle = new CampfireSmokeParticle(clientLevel, d, e, f, g, h, i, true);
			campfireSmokeParticle.setAlpha(0.95F);
			campfireSmokeParticle.pickSprite(this.sprites);
			return campfireSmokeParticle;
		}
	}
}

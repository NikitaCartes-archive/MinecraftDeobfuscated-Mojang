package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class HeartParticle extends TextureSheetParticle {
	private HeartParticle(ClientLevel clientLevel, double d, double e, double f) {
		super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
		this.speedUpWhenYMotionIsBlocked = true;
		this.friction = 0.86F;
		this.xd *= 0.01F;
		this.yd *= 0.01F;
		this.zd *= 0.01F;
		this.yd += 0.1;
		this.quadSize *= 1.5F;
		this.lifetime = 16;
		this.hasPhysics = false;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public float getQuadSize(float f) {
		return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
	}

	@Environment(EnvType.CLIENT)
	public static class AngryVillagerProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public AngryVillagerProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			HeartParticle heartParticle = new HeartParticle(clientLevel, d, e + 0.5, f);
			heartParticle.pickSprite(this.sprite);
			heartParticle.setColor(1.0F, 1.0F, 1.0F);
			return heartParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			HeartParticle heartParticle = new HeartParticle(clientLevel, d, e, f);
			heartParticle.pickSprite(this.sprite);
			return heartParticle;
		}
	}
}

package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class TrialSpawnerDetectionParticle extends TextureSheetParticle {
	private final SpriteSet sprites;
	private static final int BASE_LIFETIME = 8;

	protected TrialSpawnerDetectionParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, float j, SpriteSet spriteSet) {
		super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
		this.sprites = spriteSet;
		this.friction = 0.96F;
		this.gravity = -0.1F;
		this.speedUpWhenYMotionIsBlocked = true;
		this.xd *= 0.0;
		this.yd *= 0.9;
		this.zd *= 0.0;
		this.xd += g;
		this.yd += h;
		this.zd += i;
		this.quadSize *= 0.75F * j;
		this.lifetime = (int)(8.0F / Mth.randomBetween(this.random, 0.5F, 1.0F) * j);
		this.lifetime = Math.max(this.lifetime, 1);
		this.setSpriteFromAge(spriteSet);
		this.hasPhysics = true;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public int getLightColor(float f) {
		return 240;
	}

	@Override
	public SingleQuadParticle.FacingCameraMode getFacingCameraMode() {
		return SingleQuadParticle.FacingCameraMode.LOOKAT_Y;
	}

	@Override
	public void tick() {
		super.tick();
		this.setSpriteFromAge(this.sprites);
	}

	@Override
	public float getQuadSize(float f) {
		return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new TrialSpawnerDetectionParticle(clientLevel, d, e, f, g, h, i, 1.5F, this.sprites);
		}
	}
}

package net.minecraft.client.particle;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class GlowParticle extends TextureSheetParticle {
	static final Random RANDOM = new Random();
	private final SpriteSet sprites;

	GlowParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
		super(clientLevel, d, e, f, g, h, i);
		this.friction = 0.96F;
		this.speedUpWhenYMotionIsBlocked = true;
		this.sprites = spriteSet;
		this.quadSize *= 0.75F;
		this.hasPhysics = false;
		this.setSpriteFromAge(spriteSet);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
		super.tick();
		this.setSpriteFromAge(this.sprites);
	}

	@Environment(EnvType.CLIENT)
	public static class ElectricSparkProvider implements ParticleProvider<SimpleParticleType> {
		private final double SPEED_FACTOR = 0.25;
		private final SpriteSet sprite;

		public ElectricSparkProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			GlowParticle glowParticle = new GlowParticle(clientLevel, d, e, f, 0.0, 0.0, 0.0, this.sprite);
			glowParticle.setColor(1.0F, 0.9F, 1.0F);
			glowParticle.setParticleSpeed(g * 0.25, h * 0.25, i * 0.25);
			int j = 2;
			int k = 4;
			glowParticle.setLifetime(clientLevel.random.nextInt(2) + 2);
			return glowParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class GlowSquidProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public GlowSquidProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			GlowParticle glowParticle = new GlowParticle(
				clientLevel, d, e, f, 0.5 - GlowParticle.RANDOM.nextDouble(), h, 0.5 - GlowParticle.RANDOM.nextDouble(), this.sprite
			);
			if (clientLevel.random.nextBoolean()) {
				glowParticle.setColor(0.6F, 1.0F, 0.8F);
			} else {
				glowParticle.setColor(0.08F, 0.4F, 0.4F);
			}

			glowParticle.yd *= 0.2F;
			if (g == 0.0 && i == 0.0) {
				glowParticle.xd *= 0.1F;
				glowParticle.zd *= 0.1F;
			}

			glowParticle.setLifetime((int)(8.0 / (clientLevel.random.nextDouble() * 0.8 + 0.2)));
			return glowParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class ScrapeProvider implements ParticleProvider<SimpleParticleType> {
		private final double SPEED_FACTOR = 0.01;
		private final SpriteSet sprite;

		public ScrapeProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			GlowParticle glowParticle = new GlowParticle(clientLevel, d, e, f, 0.0, 0.0, 0.0, this.sprite);
			if (clientLevel.random.nextBoolean()) {
				glowParticle.setColor(0.29F, 0.58F, 0.51F);
			} else {
				glowParticle.setColor(0.43F, 0.77F, 0.62F);
			}

			glowParticle.setParticleSpeed(g * 0.01, h * 0.01, i * 0.01);
			int j = 10;
			int k = 40;
			glowParticle.setLifetime(clientLevel.random.nextInt(30) + 10);
			return glowParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class WaxOffProvider implements ParticleProvider<SimpleParticleType> {
		private final double SPEED_FACTOR = 0.01;
		private final SpriteSet sprite;

		public WaxOffProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			GlowParticle glowParticle = new GlowParticle(clientLevel, d, e, f, 0.0, 0.0, 0.0, this.sprite);
			glowParticle.setColor(1.0F, 0.9F, 1.0F);
			glowParticle.setParticleSpeed(g * 0.01 / 2.0, h * 0.01, i * 0.01 / 2.0);
			int j = 10;
			int k = 40;
			glowParticle.setLifetime(clientLevel.random.nextInt(30) + 10);
			return glowParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class WaxOnProvider implements ParticleProvider<SimpleParticleType> {
		private final double SPEED_FACTOR = 0.01;
		private final SpriteSet sprite;

		public WaxOnProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			GlowParticle glowParticle = new GlowParticle(clientLevel, d, e, f, 0.0, 0.0, 0.0, this.sprite);
			glowParticle.setColor(0.91F, 0.55F, 0.08F);
			glowParticle.setParticleSpeed(g * 0.01 / 2.0, h * 0.01, i * 0.01 / 2.0);
			int j = 10;
			int k = 40;
			glowParticle.setLifetime(clientLevel.random.nextInt(30) + 10);
			return glowParticle;
		}
	}
}

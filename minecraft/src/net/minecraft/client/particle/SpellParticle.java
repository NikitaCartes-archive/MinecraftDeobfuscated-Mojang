package net.minecraft.client.particle;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class SpellParticle extends TextureSheetParticle {
	private static final Random RANDOM = new Random();
	private final SpriteSet sprites;

	SpellParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
		super(clientLevel, d, e, f, 0.5 - RANDOM.nextDouble(), h, 0.5 - RANDOM.nextDouble());
		this.friction = 0.96F;
		this.gravity = -0.1F;
		this.speedUpWhenYMotionIsBlocked = true;
		this.sprites = spriteSet;
		this.yd *= 0.2F;
		if (g == 0.0 && i == 0.0) {
			this.xd *= 0.1F;
			this.zd *= 0.1F;
		}

		this.quadSize *= 0.75F;
		this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
		this.hasPhysics = false;
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
	public static class AmbientMobProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public AmbientMobProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			Particle particle = new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
			particle.setAlpha(0.15F);
			particle.setColor((float)g, (float)h, (float)i);
			return particle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class InstantProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public InstantProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class MobProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public MobProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			Particle particle = new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
			particle.setColor((float)g, (float)h, (float)i);
			return particle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class WitchProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public WitchProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			SpellParticle spellParticle = new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
			float j = clientLevel.random.nextFloat() * 0.5F + 0.35F;
			spellParticle.setColor(1.0F * j, 0.0F * j, 1.0F * j);
			return spellParticle;
		}
	}
}

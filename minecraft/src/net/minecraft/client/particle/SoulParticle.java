package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class SoulParticle extends RisingParticle {
	private final SpriteSet sprites;
	protected boolean isGlowing;

	SoulParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
		super(clientLevel, d, e, f, g, h, i);
		this.sprites = spriteSet;
		this.scale(1.5F);
		this.setSpriteFromAge(spriteSet);
	}

	@Override
	public int getLightColor(float f) {
		return this.isGlowing ? 240 : super.getLightColor(f);
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
	public static class EmissiveProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public EmissiveProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			SoulParticle soulParticle = new SoulParticle(clientLevel, d, e, f, g, h, i, this.sprite);
			soulParticle.setAlpha(1.0F);
			soulParticle.isGlowing = true;
			return soulParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			SoulParticle soulParticle = new SoulParticle(clientLevel, d, e, f, g, h, i, this.sprite);
			soulParticle.setAlpha(1.0F);
			return soulParticle;
		}
	}
}

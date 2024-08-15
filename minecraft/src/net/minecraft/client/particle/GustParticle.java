package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class GustParticle extends TextureSheetParticle {
	private final SpriteSet sprites;

	protected GustParticle(ClientLevel clientLevel, double d, double e, double f, SpriteSet spriteSet) {
		super(clientLevel, d, e, f);
		this.sprites = spriteSet;
		this.setSpriteFromAge(spriteSet);
		this.lifetime = 12 + this.random.nextInt(4);
		this.quadSize = 1.0F;
		this.setSize(1.0F, 1.0F);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public int getLightColor(float f) {
		return 15728880;
	}

	@Override
	public void tick() {
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			this.setSpriteFromAge(this.sprites);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new GustParticle(clientLevel, d, e, f, this.sprites);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class SmallProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public SmallProvider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			Particle particle = new GustParticle(clientLevel, d, e, f, this.sprites);
			particle.scale(0.15F);
			return particle;
		}
	}
}

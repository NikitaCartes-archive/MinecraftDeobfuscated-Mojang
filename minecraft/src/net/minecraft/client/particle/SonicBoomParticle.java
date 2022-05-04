package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class SonicBoomParticle extends HugeExplosionParticle {
	protected SonicBoomParticle(ClientLevel clientLevel, double d, double e, double f, double g, SpriteSet spriteSet) {
		super(clientLevel, d, e, f, g, spriteSet);
		this.lifetime = 16;
		this.quadSize = 1.5F;
		this.setSpriteFromAge(spriteSet);
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new SonicBoomParticle(clientLevel, d, e, f, g, this.sprites);
		}
	}
}

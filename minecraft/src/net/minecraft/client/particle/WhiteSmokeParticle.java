package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class WhiteSmokeParticle extends BaseAshSmokeParticle {
	private static final int COLOR_RGB24 = 12235202;

	protected WhiteSmokeParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, float j, SpriteSet spriteSet) {
		super(clientLevel, d, e, f, 0.1F, 0.1F, 0.1F, g, h, i, j, spriteSet, 0.3F, 8, -0.1F, true);
		this.rCol = 0.7294118F;
		this.gCol = 0.69411767F;
		this.bCol = 0.7607843F;
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new WhiteSmokeParticle(clientLevel, d, e, f, g, h, i, 1.0F, this.sprites);
		}
	}
}

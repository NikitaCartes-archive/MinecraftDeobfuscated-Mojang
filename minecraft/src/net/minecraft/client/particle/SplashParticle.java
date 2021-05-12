package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class SplashParticle extends WaterDropParticle {
	SplashParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
		super(clientLevel, d, e, f);
		this.gravity = 0.04F;
		if (h == 0.0 && (g != 0.0 || i != 0.0)) {
			this.xd = g;
			this.yd = 0.1;
			this.zd = i;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			SplashParticle splashParticle = new SplashParticle(clientLevel, d, e, f, g, h, i);
			splashParticle.pickSprite(this.sprite);
			return splashParticle;
		}
	}
}

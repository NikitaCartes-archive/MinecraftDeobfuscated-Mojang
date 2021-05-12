package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class EndRodParticle extends SimpleAnimatedParticle {
	EndRodParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
		super(clientLevel, d, e, f, spriteSet, 0.0125F);
		this.xd = g;
		this.yd = h;
		this.zd = i;
		this.quadSize *= 0.75F;
		this.lifetime = 60 + this.random.nextInt(12);
		this.setFadeColor(15916745);
		this.setSpriteFromAge(spriteSet);
	}

	@Override
	public void move(double d, double e, double f) {
		this.setBoundingBox(this.getBoundingBox().move(d, e, f));
		this.setLocationFromBoundingbox();
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new EndRodParticle(clientLevel, d, e, f, g, h, i, this.sprites);
		}
	}
}

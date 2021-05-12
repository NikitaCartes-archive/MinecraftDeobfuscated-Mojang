package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class WakeParticle extends TextureSheetParticle {
	private final SpriteSet sprites;

	WakeParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
		super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
		this.sprites = spriteSet;
		this.xd *= 0.3F;
		this.yd = Math.random() * 0.2F + 0.1F;
		this.zd *= 0.3F;
		this.setSize(0.01F, 0.01F);
		this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
		this.setSpriteFromAge(spriteSet);
		this.gravity = 0.0F;
		this.xd = g;
		this.yd = h;
		this.zd = i;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		int i = 60 - this.lifetime;
		if (this.lifetime-- <= 0) {
			this.remove();
		} else {
			this.yd = this.yd - (double)this.gravity;
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.98F;
			this.yd *= 0.98F;
			this.zd *= 0.98F;
			float f = (float)i * 0.001F;
			this.setSize(f, f);
			this.setSprite(this.sprites.get(i % 4, 4));
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new WakeParticle(clientLevel, d, e, f, g, h, i, this.sprites);
		}
	}
}

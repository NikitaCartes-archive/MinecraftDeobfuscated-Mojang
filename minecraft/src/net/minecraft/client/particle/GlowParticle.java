package net.minecraft.client.particle;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class GlowParticle extends TextureSheetParticle {
	private static final Random RANDOM = new Random();
	private final SpriteSet sprites;

	private GlowParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
		super(clientLevel, d, e, f, 0.5 - RANDOM.nextDouble(), h, 0.5 - RANDOM.nextDouble());
		this.friction = 0.96F;
		this.speedUpWhenYMotionIsBlocked = true;
		this.sprites = spriteSet;
		this.yd *= 0.2F;
		if (g == 0.0 && i == 0.0) {
			this.xd *= 0.1F;
			this.zd *= 0.1F;
		}

		this.quadSize *= 0.75F;
		this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
		if (this.random.nextBoolean()) {
			this.setColor(0.6F, 1.0F, 0.8F);
		} else {
			this.setColor(0.08F, 0.4F, 0.4F);
		}

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
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new GlowParticle(clientLevel, d, e, f, g, h, i, this.sprite);
		}
	}
}

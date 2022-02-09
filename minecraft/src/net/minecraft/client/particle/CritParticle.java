package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class CritParticle extends TextureSheetParticle {
	CritParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
		super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
		this.friction = 0.7F;
		this.gravity = 0.5F;
		this.xd *= 0.1F;
		this.yd *= 0.1F;
		this.zd *= 0.1F;
		this.xd += g * 0.4;
		this.yd += h * 0.4;
		this.zd += i * 0.4;
		float j = (float)(Math.random() * 0.3F + 0.6F);
		this.rCol = j;
		this.gCol = j;
		this.bCol = j;
		this.quadSize *= 0.75F;
		this.lifetime = Math.max((int)(6.0 / (Math.random() * 0.8 + 0.6)), 1);
		this.hasPhysics = false;
		this.tick();
	}

	@Override
	public float getQuadSize(float f) {
		return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
	}

	@Override
	public void tick() {
		super.tick();
		this.gCol *= 0.96F;
		this.bCol *= 0.9F;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Environment(EnvType.CLIENT)
	public static class DamageIndicatorProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public DamageIndicatorProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			CritParticle critParticle = new CritParticle(clientLevel, d, e, f, g, h + 1.0, i);
			critParticle.setLifetime(20);
			critParticle.pickSprite(this.sprite);
			return critParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class MagicProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public MagicProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			CritParticle critParticle = new CritParticle(clientLevel, d, e, f, g, h, i);
			critParticle.rCol *= 0.3F;
			critParticle.gCol *= 0.8F;
			critParticle.pickSprite(this.sprite);
			return critParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			CritParticle critParticle = new CritParticle(clientLevel, d, e, f, g, h, i);
			critParticle.pickSprite(this.sprite);
			return critParticle;
		}
	}
}

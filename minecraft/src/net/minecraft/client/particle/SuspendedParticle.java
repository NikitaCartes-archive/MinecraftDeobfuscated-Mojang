package net.minecraft.client.particle;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class SuspendedParticle extends TextureSheetParticle {
	private SuspendedParticle(ClientLevel clientLevel, double d, double e, double f) {
		super(clientLevel, d, e - 0.125, f);
		this.rCol = 0.4F;
		this.gCol = 0.4F;
		this.bCol = 0.7F;
		this.setSize(0.01F, 0.01F);
		this.quadSize = this.quadSize * (this.random.nextFloat() * 0.6F + 0.2F);
		this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
		this.hasPhysics = false;
	}

	private SuspendedParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
		super(clientLevel, d, e - 0.125, f, g, h, i);
		this.setSize(0.01F, 0.01F);
		this.quadSize = this.quadSize * (this.random.nextFloat() * 0.6F + 0.6F);
		this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
		this.hasPhysics = false;
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
		if (this.lifetime-- <= 0) {
			this.remove();
		} else {
			this.move(this.xd, this.yd, this.zd);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class CrimsonSporeProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public CrimsonSporeProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			Random random = clientLevel.random;
			double j = random.nextGaussian() * 1.0E-6F;
			double k = random.nextGaussian() * 1.0E-4F;
			double l = random.nextGaussian() * 1.0E-6F;
			SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, d, e, f, j, k, l);
			suspendedParticle.pickSprite(this.sprite);
			suspendedParticle.setColor(0.9F, 0.4F, 0.5F);
			return suspendedParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class UnderwaterProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public UnderwaterProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, d, e, f);
			suspendedParticle.pickSprite(this.sprite);
			return suspendedParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class WarpedSporeProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public WarpedSporeProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			double j = (double)clientLevel.random.nextFloat() * -1.9 * (double)clientLevel.random.nextFloat() * 0.1;
			SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, d, e, f, 0.0, j, 0.0);
			suspendedParticle.pickSprite(this.sprite);
			suspendedParticle.setColor(0.1F, 0.1F, 0.3F);
			suspendedParticle.setSize(0.001F, 0.001F);
			return suspendedParticle;
		}
	}
}

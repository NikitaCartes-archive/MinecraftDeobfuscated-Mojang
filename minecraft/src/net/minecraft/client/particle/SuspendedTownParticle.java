package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class SuspendedTownParticle extends TextureSheetParticle {
	SuspendedTownParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
		super(clientLevel, d, e, f, g, h, i);
		float j = this.random.nextFloat() * 0.1F + 0.2F;
		this.rCol = j;
		this.gCol = j;
		this.bCol = j;
		this.setSize(0.02F, 0.02F);
		this.quadSize = this.quadSize * (this.random.nextFloat() * 0.6F + 0.5F);
		this.xd *= 0.02F;
		this.yd *= 0.02F;
		this.zd *= 0.02F;
		this.lifetime = (int)(20.0 / (Math.random() * 0.8 + 0.2));
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public void move(double d, double e, double f) {
		this.setBoundingBox(this.getBoundingBox().move(d, e, f));
		this.setLocationFromBoundingbox();
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
			this.xd *= 0.99;
			this.yd *= 0.99;
			this.zd *= 0.99;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class ComposterFillProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public ComposterFillProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			SuspendedTownParticle suspendedTownParticle = new SuspendedTownParticle(clientLevel, d, e, f, g, h, i);
			suspendedTownParticle.pickSprite(this.sprite);
			suspendedTownParticle.setColor(1.0F, 1.0F, 1.0F);
			suspendedTownParticle.setLifetime(3 + clientLevel.getRandom().nextInt(5));
			return suspendedTownParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class DolphinSpeedProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public DolphinSpeedProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			SuspendedTownParticle suspendedTownParticle = new SuspendedTownParticle(clientLevel, d, e, f, g, h, i);
			suspendedTownParticle.setColor(0.3F, 0.5F, 1.0F);
			suspendedTownParticle.pickSprite(this.sprite);
			suspendedTownParticle.setAlpha(1.0F - clientLevel.random.nextFloat() * 0.7F);
			suspendedTownParticle.setLifetime(suspendedTownParticle.getLifetime() / 2);
			return suspendedTownParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class EggCrackProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public EggCrackProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			SuspendedTownParticle suspendedTownParticle = new SuspendedTownParticle(clientLevel, d, e, f, g, h, i);
			suspendedTownParticle.pickSprite(this.sprite);
			suspendedTownParticle.setColor(1.0F, 1.0F, 1.0F);
			return suspendedTownParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class HappyVillagerProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public HappyVillagerProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			SuspendedTownParticle suspendedTownParticle = new SuspendedTownParticle(clientLevel, d, e, f, g, h, i);
			suspendedTownParticle.pickSprite(this.sprite);
			suspendedTownParticle.setColor(1.0F, 1.0F, 1.0F);
			return suspendedTownParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			SuspendedTownParticle suspendedTownParticle = new SuspendedTownParticle(clientLevel, d, e, f, g, h, i);
			suspendedTownParticle.pickSprite(this.sprite);
			return suspendedTownParticle;
		}
	}
}

package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class SpellParticle extends TextureSheetParticle {
	private static final RandomSource RANDOM = RandomSource.create();
	private final SpriteSet sprites;
	private float originalAlpha = 1.0F;

	SpellParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
		super(clientLevel, d, e, f, 0.5 - RANDOM.nextDouble(), h, 0.5 - RANDOM.nextDouble());
		this.friction = 0.96F;
		this.gravity = -0.1F;
		this.speedUpWhenYMotionIsBlocked = true;
		this.sprites = spriteSet;
		this.yd *= 0.2F;
		if (g == 0.0 && i == 0.0) {
			this.xd *= 0.1F;
			this.zd *= 0.1F;
		}

		this.quadSize *= 0.75F;
		this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
		this.hasPhysics = false;
		this.setSpriteFromAge(spriteSet);
		if (this.isCloseToScopingPlayer()) {
			this.setAlpha(0.0F);
		}
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick() {
		super.tick();
		this.setSpriteFromAge(this.sprites);
		if (this.isCloseToScopingPlayer()) {
			this.alpha = 0.0F;
		} else {
			this.alpha = Mth.lerp(0.05F, this.alpha, this.originalAlpha);
		}
	}

	@Override
	protected void setAlpha(float f) {
		super.setAlpha(f);
		this.originalAlpha = f;
	}

	private boolean isCloseToScopingPlayer() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer localPlayer = minecraft.player;
		return localPlayer != null
			&& localPlayer.getEyePosition().distanceToSqr(this.x, this.y, this.z) <= 9.0
			&& minecraft.options.getCameraType().isFirstPerson()
			&& localPlayer.isScoping();
	}

	@Environment(EnvType.CLIENT)
	public static class InstantProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public InstantProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class MobEffectProvider implements ParticleProvider<ColorParticleOption> {
		private final SpriteSet sprite;

		public MobEffectProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(ColorParticleOption colorParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			Particle particle = new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
			particle.setColor(colorParticleOption.getRed(), colorParticleOption.getGreen(), colorParticleOption.getBlue());
			particle.setAlpha(colorParticleOption.getAlpha());
			return particle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class WitchProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public WitchProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			SpellParticle spellParticle = new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
			float j = clientLevel.random.nextFloat() * 0.5F + 0.35F;
			spellParticle.setColor(1.0F * j, 0.0F * j, 1.0F * j);
			return spellParticle;
		}
	}
}

package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FireworkRocketItem;

@Environment(EnvType.CLIENT)
public class FireworkParticles {
	@Environment(EnvType.CLIENT)
	public static class FlashProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public FlashProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			FireworkParticles.OverlayParticle overlayParticle = new FireworkParticles.OverlayParticle(clientLevel, d, e, f);
			overlayParticle.pickSprite(this.sprite);
			return overlayParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class OverlayParticle extends TextureSheetParticle {
		OverlayParticle(ClientLevel clientLevel, double d, double e, double f) {
			super(clientLevel, d, e, f);
			this.lifetime = 4;
		}

		@Override
		public ParticleRenderType getRenderType() {
			return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
		}

		@Override
		public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
			this.setAlpha(0.6F - ((float)this.age + f - 1.0F) * 0.25F * 0.5F);
			super.render(vertexConsumer, camera, f);
		}

		@Override
		public float getQuadSize(float f) {
			return 7.1F * Mth.sin(((float)this.age + f - 1.0F) * 0.25F * (float) Math.PI);
		}
	}

	@Environment(EnvType.CLIENT)
	static class SparkParticle extends SimpleAnimatedParticle {
		private boolean trail;
		private boolean flicker;
		private final ParticleEngine engine;
		private float fadeR;
		private float fadeG;
		private float fadeB;
		private boolean hasFade;

		SparkParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, ParticleEngine particleEngine, SpriteSet spriteSet) {
			super(clientLevel, d, e, f, spriteSet, 0.1F);
			this.xd = g;
			this.yd = h;
			this.zd = i;
			this.engine = particleEngine;
			this.quadSize *= 0.75F;
			this.lifetime = 48 + this.random.nextInt(12);
			this.setSpriteFromAge(spriteSet);
		}

		public void setTrail(boolean bl) {
			this.trail = bl;
		}

		public void setFlicker(boolean bl) {
			this.flicker = bl;
		}

		@Override
		public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
			if (!this.flicker || this.age < this.lifetime / 3 || (this.age + this.lifetime) / 3 % 2 == 0) {
				super.render(vertexConsumer, camera, f);
			}
		}

		@Override
		public void tick() {
			super.tick();
			if (this.trail && this.age < this.lifetime / 2 && (this.age + this.lifetime) % 2 == 0) {
				FireworkParticles.SparkParticle sparkParticle = new FireworkParticles.SparkParticle(
					this.level, this.x, this.y, this.z, 0.0, 0.0, 0.0, this.engine, this.sprites
				);
				sparkParticle.setAlpha(0.99F);
				sparkParticle.setColor(this.rCol, this.gCol, this.bCol);
				sparkParticle.age = sparkParticle.lifetime / 2;
				if (this.hasFade) {
					sparkParticle.hasFade = true;
					sparkParticle.fadeR = this.fadeR;
					sparkParticle.fadeG = this.fadeG;
					sparkParticle.fadeB = this.fadeB;
				}

				sparkParticle.flicker = this.flicker;
				this.engine.add(sparkParticle);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static class SparkProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public SparkProvider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			FireworkParticles.SparkParticle sparkParticle = new FireworkParticles.SparkParticle(
				clientLevel, d, e, f, g, h, i, Minecraft.getInstance().particleEngine, this.sprites
			);
			sparkParticle.setAlpha(0.99F);
			return sparkParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Starter extends NoRenderParticle {
		private int life;
		private final ParticleEngine engine;
		private ListTag explosions;
		private boolean twinkleDelay;

		public Starter(
			ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, ParticleEngine particleEngine, @Nullable CompoundTag compoundTag
		) {
			super(clientLevel, d, e, f);
			this.xd = g;
			this.yd = h;
			this.zd = i;
			this.engine = particleEngine;
			this.lifetime = 8;
			if (compoundTag != null) {
				this.explosions = compoundTag.getList("Explosions", 10);
				if (this.explosions.isEmpty()) {
					this.explosions = null;
				} else {
					this.lifetime = this.explosions.size() * 2 - 1;

					for (int j = 0; j < this.explosions.size(); j++) {
						CompoundTag compoundTag2 = this.explosions.getCompound(j);
						if (compoundTag2.getBoolean("Flicker")) {
							this.twinkleDelay = true;
							this.lifetime += 15;
							break;
						}
					}
				}
			}
		}

		@Override
		public void tick() {
			if (this.life == 0 && this.explosions != null) {
				boolean bl = this.isFarAwayFromCamera();
				boolean bl2 = false;
				if (this.explosions.size() >= 3) {
					bl2 = true;
				} else {
					for (int i = 0; i < this.explosions.size(); i++) {
						CompoundTag compoundTag = this.explosions.getCompound(i);
						if (FireworkRocketItem.Shape.byId(compoundTag.getByte("Type")) == FireworkRocketItem.Shape.LARGE_BALL) {
							bl2 = true;
							break;
						}
					}
				}

				SoundEvent soundEvent;
				if (bl2) {
					soundEvent = bl ? SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR : SoundEvents.FIREWORK_ROCKET_LARGE_BLAST;
				} else {
					soundEvent = bl ? SoundEvents.FIREWORK_ROCKET_BLAST_FAR : SoundEvents.FIREWORK_ROCKET_BLAST;
				}

				this.level.playLocalSound(this.x, this.y, this.z, soundEvent, SoundSource.AMBIENT, 20.0F, 0.95F + this.random.nextFloat() * 0.1F, true);
			}

			if (this.life % 2 == 0 && this.explosions != null && this.life / 2 < this.explosions.size()) {
				int j = this.life / 2;
				CompoundTag compoundTag2 = this.explosions.getCompound(j);
				FireworkRocketItem.Shape shape = FireworkRocketItem.Shape.byId(compoundTag2.getByte("Type"));
				boolean bl3 = compoundTag2.getBoolean("Trail");
				boolean bl4 = compoundTag2.getBoolean("Flicker");
				int[] is = compoundTag2.getIntArray("Colors");
				int[] js = compoundTag2.getIntArray("FadeColors");
				if (is.length == 0) {
					is = new int[]{DyeColor.BLACK.getFireworkColor()};
				}

				switch (shape) {
					case SMALL_BALL:
					default:
						this.createParticleBall(0.25, 2, is, js, bl3, bl4);
						break;
					case LARGE_BALL:
						this.createParticleBall(0.5, 4, is, js, bl3, bl4);
						break;
					case STAR:
						this.createParticleShape(
							0.5,
							new double[][]{
								{0.0, 1.0},
								{0.3455, 0.309},
								{0.9511, 0.309},
								{0.3795918367346939, -0.12653061224489795},
								{0.6122448979591837, -0.8040816326530612},
								{0.0, -0.35918367346938773}
							},
							is,
							js,
							bl3,
							bl4,
							false
						);
						break;
					case CREEPER:
						this.createParticleShape(
							0.5,
							new double[][]{
								{0.0, 0.2}, {0.2, 0.2}, {0.2, 0.6}, {0.6, 0.6}, {0.6, 0.2}, {0.2, 0.2}, {0.2, 0.0}, {0.4, 0.0}, {0.4, -0.6}, {0.2, -0.6}, {0.2, -0.4}, {0.0, -0.4}
							},
							is,
							js,
							bl3,
							bl4,
							true
						);
						break;
					case BURST:
						this.createParticleBurst(is, js, bl3, bl4);
				}

				int k = is[0];
				float f = (float)((k & 0xFF0000) >> 16) / 255.0F;
				float g = (float)((k & 0xFF00) >> 8) / 255.0F;
				float h = (float)((k & 0xFF) >> 0) / 255.0F;
				Particle particle = this.engine.createParticle(ParticleTypes.FLASH, this.x, this.y, this.z, 0.0, 0.0, 0.0);
				particle.setColor(f, g, h);
			}

			this.life++;
			if (this.life > this.lifetime) {
				if (this.twinkleDelay) {
					boolean blx = this.isFarAwayFromCamera();
					SoundEvent soundEvent2 = blx ? SoundEvents.FIREWORK_ROCKET_TWINKLE_FAR : SoundEvents.FIREWORK_ROCKET_TWINKLE;
					this.level.playLocalSound(this.x, this.y, this.z, soundEvent2, SoundSource.AMBIENT, 20.0F, 0.9F + this.random.nextFloat() * 0.15F, true);
				}

				this.remove();
			}
		}

		private boolean isFarAwayFromCamera() {
			Minecraft minecraft = Minecraft.getInstance();
			return minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(this.x, this.y, this.z) >= 256.0;
		}

		private void createParticle(double d, double e, double f, double g, double h, double i, int[] is, int[] js, boolean bl, boolean bl2) {
			FireworkParticles.SparkParticle sparkParticle = (FireworkParticles.SparkParticle)this.engine.createParticle(ParticleTypes.FIREWORK, d, e, f, g, h, i);
			sparkParticle.setTrail(bl);
			sparkParticle.setFlicker(bl2);
			sparkParticle.setAlpha(0.99F);
			int j = this.random.nextInt(is.length);
			sparkParticle.setColor(is[j]);
			if (js.length > 0) {
				sparkParticle.setFadeColor(Util.getRandom(js, this.random));
			}
		}

		private void createParticleBall(double d, int i, int[] is, int[] js, boolean bl, boolean bl2) {
			double e = this.x;
			double f = this.y;
			double g = this.z;

			for (int j = -i; j <= i; j++) {
				for (int k = -i; k <= i; k++) {
					for (int l = -i; l <= i; l++) {
						double h = (double)k + (this.random.nextDouble() - this.random.nextDouble()) * 0.5;
						double m = (double)j + (this.random.nextDouble() - this.random.nextDouble()) * 0.5;
						double n = (double)l + (this.random.nextDouble() - this.random.nextDouble()) * 0.5;
						double o = (double)Mth.sqrt(h * h + m * m + n * n) / d + this.random.nextGaussian() * 0.05;
						this.createParticle(e, f, g, h / o, m / o, n / o, is, js, bl, bl2);
						if (j != -i && j != i && k != -i && k != i) {
							l += i * 2 - 1;
						}
					}
				}
			}
		}

		private void createParticleShape(double d, double[][] ds, int[] is, int[] js, boolean bl, boolean bl2, boolean bl3) {
			double e = ds[0][0];
			double f = ds[0][1];
			this.createParticle(this.x, this.y, this.z, e * d, f * d, 0.0, is, js, bl, bl2);
			float g = this.random.nextFloat() * (float) Math.PI;
			double h = bl3 ? 0.034 : 0.34;

			for (int i = 0; i < 3; i++) {
				double j = (double)g + (double)((float)i * (float) Math.PI) * h;
				double k = e;
				double l = f;

				for (int m = 1; m < ds.length; m++) {
					double n = ds[m][0];
					double o = ds[m][1];

					for (double p = 0.25; p <= 1.0; p += 0.25) {
						double q = Mth.lerp(p, k, n) * d;
						double r = Mth.lerp(p, l, o) * d;
						double s = q * Math.sin(j);
						q *= Math.cos(j);

						for (double t = -1.0; t <= 1.0; t += 2.0) {
							this.createParticle(this.x, this.y, this.z, q * t, r, s * t, is, js, bl, bl2);
						}
					}

					k = n;
					l = o;
				}
			}
		}

		private void createParticleBurst(int[] is, int[] js, boolean bl, boolean bl2) {
			double d = this.random.nextGaussian() * 0.05;
			double e = this.random.nextGaussian() * 0.05;

			for (int i = 0; i < 70; i++) {
				double f = this.xd * 0.5 + this.random.nextGaussian() * 0.15 + d;
				double g = this.zd * 0.5 + this.random.nextGaussian() * 0.15 + e;
				double h = this.yd * 0.5 + this.random.nextDouble() * 0.5;
				this.createParticle(this.x, this.y, this.z, f, h, g, is, js, bl, bl2);
			}
		}
	}
}

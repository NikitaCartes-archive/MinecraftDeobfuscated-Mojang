package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class FogRenderer {
	private static final int WATER_FOG_DISTANCE = 96;
	private static final List<FogRenderer.MobEffectFogFunction> MOB_EFFECT_FOG = Lists.<FogRenderer.MobEffectFogFunction>newArrayList(
		new FogRenderer.BlindnessFogFunction(), new FogRenderer.DarknessFogFunction()
	);
	public static final float BIOME_FOG_TRANSITION_TIME = 5000.0F;
	private static float fogRed;
	private static float fogGreen;
	private static float fogBlue;
	private static int targetBiomeFog = -1;
	private static int previousBiomeFog = -1;
	private static long biomeChangedTime = -1L;

	public static void setupColor(Camera camera, float f, ClientLevel clientLevel, int i, float g) {
		FogType fogType = camera.getFluidInCamera();
		Entity entity = camera.getEntity();
		if (fogType == FogType.WATER) {
			long l = Util.getMillis();
			int j = clientLevel.getBiome(BlockPos.containing(camera.getPosition())).value().getWaterFogColor();
			if (biomeChangedTime < 0L) {
				targetBiomeFog = j;
				previousBiomeFog = j;
				biomeChangedTime = l;
			}

			int k = targetBiomeFog >> 16 & 0xFF;
			int m = targetBiomeFog >> 8 & 0xFF;
			int n = targetBiomeFog & 0xFF;
			int o = previousBiomeFog >> 16 & 0xFF;
			int p = previousBiomeFog >> 8 & 0xFF;
			int q = previousBiomeFog & 0xFF;
			float h = Mth.clamp((float)(l - biomeChangedTime) / 5000.0F, 0.0F, 1.0F);
			float r = Mth.lerp(h, (float)o, (float)k);
			float s = Mth.lerp(h, (float)p, (float)m);
			float t = Mth.lerp(h, (float)q, (float)n);
			fogRed = r / 255.0F;
			fogGreen = s / 255.0F;
			fogBlue = t / 255.0F;
			if (targetBiomeFog != j) {
				targetBiomeFog = j;
				previousBiomeFog = Mth.floor(r) << 16 | Mth.floor(s) << 8 | Mth.floor(t);
				biomeChangedTime = l;
			}
		} else if (fogType == FogType.LAVA) {
			fogRed = 0.6F;
			fogGreen = 0.1F;
			fogBlue = 0.0F;
			biomeChangedTime = -1L;
		} else if (fogType == FogType.POWDER_SNOW) {
			fogRed = 0.623F;
			fogGreen = 0.734F;
			fogBlue = 0.785F;
			biomeChangedTime = -1L;
			RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
		} else {
			float u = 0.25F + 0.75F * (float)i / 32.0F;
			u = 1.0F - (float)Math.pow((double)u, 0.25);
			Vec3 vec3 = clientLevel.getSkyColor(camera.getPosition(), f);
			float v = (float)vec3.x;
			float w = (float)vec3.y;
			float x = (float)vec3.z;
			float y = Mth.clamp(Mth.cos(clientLevel.getTimeOfDay(f) * (float) (Math.PI * 2)) * 2.0F + 0.5F, 0.0F, 1.0F);
			BiomeManager biomeManager = clientLevel.getBiomeManager();
			Vec3 vec32 = camera.getPosition().subtract(2.0, 2.0, 2.0).scale(0.25);
			Vec3 vec33 = CubicSampler.gaussianSampleVec3(
				vec32,
				(ix, jx, k) -> clientLevel.effects().getBrightnessDependentFogColor(Vec3.fromRGB24(biomeManager.getNoiseBiomeAtQuart(ix, jx, k).value().getFogColor()), y)
			);
			fogRed = (float)vec33.x();
			fogGreen = (float)vec33.y();
			fogBlue = (float)vec33.z();
			if (i >= 4) {
				float h = Mth.sin(clientLevel.getSunAngle(f)) > 0.0F ? -1.0F : 1.0F;
				Vector3f vector3f = new Vector3f(h, 0.0F, 0.0F);
				float s = camera.getLookVector().dot(vector3f);
				if (s < 0.0F) {
					s = 0.0F;
				}

				if (s > 0.0F) {
					float[] fs = clientLevel.effects().getSunriseColor(clientLevel.getTimeOfDay(f), f);
					if (fs != null) {
						s *= fs[3];
						fogRed = fogRed * (1.0F - s) + fs[0] * s;
						fogGreen = fogGreen * (1.0F - s) + fs[1] * s;
						fogBlue = fogBlue * (1.0F - s) + fs[2] * s;
					}
				}
			}

			fogRed = fogRed + (v - fogRed) * u;
			fogGreen = fogGreen + (w - fogGreen) * u;
			fogBlue = fogBlue + (x - fogBlue) * u;
			float hx = clientLevel.getRainLevel(f);
			if (hx > 0.0F) {
				float r = 1.0F - hx * 0.5F;
				float sx = 1.0F - hx * 0.4F;
				fogRed *= r;
				fogGreen *= r;
				fogBlue *= sx;
			}

			float r = clientLevel.getThunderLevel(f);
			if (r > 0.0F) {
				float sx = 1.0F - r * 0.5F;
				fogRed *= sx;
				fogGreen *= sx;
				fogBlue *= sx;
			}

			biomeChangedTime = -1L;
		}

		float ux = ((float)camera.getPosition().y - (float)clientLevel.getMinBuildHeight()) * clientLevel.getLevelData().getClearColorScale();
		FogRenderer.MobEffectFogFunction mobEffectFogFunction = getPriorityFogFunction(entity, f);
		if (mobEffectFogFunction != null) {
			LivingEntity livingEntity = (LivingEntity)entity;
			ux = mobEffectFogFunction.getModifiedVoidDarkness(livingEntity, livingEntity.getEffect(mobEffectFogFunction.getMobEffect()), ux, f);
		}

		if (ux < 1.0F && fogType != FogType.LAVA && fogType != FogType.POWDER_SNOW) {
			if (ux < 0.0F) {
				ux = 0.0F;
			}

			ux *= ux;
			fogRed *= ux;
			fogGreen *= ux;
			fogBlue *= ux;
		}

		if (g > 0.0F) {
			fogRed = fogRed * (1.0F - g) + fogRed * 0.7F * g;
			fogGreen = fogGreen * (1.0F - g) + fogGreen * 0.6F * g;
			fogBlue = fogBlue * (1.0F - g) + fogBlue * 0.6F * g;
		}

		float vx;
		if (fogType == FogType.WATER) {
			if (entity instanceof LocalPlayer) {
				vx = ((LocalPlayer)entity).getWaterVision();
			} else {
				vx = 1.0F;
			}
		} else {
			label86: {
				if (entity instanceof LivingEntity livingEntity2 && livingEntity2.hasEffect(MobEffects.NIGHT_VISION) && !livingEntity2.hasEffect(MobEffects.DARKNESS)) {
					vx = GameRenderer.getNightVisionScale(livingEntity2, f);
					break label86;
				}

				vx = 0.0F;
			}
		}

		if (fogRed != 0.0F && fogGreen != 0.0F && fogBlue != 0.0F) {
			float wx = Math.min(1.0F / fogRed, Math.min(1.0F / fogGreen, 1.0F / fogBlue));
			fogRed = fogRed * (1.0F - vx) + fogRed * wx * vx;
			fogGreen = fogGreen * (1.0F - vx) + fogGreen * wx * vx;
			fogBlue = fogBlue * (1.0F - vx) + fogBlue * wx * vx;
		}

		RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
	}

	public static void setupNoFog() {
		RenderSystem.setShaderFogStart(Float.MAX_VALUE);
	}

	@Nullable
	private static FogRenderer.MobEffectFogFunction getPriorityFogFunction(Entity entity, float f) {
		return entity instanceof LivingEntity livingEntity
			? (FogRenderer.MobEffectFogFunction)MOB_EFFECT_FOG.stream()
				.filter(mobEffectFogFunction -> mobEffectFogFunction.isEnabled(livingEntity, f))
				.findFirst()
				.orElse(null)
			: null;
	}

	public static void setupFog(Camera camera, FogRenderer.FogMode fogMode, float f, boolean bl, float g) {
		FogType fogType = camera.getFluidInCamera();
		Entity entity = camera.getEntity();
		FogRenderer.FogData fogData = new FogRenderer.FogData(fogMode);
		FogRenderer.MobEffectFogFunction mobEffectFogFunction = getPriorityFogFunction(entity, g);
		if (fogType == FogType.LAVA) {
			if (entity.isSpectator()) {
				fogData.start = -8.0F;
				fogData.end = f * 0.5F;
			} else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.FIRE_RESISTANCE)) {
				fogData.start = 0.0F;
				fogData.end = 3.0F;
			} else {
				fogData.start = 0.25F;
				fogData.end = 1.0F;
			}
		} else if (fogType == FogType.POWDER_SNOW) {
			if (entity.isSpectator()) {
				fogData.start = -8.0F;
				fogData.end = f * 0.5F;
			} else {
				fogData.start = 0.0F;
				fogData.end = 2.0F;
			}
		} else if (mobEffectFogFunction != null) {
			LivingEntity livingEntity = (LivingEntity)entity;
			MobEffectInstance mobEffectInstance = livingEntity.getEffect(mobEffectFogFunction.getMobEffect());
			if (mobEffectInstance != null) {
				mobEffectFogFunction.setupFog(fogData, livingEntity, mobEffectInstance, f, g);
			}
		} else if (fogType == FogType.WATER) {
			fogData.start = -8.0F;
			fogData.end = 96.0F;
			if (entity instanceof LocalPlayer localPlayer) {
				fogData.end = fogData.end * Math.max(0.25F, localPlayer.getWaterVision());
				Holder<Biome> holder = localPlayer.level().getBiome(localPlayer.blockPosition());
				if (holder.is(BiomeTags.HAS_CLOSER_WATER_FOG)) {
					fogData.end *= 0.85F;
				}
			}

			if (fogData.end > f) {
				fogData.end = f;
				fogData.shape = FogShape.CYLINDER;
			}
		} else if (bl) {
			fogData.start = f * 0.05F;
			fogData.end = Math.min(f, 192.0F) * 0.5F;
		} else if (fogMode == FogRenderer.FogMode.FOG_SKY) {
			fogData.start = 0.0F;
			fogData.end = f;
			fogData.shape = FogShape.CYLINDER;
		} else {
			float h = Mth.clamp(f / 10.0F, 4.0F, 64.0F);
			fogData.start = f - h;
			fogData.end = f;
			fogData.shape = FogShape.CYLINDER;
		}

		RenderSystem.setShaderFogStart(fogData.start);
		RenderSystem.setShaderFogEnd(fogData.end);
		RenderSystem.setShaderFogShape(fogData.shape);
	}

	public static void levelFogColor() {
		RenderSystem.setShaderFogColor(fogRed, fogGreen, fogBlue);
	}

	@Environment(EnvType.CLIENT)
	static class BlindnessFogFunction implements FogRenderer.MobEffectFogFunction {
		@Override
		public MobEffect getMobEffect() {
			return MobEffects.BLINDNESS;
		}

		@Override
		public void setupFog(FogRenderer.FogData fogData, LivingEntity livingEntity, MobEffectInstance mobEffectInstance, float f, float g) {
			float h = mobEffectInstance.isInfiniteDuration() ? 5.0F : Mth.lerp(Math.min(1.0F, (float)mobEffectInstance.getDuration() / 20.0F), f, 5.0F);
			if (fogData.mode == FogRenderer.FogMode.FOG_SKY) {
				fogData.start = 0.0F;
				fogData.end = h * 0.8F;
			} else {
				fogData.start = h * 0.25F;
				fogData.end = h;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static class DarknessFogFunction implements FogRenderer.MobEffectFogFunction {
		@Override
		public MobEffect getMobEffect() {
			return MobEffects.DARKNESS;
		}

		@Override
		public void setupFog(FogRenderer.FogData fogData, LivingEntity livingEntity, MobEffectInstance mobEffectInstance, float f, float g) {
			if (!mobEffectInstance.getFactorData().isEmpty()) {
				float h = Mth.lerp(((MobEffectInstance.FactorData)mobEffectInstance.getFactorData().get()).getFactor(livingEntity, g), f, 15.0F);
				fogData.start = fogData.mode == FogRenderer.FogMode.FOG_SKY ? 0.0F : h * 0.75F;
				fogData.end = h;
			}
		}

		@Override
		public float getModifiedVoidDarkness(LivingEntity livingEntity, MobEffectInstance mobEffectInstance, float f, float g) {
			return mobEffectInstance.getFactorData().isEmpty()
				? 0.0F
				: 1.0F - ((MobEffectInstance.FactorData)mobEffectInstance.getFactorData().get()).getFactor(livingEntity, g);
		}
	}

	@Environment(EnvType.CLIENT)
	static class FogData {
		public final FogRenderer.FogMode mode;
		public float start;
		public float end;
		public FogShape shape = FogShape.SPHERE;

		public FogData(FogRenderer.FogMode fogMode) {
			this.mode = fogMode;
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum FogMode {
		FOG_SKY,
		FOG_TERRAIN;
	}

	@Environment(EnvType.CLIENT)
	interface MobEffectFogFunction {
		MobEffect getMobEffect();

		void setupFog(FogRenderer.FogData fogData, LivingEntity livingEntity, MobEffectInstance mobEffectInstance, float f, float g);

		default boolean isEnabled(LivingEntity livingEntity, float f) {
			return livingEntity.hasEffect(this.getMobEffect());
		}

		default float getModifiedVoidDarkness(LivingEntity livingEntity, MobEffectInstance mobEffectInstance, float f, float g) {
			MobEffectInstance mobEffectInstance2 = livingEntity.getEffect(this.getMobEffect());
			if (mobEffectInstance2 != null) {
				if (mobEffectInstance2.endsWithin(19)) {
					f = 1.0F - (float)mobEffectInstance2.getDuration() / 20.0F;
				} else {
					f = 0.0F;
				}
			}

			return f;
		}
	}
}

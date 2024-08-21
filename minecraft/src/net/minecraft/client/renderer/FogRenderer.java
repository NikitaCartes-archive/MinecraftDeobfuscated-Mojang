package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.shaders.FogShape;
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
import net.minecraft.util.ARGB;
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
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public class FogRenderer {
	private static final int WATER_FOG_DISTANCE = 96;
	private static final List<FogRenderer.MobEffectFogFunction> MOB_EFFECT_FOG = Lists.<FogRenderer.MobEffectFogFunction>newArrayList(
		new FogRenderer.BlindnessFogFunction(), new FogRenderer.DarknessFogFunction()
	);
	public static final float BIOME_FOG_TRANSITION_TIME = 5000.0F;
	private static int targetBiomeFog = -1;
	private static int previousBiomeFog = -1;
	private static long biomeChangedTime = -1L;

	public static Vector4f computeFogColor(Camera camera, float f, ClientLevel clientLevel, int i, float g) {
		FogType fogType = camera.getFluidInCamera();
		Entity entity = camera.getEntity();
		float u;
		float v;
		float w;
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
			u = r / 255.0F;
			v = s / 255.0F;
			w = t / 255.0F;
			if (targetBiomeFog != j) {
				targetBiomeFog = j;
				previousBiomeFog = Mth.floor(r) << 16 | Mth.floor(s) << 8 | Mth.floor(t);
				biomeChangedTime = l;
			}
		} else if (fogType == FogType.LAVA) {
			u = 0.6F;
			v = 0.1F;
			w = 0.0F;
			biomeChangedTime = -1L;
		} else if (fogType == FogType.POWDER_SNOW) {
			u = 0.623F;
			v = 0.734F;
			w = 0.785F;
			biomeChangedTime = -1L;
		} else {
			float x = 0.25F + 0.75F * (float)i / 32.0F;
			x = 1.0F - (float)Math.pow((double)x, 0.25);
			int y = clientLevel.getSkyColor(camera.getPosition(), f);
			float z = ARGB.from8BitChannel(ARGB.red(y));
			float aa = ARGB.from8BitChannel(ARGB.green(y));
			float ab = ARGB.from8BitChannel(ARGB.blue(y));
			float ac = Mth.clamp(Mth.cos(clientLevel.getTimeOfDay(f) * (float) (Math.PI * 2)) * 2.0F + 0.5F, 0.0F, 1.0F);
			BiomeManager biomeManager = clientLevel.getBiomeManager();
			Vec3 vec3 = camera.getPosition().subtract(2.0, 2.0, 2.0).scale(0.25);
			Vec3 vec32 = CubicSampler.gaussianSampleVec3(
				vec3,
				(ix, jx, k) -> clientLevel.effects().getBrightnessDependentFogColor(Vec3.fromRGB24(biomeManager.getNoiseBiomeAtQuart(ix, jx, k).value().getFogColor()), ac)
			);
			u = (float)vec32.x();
			v = (float)vec32.y();
			w = (float)vec32.z();
			if (i >= 4) {
				float h = Mth.sin(clientLevel.getSunAngle(f)) > 0.0F ? -1.0F : 1.0F;
				Vector3f vector3f = new Vector3f(h, 0.0F, 0.0F);
				float s = camera.getLookVector().dot(vector3f);
				if (s < 0.0F) {
					s = 0.0F;
				}

				if (s > 0.0F && clientLevel.effects().isSunriseOrSunset(clientLevel.getTimeOfDay(f))) {
					int ad = clientLevel.effects().getSunriseOrSunsetColor(clientLevel.getTimeOfDay(f));
					s *= ARGB.from8BitChannel(ARGB.alpha(ad));
					u = u * (1.0F - s) + ARGB.from8BitChannel(ARGB.red(ad)) * s;
					v = v * (1.0F - s) + ARGB.from8BitChannel(ARGB.green(ad)) * s;
					w = w * (1.0F - s) + ARGB.from8BitChannel(ARGB.blue(ad)) * s;
				}
			}

			u += (z - u) * x;
			v += (aa - v) * x;
			w += (ab - w) * x;
			float hx = clientLevel.getRainLevel(f);
			if (hx > 0.0F) {
				float r = 1.0F - hx * 0.5F;
				float sx = 1.0F - hx * 0.4F;
				u *= r;
				v *= r;
				w *= sx;
			}

			float r = clientLevel.getThunderLevel(f);
			if (r > 0.0F) {
				float sx = 1.0F - r * 0.5F;
				u *= sx;
				v *= sx;
				w *= sx;
			}

			biomeChangedTime = -1L;
		}

		float xx = ((float)camera.getPosition().y - (float)clientLevel.getMinY()) * clientLevel.getLevelData().getClearColorScale();
		FogRenderer.MobEffectFogFunction mobEffectFogFunction = getPriorityFogFunction(entity, f);
		if (mobEffectFogFunction != null) {
			LivingEntity livingEntity = (LivingEntity)entity;
			xx = mobEffectFogFunction.getModifiedVoidDarkness(livingEntity, livingEntity.getEffect(mobEffectFogFunction.getMobEffect()), xx, f);
		}

		if (xx < 1.0F && fogType != FogType.LAVA && fogType != FogType.POWDER_SNOW) {
			if (xx < 0.0F) {
				xx = 0.0F;
			}

			xx *= xx;
			u *= xx;
			v *= xx;
			w *= xx;
		}

		if (g > 0.0F) {
			u = u * (1.0F - g) + u * 0.7F * g;
			v = v * (1.0F - g) + v * 0.6F * g;
			w = w * (1.0F - g) + w * 0.6F * g;
		}

		float zx;
		if (fogType == FogType.WATER) {
			if (entity instanceof LocalPlayer) {
				zx = ((LocalPlayer)entity).getWaterVision();
			} else {
				zx = 1.0F;
			}
		} else {
			label86: {
				if (entity instanceof LivingEntity livingEntity2 && livingEntity2.hasEffect(MobEffects.NIGHT_VISION) && !livingEntity2.hasEffect(MobEffects.DARKNESS)) {
					zx = GameRenderer.getNightVisionScale(livingEntity2, f);
					break label86;
				}

				zx = 0.0F;
			}
		}

		if (u != 0.0F && v != 0.0F && w != 0.0F) {
			float aax = Math.min(1.0F / u, Math.min(1.0F / v, 1.0F / w));
			u = u * (1.0F - zx) + u * aax * zx;
			v = v * (1.0F - zx) + v * aax * zx;
			w = w * (1.0F - zx) + w * aax * zx;
		}

		return new Vector4f(u, v, w, 1.0F);
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

	public static FogParameters setupFog(Camera camera, FogRenderer.FogMode fogMode, Vector4f vector4f, float f, boolean bl, float g) {
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
				fogData.end = 5.0F;
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
		} else if (fogMode == FogRenderer.FogMode.FOG_TERRAIN) {
			float h = Mth.clamp(f / 10.0F, 4.0F, 64.0F);
			fogData.start = f - h;
			fogData.end = f;
			fogData.shape = FogShape.CYLINDER;
		}

		return new FogParameters(fogData.start, fogData.end, fogData.shape, vector4f.x, vector4f.y, vector4f.z, vector4f.w);
	}

	@Environment(EnvType.CLIENT)
	static class BlindnessFogFunction implements FogRenderer.MobEffectFogFunction {
		@Override
		public Holder<MobEffect> getMobEffect() {
			return MobEffects.BLINDNESS;
		}

		@Override
		public void setupFog(FogRenderer.FogData fogData, LivingEntity livingEntity, MobEffectInstance mobEffectInstance, float f, float g) {
			float h = mobEffectInstance.isInfiniteDuration() ? 5.0F : Mth.lerp(Math.min(1.0F, (float)mobEffectInstance.getDuration() / 20.0F), f, 5.0F);
			if (fogData.mode == FogRenderer.FogMode.FOG_SKY) {
				fogData.start = 0.0F;
				fogData.end = h * 0.8F;
			} else if (fogData.mode == FogRenderer.FogMode.FOG_TERRAIN) {
				fogData.start = h * 0.25F;
				fogData.end = h;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static class DarknessFogFunction implements FogRenderer.MobEffectFogFunction {
		@Override
		public Holder<MobEffect> getMobEffect() {
			return MobEffects.DARKNESS;
		}

		@Override
		public void setupFog(FogRenderer.FogData fogData, LivingEntity livingEntity, MobEffectInstance mobEffectInstance, float f, float g) {
			float h = Mth.lerp(mobEffectInstance.getBlendFactor(livingEntity, g), f, 15.0F);

			fogData.start = switch (fogData.mode) {
				case FOG_SKY -> 0.0F;
				case FOG_TERRAIN -> h * 0.75F;
			};
			fogData.end = h;
		}

		@Override
		public float getModifiedVoidDarkness(LivingEntity livingEntity, MobEffectInstance mobEffectInstance, float f, float g) {
			return 1.0F - mobEffectInstance.getBlendFactor(livingEntity, g);
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
		Holder<MobEffect> getMobEffect();

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

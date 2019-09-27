package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.FloatBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class FogRenderer {
	private static final FloatBuffer BLACK_BUFFER = Util.make(
		MemoryTracker.createFloatBuffer(16), floatBuffer -> floatBuffer.put(0.0F).put(0.0F).put(0.0F).put(1.0F).flip()
	);
	private static final FloatBuffer COLOR_BUFFER = MemoryTracker.createFloatBuffer(16);
	private float fogRed;
	private float fogGreen;
	private float fogBlue;
	private int targetBiomeFog = -1;
	private int previousBiomeFog = -1;
	private long biomeChangedTime = -1L;

	public void setupClearColor(Camera camera, float f, Level level, int i, float g) {
		FluidState fluidState = camera.getFluidInCamera();
		float u;
		float v;
		float w;
		if (fluidState.is(FluidTags.WATER)) {
			long l = Util.getMillis();
			int j = level.getBiome(new BlockPos(camera.getPosition())).getWaterFogColor();
			if (this.biomeChangedTime < 0L) {
				this.targetBiomeFog = j;
				this.previousBiomeFog = j;
				this.biomeChangedTime = l;
			}

			int k = this.targetBiomeFog >> 16 & 0xFF;
			int m = this.targetBiomeFog >> 8 & 0xFF;
			int n = this.targetBiomeFog & 0xFF;
			int o = this.previousBiomeFog >> 16 & 0xFF;
			int p = this.previousBiomeFog >> 8 & 0xFF;
			int q = this.previousBiomeFog & 0xFF;
			float h = Mth.clamp((float)(l - this.biomeChangedTime) / 5000.0F, 0.0F, 1.0F);
			float r = Mth.lerp(h, (float)o, (float)k);
			float s = Mth.lerp(h, (float)p, (float)m);
			float t = Mth.lerp(h, (float)q, (float)n);
			u = r / 255.0F;
			v = s / 255.0F;
			w = t / 255.0F;
			if (this.targetBiomeFog != j) {
				this.targetBiomeFog = j;
				this.previousBiomeFog = Mth.floor(r) << 16 | Mth.floor(s) << 8 | Mth.floor(t);
				this.biomeChangedTime = l;
			}
		} else if (fluidState.is(FluidTags.LAVA)) {
			u = 0.6F;
			v = 0.1F;
			w = 0.0F;
			this.biomeChangedTime = -1L;
		} else {
			float x = 0.25F + 0.75F * (float)i / 32.0F;
			x = 1.0F - (float)Math.pow((double)x, 0.25);
			Vec3 vec3 = level.getSkyColor(camera.getBlockPosition(), f);
			float y = (float)vec3.x;
			float z = (float)vec3.y;
			float aa = (float)vec3.z;
			Vec3 vec32 = level.getFogColor(f);
			u = (float)vec32.x;
			v = (float)vec32.y;
			w = (float)vec32.z;
			if (i >= 4) {
				double d = Mth.sin(level.getSunAngle(f)) > 0.0F ? -1.0 : 1.0;
				Vec3 vec33 = new Vec3(d, 0.0, 0.0);
				float h = (float)camera.getLookVector().dot(vec33);
				if (h < 0.0F) {
					h = 0.0F;
				}

				if (h > 0.0F) {
					float[] fs = level.dimension.getSunriseColor(level.getTimeOfDay(f), f);
					if (fs != null) {
						h *= fs[3];
						u = u * (1.0F - h) + fs[0] * h;
						v = v * (1.0F - h) + fs[1] * h;
						w = w * (1.0F - h) + fs[2] * h;
					}
				}
			}

			u += (y - u) * x;
			v += (z - v) * x;
			w += (aa - w) * x;
			float ab = level.getRainLevel(f);
			if (ab > 0.0F) {
				float ac = 1.0F - ab * 0.5F;
				float ad = 1.0F - ab * 0.4F;
				u *= ac;
				v *= ac;
				w *= ad;
			}

			float ac = level.getThunderLevel(f);
			if (ac > 0.0F) {
				float ad = 1.0F - ac * 0.5F;
				u *= ad;
				v *= ad;
				w *= ad;
			}

			this.biomeChangedTime = -1L;
		}

		double e = camera.getPosition().y * level.dimension.getClearColorScale();
		if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
			int jx = ((LivingEntity)camera.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
			if (jx < 20) {
				e *= (double)(1.0F - (float)jx / 20.0F);
			} else {
				e = 0.0;
			}
		}

		if (e < 1.0) {
			if (e < 0.0) {
				e = 0.0;
			}

			e *= e;
			u = (float)((double)u * e);
			v = (float)((double)v * e);
			w = (float)((double)w * e);
		}

		if (g > 0.0F) {
			u = u * (1.0F - g) + u * 0.7F * g;
			v = v * (1.0F - g) + v * 0.6F * g;
			w = w * (1.0F - g) + w * 0.6F * g;
		}

		if (fluidState.is(FluidTags.WATER)) {
			float yx = 0.0F;
			if (camera.getEntity() instanceof LocalPlayer) {
				LocalPlayer localPlayer = (LocalPlayer)camera.getEntity();
				yx = localPlayer.getWaterVision();
			}

			float zx = Math.min(1.0F / u, Math.min(1.0F / v, 1.0F / w));
			u = u * (1.0F - yx) + u * zx * yx;
			v = v * (1.0F - yx) + v * zx * yx;
			w = w * (1.0F - yx) + w * zx * yx;
		} else if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.NIGHT_VISION)) {
			float yx = GameRenderer.getNightVisionScale((LivingEntity)camera.getEntity(), f);
			float zx = Math.min(1.0F / u, Math.min(1.0F / v, 1.0F / w));
			u = u * (1.0F - yx) + u * zx * yx;
			v = v * (1.0F - yx) + v * zx * yx;
			w = w * (1.0F - yx) + w * zx * yx;
		}

		RenderSystem.clearColor(u, v, w, 0.0F);
		if (this.fogRed != u || this.fogGreen != v || this.fogBlue != w) {
			COLOR_BUFFER.clear();
			COLOR_BUFFER.put(u).put(v).put(w).put(1.0F);
			COLOR_BUFFER.flip();
			this.fogRed = u;
			this.fogGreen = v;
			this.fogBlue = w;
		}
	}

	public static void setupFog(Camera camera, FogRenderer.FogMode fogMode, float f, boolean bl) {
		resetFogColor(false);
		RenderSystem.normal3f(0.0F, -1.0F, 0.0F);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		FluidState fluidState = camera.getFluidInCamera();
		if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
			float g = 5.0F;
			int i = ((LivingEntity)camera.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
			if (i < 20) {
				g = Mth.lerp(1.0F - (float)i / 20.0F, 5.0F, f);
			}

			RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
			if (fogMode == FogRenderer.FogMode.FOG_SKY) {
				RenderSystem.fogStart(0.0F);
				RenderSystem.fogEnd(g * 0.8F);
			} else {
				RenderSystem.fogStart(g * 0.25F);
				RenderSystem.fogEnd(g);
			}

			RenderSystem.setupNvFogDistance();
		} else if (fluidState.is(FluidTags.WATER)) {
			RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
			if (camera.getEntity() instanceof LivingEntity) {
				if (camera.getEntity() instanceof LocalPlayer) {
					LocalPlayer localPlayer = (LocalPlayer)camera.getEntity();
					float h = 0.05F - localPlayer.getWaterVision() * localPlayer.getWaterVision() * 0.03F;
					Biome biome = localPlayer.level.getBiome(new BlockPos(localPlayer));
					if (biome == Biomes.SWAMP || biome == Biomes.SWAMP_HILLS) {
						h += 0.005F;
					}

					RenderSystem.fogDensity(h);
				} else {
					RenderSystem.fogDensity(0.05F);
				}
			} else {
				RenderSystem.fogDensity(0.1F);
			}
		} else if (fluidState.is(FluidTags.LAVA)) {
			RenderSystem.fogMode(GlStateManager.FogMode.EXP);
			RenderSystem.fogDensity(2.0F);
		} else {
			RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
			if (fogMode == FogRenderer.FogMode.FOG_SKY) {
				RenderSystem.fogStart(0.0F);
				RenderSystem.fogEnd(f);
			} else {
				RenderSystem.fogStart(f * 0.75F);
				RenderSystem.fogEnd(f);
			}

			RenderSystem.setupNvFogDistance();
			if (bl) {
				RenderSystem.fogStart(f * 0.05F);
				RenderSystem.fogEnd(Math.min(f, 192.0F) * 0.5F);
			}
		}

		RenderSystem.enableFog();
		RenderSystem.colorMaterial(1028, 4608);
	}

	public static void resetFogColor(boolean bl) {
		RenderSystem.fog(2918, bl ? BLACK_BUFFER : COLOR_BUFFER);
	}

	@Environment(EnvType.CLIENT)
	public static enum FogMode {
		FOG_SKY,
		FOG_TERRAIN;
	}
}

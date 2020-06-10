package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class FogRenderer {
	private static float fogRed;
	private static float fogGreen;
	private static float fogBlue;
	private static int targetBiomeFog = -1;
	private static int previousBiomeFog = -1;
	private static long biomeChangedTime = -1L;

	public static void setupColor(Camera camera, float f, ClientLevel clientLevel, int i, float g) {
		FluidState fluidState = camera.getFluidInCamera();
		if (fluidState.is(FluidTags.WATER)) {
			long l = Util.getMillis();
			int j = clientLevel.getBiome(new BlockPos(camera.getPosition())).getWaterFogColor();
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
		} else if (fluidState.is(FluidTags.LAVA)) {
			fogRed = 0.6F;
			fogGreen = 0.1F;
			fogBlue = 0.0F;
			biomeChangedTime = -1L;
		} else {
			float u = 0.25F + 0.75F * (float)i / 32.0F;
			u = 1.0F - (float)Math.pow((double)u, 0.25);
			Vec3 vec3 = clientLevel.getSkyColor(camera.getBlockPosition(), f);
			float v = (float)vec3.x;
			float w = (float)vec3.y;
			float x = (float)vec3.z;
			float y = Mth.clamp(Mth.cos(clientLevel.getTimeOfDay(f) * (float) (Math.PI * 2)) * 2.0F + 0.5F, 0.0F, 1.0F);
			BiomeManager biomeManager = clientLevel.getBiomeManager();
			Vec3 vec32 = camera.getPosition().subtract(2.0, 2.0, 2.0).scale(0.25);
			Vec3 vec33 = CubicSampler.gaussianSampleVec3(
				vec32, (ix, jx, k) -> clientLevel.effects().getBrightnessDependentFogColor(Vec3.fromRGB24(biomeManager.getNoiseBiomeAtQuart(ix, jx, k).getFogColor()), y)
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

		double d = camera.getPosition().y * clientLevel.getLevelData().getClearColorScale();
		if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
			int jx = ((LivingEntity)camera.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
			if (jx < 20) {
				d *= (double)(1.0F - (float)jx / 20.0F);
			} else {
				d = 0.0;
			}
		}

		if (d < 1.0 && !fluidState.is(FluidTags.LAVA)) {
			if (d < 0.0) {
				d = 0.0;
			}

			d *= d;
			fogRed = (float)((double)fogRed * d);
			fogGreen = (float)((double)fogGreen * d);
			fogBlue = (float)((double)fogBlue * d);
		}

		if (g > 0.0F) {
			fogRed = fogRed * (1.0F - g) + fogRed * 0.7F * g;
			fogGreen = fogGreen * (1.0F - g) + fogGreen * 0.6F * g;
			fogBlue = fogBlue * (1.0F - g) + fogBlue * 0.6F * g;
		}

		if (fluidState.is(FluidTags.WATER)) {
			float vx = 0.0F;
			if (camera.getEntity() instanceof LocalPlayer) {
				LocalPlayer localPlayer = (LocalPlayer)camera.getEntity();
				vx = localPlayer.getWaterVision();
			}

			float wx = Math.min(1.0F / fogRed, Math.min(1.0F / fogGreen, 1.0F / fogBlue));
			fogRed = fogRed * (1.0F - vx) + fogRed * wx * vx;
			fogGreen = fogGreen * (1.0F - vx) + fogGreen * wx * vx;
			fogBlue = fogBlue * (1.0F - vx) + fogBlue * wx * vx;
		} else if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.NIGHT_VISION)) {
			float vx = GameRenderer.getNightVisionScale((LivingEntity)camera.getEntity(), f);
			float wx = Math.min(1.0F / fogRed, Math.min(1.0F / fogGreen, 1.0F / fogBlue));
			fogRed = fogRed * (1.0F - vx) + fogRed * wx * vx;
			fogGreen = fogGreen * (1.0F - vx) + fogGreen * wx * vx;
			fogBlue = fogBlue * (1.0F - vx) + fogBlue * wx * vx;
		}

		RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
	}

	public static void setupNoFog() {
		RenderSystem.fogDensity(0.0F);
		RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
	}

	public static void setupFog(Camera camera, FogRenderer.FogMode fogMode, float f, boolean bl) {
		FluidState fluidState = camera.getFluidInCamera();
		Entity entity = camera.getEntity();
		boolean bl2 = fluidState.getType() != Fluids.EMPTY;
		if (fluidState.is(FluidTags.WATER)) {
			float g = 1.0F;
			g = 0.05F;
			if (entity instanceof LocalPlayer) {
				LocalPlayer localPlayer = (LocalPlayer)entity;
				g -= localPlayer.getWaterVision() * localPlayer.getWaterVision() * 0.03F;
				Biome biome = localPlayer.level.getBiome(localPlayer.blockPosition());
				if (biome == Biomes.SWAMP || biome == Biomes.SWAMP_HILLS) {
					g += 0.005F;
				}
			}

			RenderSystem.fogDensity(g);
			RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
		} else {
			float g;
			float h;
			if (fluidState.is(FluidTags.LAVA)) {
				if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.FIRE_RESISTANCE)) {
					g = 0.0F;
					h = 3.0F;
				} else {
					g = 0.25F;
					h = 1.0F;
				}
			} else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.BLINDNESS)) {
				int i = ((LivingEntity)entity).getEffect(MobEffects.BLINDNESS).getDuration();
				float j = Mth.lerp(Math.min(1.0F, (float)i / 20.0F), f, 5.0F);
				if (fogMode == FogRenderer.FogMode.FOG_SKY) {
					g = 0.0F;
					h = j * 0.8F;
				} else {
					g = j * 0.25F;
					h = j;
				}
			} else if (bl) {
				g = f * 0.05F;
				h = Math.min(f, 192.0F) * 0.5F;
			} else if (fogMode == FogRenderer.FogMode.FOG_SKY) {
				g = 0.0F;
				h = f;
			} else {
				g = f * 0.75F;
				h = f;
			}

			RenderSystem.fogStart(g);
			RenderSystem.fogEnd(h);
			RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
			RenderSystem.setupNvFogDistance();
		}
	}

	public static void levelFogColor() {
		RenderSystem.fog(2918, fogRed, fogGreen, fogBlue, 1.0F);
	}

	@Environment(EnvType.CLIENT)
	public static enum FogMode {
		FOG_SKY,
		FOG_TERRAIN;
	}
}

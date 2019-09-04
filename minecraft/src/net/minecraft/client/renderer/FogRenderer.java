package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.FloatBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class FogRenderer {
	private final FloatBuffer blackBuffer = MemoryTracker.createFloatBuffer(16);
	private final FloatBuffer colorBuffer = MemoryTracker.createFloatBuffer(16);
	private float fogRed;
	private float fogGreen;
	private float fogBlue;
	private float oldRed = -1.0F;
	private float oldGreen = -1.0F;
	private float oldBlue = -1.0F;
	private int targetBiomeFog = -1;
	private int previousBiomeFog = -1;
	private long biomeChangedTime = -1L;
	private final GameRenderer renderer;
	private final Minecraft minecraft;

	public FogRenderer(GameRenderer gameRenderer) {
		this.renderer = gameRenderer;
		this.minecraft = gameRenderer.getMinecraft();
		this.blackBuffer.put(0.0F).put(0.0F).put(0.0F).put(1.0F).flip();
	}

	public void setupClearColor(Camera camera, float f) {
		Level level = this.minecraft.level;
		FluidState fluidState = camera.getFluidInCamera();
		if (fluidState.is(FluidTags.WATER)) {
			this.setWaterFogColor(camera, level);
		} else if (fluidState.is(FluidTags.LAVA)) {
			this.fogRed = 0.6F;
			this.fogGreen = 0.1F;
			this.fogBlue = 0.0F;
			this.biomeChangedTime = -1L;
		} else {
			this.setLandFogColor(camera, level, f);
			this.biomeChangedTime = -1L;
		}

		double d = camera.getPosition().y * level.dimension.getClearColorScale();
		if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
			int i = ((LivingEntity)camera.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
			if (i < 20) {
				d *= (double)(1.0F - (float)i / 20.0F);
			} else {
				d = 0.0;
			}
		}

		if (d < 1.0) {
			if (d < 0.0) {
				d = 0.0;
			}

			d *= d;
			this.fogRed = (float)((double)this.fogRed * d);
			this.fogGreen = (float)((double)this.fogGreen * d);
			this.fogBlue = (float)((double)this.fogBlue * d);
		}

		if (this.renderer.getDarkenWorldAmount(f) > 0.0F) {
			float g = this.renderer.getDarkenWorldAmount(f);
			this.fogRed = this.fogRed * (1.0F - g) + this.fogRed * 0.7F * g;
			this.fogGreen = this.fogGreen * (1.0F - g) + this.fogGreen * 0.6F * g;
			this.fogBlue = this.fogBlue * (1.0F - g) + this.fogBlue * 0.6F * g;
		}

		if (fluidState.is(FluidTags.WATER)) {
			float g = 0.0F;
			if (camera.getEntity() instanceof LocalPlayer) {
				LocalPlayer localPlayer = (LocalPlayer)camera.getEntity();
				g = localPlayer.getWaterVision();
			}

			float h = 1.0F / this.fogRed;
			if (h > 1.0F / this.fogGreen) {
				h = 1.0F / this.fogGreen;
			}

			if (h > 1.0F / this.fogBlue) {
				h = 1.0F / this.fogBlue;
			}

			this.fogRed = this.fogRed * (1.0F - g) + this.fogRed * h * g;
			this.fogGreen = this.fogGreen * (1.0F - g) + this.fogGreen * h * g;
			this.fogBlue = this.fogBlue * (1.0F - g) + this.fogBlue * h * g;
		} else if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.NIGHT_VISION)) {
			float gx = this.renderer.getNightVisionScale((LivingEntity)camera.getEntity(), f);
			float hx = 1.0F / this.fogRed;
			if (hx > 1.0F / this.fogGreen) {
				hx = 1.0F / this.fogGreen;
			}

			if (hx > 1.0F / this.fogBlue) {
				hx = 1.0F / this.fogBlue;
			}

			this.fogRed = this.fogRed * (1.0F - gx) + this.fogRed * hx * gx;
			this.fogGreen = this.fogGreen * (1.0F - gx) + this.fogGreen * hx * gx;
			this.fogBlue = this.fogBlue * (1.0F - gx) + this.fogBlue * hx * gx;
		}

		RenderSystem.clearColor(this.fogRed, this.fogGreen, this.fogBlue, 0.0F);
	}

	private void setLandFogColor(Camera camera, Level level, float f) {
		float g = 0.25F + 0.75F * (float)this.minecraft.options.renderDistance / 32.0F;
		g = 1.0F - (float)Math.pow((double)g, 0.25);
		Vec3 vec3 = level.getSkyColor(camera.getBlockPosition(), f);
		float h = (float)vec3.x;
		float i = (float)vec3.y;
		float j = (float)vec3.z;
		Vec3 vec32 = level.getFogColor(f);
		this.fogRed = (float)vec32.x;
		this.fogGreen = (float)vec32.y;
		this.fogBlue = (float)vec32.z;
		if (this.minecraft.options.renderDistance >= 4) {
			double d = Mth.sin(level.getSunAngle(f)) > 0.0F ? -1.0 : 1.0;
			Vec3 vec33 = new Vec3(d, 0.0, 0.0);
			float k = (float)camera.getLookVector().dot(vec33);
			if (k < 0.0F) {
				k = 0.0F;
			}

			if (k > 0.0F) {
				float[] fs = level.dimension.getSunriseColor(level.getTimeOfDay(f), f);
				if (fs != null) {
					k *= fs[3];
					this.fogRed = this.fogRed * (1.0F - k) + fs[0] * k;
					this.fogGreen = this.fogGreen * (1.0F - k) + fs[1] * k;
					this.fogBlue = this.fogBlue * (1.0F - k) + fs[2] * k;
				}
			}
		}

		this.fogRed = this.fogRed + (h - this.fogRed) * g;
		this.fogGreen = this.fogGreen + (i - this.fogGreen) * g;
		this.fogBlue = this.fogBlue + (j - this.fogBlue) * g;
		float l = level.getRainLevel(f);
		if (l > 0.0F) {
			float m = 1.0F - l * 0.5F;
			float n = 1.0F - l * 0.4F;
			this.fogRed *= m;
			this.fogGreen *= m;
			this.fogBlue *= n;
		}

		float m = level.getThunderLevel(f);
		if (m > 0.0F) {
			float n = 1.0F - m * 0.5F;
			this.fogRed *= n;
			this.fogGreen *= n;
			this.fogBlue *= n;
		}
	}

	private void setWaterFogColor(Camera camera, LevelReader levelReader) {
		long l = Util.getMillis();
		int i = levelReader.getBiome(new BlockPos(camera.getPosition())).getWaterFogColor();
		if (this.biomeChangedTime < 0L) {
			this.targetBiomeFog = i;
			this.previousBiomeFog = i;
			this.biomeChangedTime = l;
		}

		int j = this.targetBiomeFog >> 16 & 0xFF;
		int k = this.targetBiomeFog >> 8 & 0xFF;
		int m = this.targetBiomeFog & 0xFF;
		int n = this.previousBiomeFog >> 16 & 0xFF;
		int o = this.previousBiomeFog >> 8 & 0xFF;
		int p = this.previousBiomeFog & 0xFF;
		float f = Mth.clamp((float)(l - this.biomeChangedTime) / 5000.0F, 0.0F, 1.0F);
		float g = Mth.lerp(f, (float)n, (float)j);
		float h = Mth.lerp(f, (float)o, (float)k);
		float q = Mth.lerp(f, (float)p, (float)m);
		this.fogRed = g / 255.0F;
		this.fogGreen = h / 255.0F;
		this.fogBlue = q / 255.0F;
		if (this.targetBiomeFog != i) {
			this.targetBiomeFog = i;
			this.previousBiomeFog = Mth.floor(g) << 16 | Mth.floor(h) << 8 | Mth.floor(q);
			this.biomeChangedTime = l;
		}
	}

	public void setupFog(Camera camera, int i) {
		this.resetFogColor(false);
		RenderSystem.normal3f(0.0F, -1.0F, 0.0F);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		FluidState fluidState = camera.getFluidInCamera();
		if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
			float f = 5.0F;
			int j = ((LivingEntity)camera.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
			if (j < 20) {
				f = Mth.lerp(1.0F - (float)j / 20.0F, 5.0F, this.renderer.getRenderDistance());
			}

			RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
			if (i == -1) {
				RenderSystem.fogStart(0.0F);
				RenderSystem.fogEnd(f * 0.8F);
			} else {
				RenderSystem.fogStart(f * 0.25F);
				RenderSystem.fogEnd(f);
			}

			RenderSystem.setupNvFogDistance();
		} else if (fluidState.is(FluidTags.WATER)) {
			RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
			if (camera.getEntity() instanceof LivingEntity) {
				if (camera.getEntity() instanceof LocalPlayer) {
					LocalPlayer localPlayer = (LocalPlayer)camera.getEntity();
					float g = 0.05F - localPlayer.getWaterVision() * localPlayer.getWaterVision() * 0.03F;
					Biome biome = localPlayer.level.getBiome(new BlockPos(localPlayer));
					if (biome == Biomes.SWAMP || biome == Biomes.SWAMP_HILLS) {
						g += 0.005F;
					}

					RenderSystem.fogDensity(g);
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
			float fx = this.renderer.getRenderDistance();
			RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
			if (i == -1) {
				RenderSystem.fogStart(0.0F);
				RenderSystem.fogEnd(fx);
			} else {
				RenderSystem.fogStart(fx * 0.75F);
				RenderSystem.fogEnd(fx);
			}

			RenderSystem.setupNvFogDistance();
			if (this.minecraft.level.dimension.isFoggyAt(Mth.floor(camera.getPosition().x), Mth.floor(camera.getPosition().z))
				|| this.minecraft.gui.getBossOverlay().shouldCreateWorldFog()) {
				RenderSystem.fogStart(fx * 0.05F);
				RenderSystem.fogEnd(Math.min(fx, 192.0F) * 0.5F);
			}
		}

		RenderSystem.enableColorMaterial();
		RenderSystem.enableFog();
		RenderSystem.colorMaterial(1028, 4608);
	}

	public void resetFogColor(boolean bl) {
		if (bl) {
			RenderSystem.fog(2918, this.blackBuffer);
		} else {
			RenderSystem.fog(2918, this.updateColorBuffer());
		}
	}

	private FloatBuffer updateColorBuffer() {
		if (this.oldRed != this.fogRed || this.oldGreen != this.fogGreen || this.oldBlue != this.fogBlue) {
			this.colorBuffer.clear();
			this.colorBuffer.put(this.fogRed).put(this.fogGreen).put(this.fogBlue).put(1.0F);
			this.colorBuffer.flip();
			this.oldRed = this.fogRed;
			this.oldGreen = this.fogGreen;
			this.oldBlue = this.fogBlue;
		}

		return this.colorBuffer;
	}
}

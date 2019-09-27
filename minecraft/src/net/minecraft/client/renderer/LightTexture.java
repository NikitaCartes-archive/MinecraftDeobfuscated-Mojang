package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

@Environment(EnvType.CLIENT)
public class LightTexture implements AutoCloseable {
	private final DynamicTexture lightTexture;
	private final NativeImage lightPixels;
	private final ResourceLocation lightTextureLocation;
	private boolean updateLightTexture;
	private float blockLightRed;
	private float blockLightRedTotal;
	private final GameRenderer renderer;
	private final Minecraft minecraft;

	public LightTexture(GameRenderer gameRenderer, Minecraft minecraft) {
		this.renderer = gameRenderer;
		this.minecraft = minecraft;
		this.lightTexture = new DynamicTexture(16, 16, false);
		this.lightTextureLocation = this.minecraft.getTextureManager().register("light_map", this.lightTexture);
		this.lightPixels = this.lightTexture.getPixels();
	}

	public void close() {
		this.lightTexture.close();
	}

	public void tick() {
		this.blockLightRedTotal = (float)((double)this.blockLightRedTotal + (Math.random() - Math.random()) * Math.random() * Math.random());
		this.blockLightRedTotal = (float)((double)this.blockLightRedTotal * 0.9);
		this.blockLightRed = this.blockLightRed + (this.blockLightRedTotal - this.blockLightRed);
		this.updateLightTexture = true;
	}

	public void turnOffLightLayer() {
		RenderSystem.activeTexture(33986);
		RenderSystem.disableTexture();
		RenderSystem.activeTexture(33984);
	}

	public void turnOnLightLayer() {
		RenderSystem.activeTexture(33986);
		RenderSystem.matrixMode(5890);
		RenderSystem.loadIdentity();
		float f = 0.00390625F;
		RenderSystem.scalef(0.00390625F, 0.00390625F, 0.00390625F);
		RenderSystem.translatef(8.0F, 8.0F, 8.0F);
		RenderSystem.matrixMode(5888);
		this.minecraft.getTextureManager().bind(this.lightTextureLocation);
		RenderSystem.texParameter(3553, 10241, 9729);
		RenderSystem.texParameter(3553, 10240, 9729);
		RenderSystem.texParameter(3553, 10242, 10496);
		RenderSystem.texParameter(3553, 10243, 10496);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableTexture();
		RenderSystem.activeTexture(33984);
	}

	public void updateLightTexture(float f) {
		if (this.updateLightTexture) {
			this.minecraft.getProfiler().push("lightTex");
			Level level = this.minecraft.level;
			if (level != null) {
				float g = level.getSkyDarken(1.0F);
				float h = g * 0.95F + 0.05F;
				float i = this.minecraft.player.getWaterVision();
				float j;
				if (this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION)) {
					j = GameRenderer.getNightVisionScale(this.minecraft.player, f);
				} else if (i > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER)) {
					j = i;
				} else {
					j = 0.0F;
				}

				for (int k = 0; k < 16; k++) {
					for (int l = 0; l < 16; l++) {
						float m = level.dimension.getBrightnessRamp()[k] * h;
						float n = level.dimension.getBrightnessRamp()[l] * (this.blockLightRed * 0.1F + 1.5F);
						if (level.getSkyFlashTime() > 0) {
							m = level.dimension.getBrightnessRamp()[k];
						}

						float o = m * (g * 0.65F + 0.35F);
						float p = m * (g * 0.65F + 0.35F);
						float s = n * ((n * 0.6F + 0.4F) * 0.6F + 0.4F);
						float t = n * (n * n * 0.6F + 0.4F);
						float u = o + n;
						float v = p + s;
						float w = m + t;
						u = u * 0.96F + 0.03F;
						v = v * 0.96F + 0.03F;
						w = w * 0.96F + 0.03F;
						if (this.renderer.getDarkenWorldAmount(f) > 0.0F) {
							float x = this.renderer.getDarkenWorldAmount(f);
							u = u * (1.0F - x) + u * 0.7F * x;
							v = v * (1.0F - x) + v * 0.6F * x;
							w = w * (1.0F - x) + w * 0.6F * x;
						}

						if (level.dimension.getType() == DimensionType.THE_END) {
							u = 0.22F + n * 0.75F;
							v = 0.28F + s * 0.75F;
							w = 0.25F + t * 0.75F;
						}

						if (j > 0.0F) {
							float x = 1.0F / u;
							if (x > 1.0F / v) {
								x = 1.0F / v;
							}

							if (x > 1.0F / w) {
								x = 1.0F / w;
							}

							u = u * (1.0F - j) + u * x * j;
							v = v * (1.0F - j) + v * x * j;
							w = w * (1.0F - j) + w * x * j;
						}

						if (u > 1.0F) {
							u = 1.0F;
						}

						if (v > 1.0F) {
							v = 1.0F;
						}

						if (w > 1.0F) {
							w = 1.0F;
						}

						float xx = (float)this.minecraft.options.gamma;
						float y = 1.0F - u;
						float z = 1.0F - v;
						float aa = 1.0F - w;
						y = 1.0F - y * y * y * y;
						z = 1.0F - z * z * z * z;
						aa = 1.0F - aa * aa * aa * aa;
						u = u * (1.0F - xx) + y * xx;
						v = v * (1.0F - xx) + z * xx;
						w = w * (1.0F - xx) + aa * xx;
						u = u * 0.96F + 0.03F;
						v = v * 0.96F + 0.03F;
						w = w * 0.96F + 0.03F;
						if (u > 1.0F) {
							u = 1.0F;
						}

						if (v > 1.0F) {
							v = 1.0F;
						}

						if (w > 1.0F) {
							w = 1.0F;
						}

						if (u < 0.0F) {
							u = 0.0F;
						}

						if (v < 0.0F) {
							v = 0.0F;
						}

						if (w < 0.0F) {
							w = 0.0F;
						}

						int ab = 255;
						int ac = (int)(u * 255.0F);
						int ad = (int)(v * 255.0F);
						int ae = (int)(w * 255.0F);
						this.lightPixels.setPixelRGBA(l, k, 0xFF000000 | ae << 16 | ad << 8 | ac);
					}
				}

				this.lightTexture.upload();
				this.updateLightTexture = false;
				this.minecraft.getProfiler().pop();
			}
		}
	}
}

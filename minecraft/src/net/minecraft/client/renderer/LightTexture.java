package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class LightTexture implements AutoCloseable {
	public static final int FULL_BRIGHT = 15728880;
	public static final int FULL_SKY = 15728640;
	public static final int FULL_BLOCK = 240;
	private final DynamicTexture lightTexture;
	private final NativeImage lightPixels;
	private final ResourceLocation lightTextureLocation;
	private boolean updateLightTexture;
	private float blockLightRedFlicker;
	private final GameRenderer renderer;
	private final Minecraft minecraft;

	public LightTexture(GameRenderer gameRenderer, Minecraft minecraft) {
		this.renderer = gameRenderer;
		this.minecraft = minecraft;
		this.lightTexture = new DynamicTexture(16, 16, false);
		this.lightTextureLocation = this.minecraft.getTextureManager().register("light_map", this.lightTexture);
		this.lightPixels = this.lightTexture.getPixels();

		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				this.lightPixels.setPixelRGBA(j, i, -1);
			}
		}

		this.lightTexture.upload();
	}

	public void close() {
		this.lightTexture.close();
	}

	public void tick() {
		this.blockLightRedFlicker = (float)((double)this.blockLightRedFlicker + (Math.random() - Math.random()) * Math.random() * Math.random() * 0.1);
		this.blockLightRedFlicker = (float)((double)this.blockLightRedFlicker * 0.9);
		this.updateLightTexture = true;
	}

	public void turnOffLightLayer() {
		RenderSystem.setShaderTexture(2, 0);
	}

	public void turnOnLightLayer() {
		RenderSystem.setShaderTexture(2, this.lightTextureLocation);
		this.minecraft.getTextureManager().bindForSetup(this.lightTextureLocation);
		RenderSystem.texParameter(3553, 10241, 9729);
		RenderSystem.texParameter(3553, 10240, 9729);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public void updateLightTexture(float f) {
		if (this.updateLightTexture) {
			this.updateLightTexture = false;
			this.minecraft.getProfiler().push("lightTex");
			ClientLevel clientLevel = this.minecraft.level;
			if (clientLevel != null) {
				float g = clientLevel.getSkyDarken(1.0F);
				float h;
				if (clientLevel.getSkyFlashTime() > 0) {
					h = 1.0F;
				} else {
					h = g * 0.95F + 0.05F;
				}

				float i = this.minecraft.player.getWaterVision();
				float j;
				if (this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION)) {
					j = GameRenderer.getNightVisionScale(this.minecraft.player, f);
				} else if (i > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER)) {
					j = i;
				} else {
					j = 0.0F;
				}

				Vector3f vector3f = new Vector3f(g, g, 1.0F);
				vector3f.lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
				float k = this.blockLightRedFlicker + 1.5F;
				Vector3f vector3f2 = new Vector3f();

				for (int l = 0; l < 16; l++) {
					for (int m = 0; m < 16; m++) {
						float n = this.getBrightness(clientLevel, l) * h;
						float o = this.getBrightness(clientLevel, m) * k;
						float q = o * ((o * 0.6F + 0.4F) * 0.6F + 0.4F);
						float r = o * (o * o * 0.6F + 0.4F);
						vector3f2.set(o, q, r);
						if (clientLevel.effects().forceBrightLightmap()) {
							vector3f2.lerp(new Vector3f(0.99F, 1.12F, 1.0F), 0.25F);
						} else {
							Vector3f vector3f3 = vector3f.copy();
							vector3f3.mul(n);
							vector3f2.add(vector3f3);
							vector3f2.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
							if (this.renderer.getDarkenWorldAmount(f) > 0.0F) {
								float s = this.renderer.getDarkenWorldAmount(f);
								Vector3f vector3f4 = vector3f2.copy();
								vector3f4.mul(0.7F, 0.6F, 0.6F);
								vector3f2.lerp(vector3f4, s);
							}
						}

						vector3f2.clamp(0.0F, 1.0F);
						if (j > 0.0F) {
							float t = Math.max(vector3f2.x(), Math.max(vector3f2.y(), vector3f2.z()));
							if (t < 1.0F) {
								float s = 1.0F / t;
								Vector3f vector3f4 = vector3f2.copy();
								vector3f4.mul(s);
								vector3f2.lerp(vector3f4, j);
							}
						}

						float t = (float)this.minecraft.options.gamma;
						Vector3f vector3f5 = vector3f2.copy();
						vector3f5.map(this::notGamma);
						vector3f2.lerp(vector3f5, t);
						vector3f2.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
						vector3f2.clamp(0.0F, 1.0F);
						vector3f2.mul(255.0F);
						int u = 255;
						int v = (int)vector3f2.x();
						int w = (int)vector3f2.y();
						int x = (int)vector3f2.z();
						this.lightPixels.setPixelRGBA(m, l, 0xFF000000 | x << 16 | w << 8 | v);
					}
				}

				this.lightTexture.upload();
				this.minecraft.getProfiler().pop();
			}
		}
	}

	private float notGamma(float f) {
		float g = 1.0F - f;
		return 1.0F - g * g * g * g;
	}

	private float getBrightness(Level level, int i) {
		return level.dimensionType().brightness(i);
	}

	public static int pack(int i, int j) {
		return i << 4 | j << 20;
	}

	public static int block(int i) {
		return i >> 4 & 65535;
	}

	public static int sky(int i) {
		return i >> 20 & 65535;
	}
}

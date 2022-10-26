package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Vector3f;

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
		this.blockLightRedFlicker = this.blockLightRedFlicker + (float)((Math.random() - Math.random()) * Math.random() * Math.random() * 0.1);
		this.blockLightRedFlicker *= 0.9F;
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

	private float getDarknessGamma(float f) {
		if (this.minecraft.player.hasEffect(MobEffects.DARKNESS)) {
			MobEffectInstance mobEffectInstance = this.minecraft.player.getEffect(MobEffects.DARKNESS);
			if (mobEffectInstance != null && mobEffectInstance.getFactorData().isPresent()) {
				return ((MobEffectInstance.FactorData)mobEffectInstance.getFactorData().get()).getFactor(this.minecraft.player, f);
			}
		}

		return 0.0F;
	}

	private float calculateDarknessScale(LivingEntity livingEntity, float f, float g) {
		float h = 0.45F * f;
		return Math.max(0.0F, Mth.cos(((float)livingEntity.tickCount - g) * (float) Math.PI * 0.025F) * h);
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

				float i = this.minecraft.options.darknessEffectScale().get().floatValue();
				float j = this.getDarknessGamma(f) * i;
				float k = this.calculateDarknessScale(this.minecraft.player, j, f) * i;
				float l = this.minecraft.player.getWaterVision();
				float m;
				if (this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION)) {
					m = GameRenderer.getNightVisionScale(this.minecraft.player, f);
				} else if (l > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER)) {
					m = l;
				} else {
					m = 0.0F;
				}

				Vector3f vector3f = new Vector3f(g, g, 1.0F).lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
				float n = this.blockLightRedFlicker + 1.5F;
				Vector3f vector3f2 = new Vector3f();

				for (int o = 0; o < 16; o++) {
					for (int p = 0; p < 16; p++) {
						float q = getBrightness(clientLevel.dimensionType(), o) * h;
						float r = getBrightness(clientLevel.dimensionType(), p) * n;
						float t = r * ((r * 0.6F + 0.4F) * 0.6F + 0.4F);
						float u = r * (r * r * 0.6F + 0.4F);
						vector3f2.set(r, t, u);
						boolean bl = clientLevel.effects().forceBrightLightmap();
						if (bl) {
							vector3f2.lerp(new Vector3f(0.99F, 1.12F, 1.0F), 0.25F);
							clampColor(vector3f2);
						} else {
							Vector3f vector3f3 = new Vector3f(vector3f).mul(q);
							vector3f2.add(vector3f3);
							vector3f2.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
							if (this.renderer.getDarkenWorldAmount(f) > 0.0F) {
								float v = this.renderer.getDarkenWorldAmount(f);
								Vector3f vector3f4 = new Vector3f(vector3f2).mul(0.7F, 0.6F, 0.6F);
								vector3f2.lerp(vector3f4, v);
							}
						}

						if (m > 0.0F) {
							float w = Math.max(vector3f2.x(), Math.max(vector3f2.y(), vector3f2.z()));
							if (w < 1.0F) {
								float v = 1.0F / w;
								Vector3f vector3f4 = new Vector3f(vector3f2).mul(v);
								vector3f2.lerp(vector3f4, m);
							}
						}

						if (!bl) {
							if (k > 0.0F) {
								vector3f2.add(-k, -k, -k);
							}

							clampColor(vector3f2);
						}

						float w = this.minecraft.options.gamma().get().floatValue();
						Vector3f vector3f5 = new Vector3f(this.notGamma(vector3f2.x), this.notGamma(vector3f2.y), this.notGamma(vector3f2.z));
						vector3f2.lerp(vector3f5, Math.max(0.0F, w - j));
						vector3f2.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
						clampColor(vector3f2);
						vector3f2.mul(255.0F);
						int x = 255;
						int y = (int)vector3f2.x();
						int z = (int)vector3f2.y();
						int aa = (int)vector3f2.z();
						this.lightPixels.setPixelRGBA(p, o, 0xFF000000 | aa << 16 | z << 8 | y);
					}
				}

				this.lightTexture.upload();
				this.minecraft.getProfiler().pop();
			}
		}
	}

	private static void clampColor(Vector3f vector3f) {
		vector3f.set(Mth.clamp(vector3f.x, 0.0F, 1.0F), Mth.clamp(vector3f.y, 0.0F, 1.0F), Mth.clamp(vector3f.z, 0.0F, 1.0F));
	}

	private float notGamma(float f) {
		float g = 1.0F - f;
		return 1.0F - g * g * g * g;
	}

	public static float getBrightness(DimensionType dimensionType, int i) {
		float f = (float)i / 15.0F;
		float g = f / (4.0F - 3.0F * f);
		return Mth.lerp(dimensionType.ambientLight(), g, 1.0F);
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

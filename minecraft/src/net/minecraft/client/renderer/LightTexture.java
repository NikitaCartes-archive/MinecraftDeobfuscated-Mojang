package net.minecraft.client.renderer;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Vector3f;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class LightTexture implements AutoCloseable {
	public static final int FULL_BRIGHT = 15728880;
	public static final int FULL_SKY = 15728640;
	public static final int FULL_BLOCK = 240;
	private static final int TEXTURE_SIZE = 16;
	private static final Logger LOGGER = LogUtils.getLogger();
	@Nullable
	private ShaderInstance shader;
	private final TextureTarget target;
	private boolean updateLightTexture;
	private float blockLightRedFlicker;
	private final GameRenderer renderer;
	private final Minecraft minecraft;

	public LightTexture(GameRenderer gameRenderer, Minecraft minecraft) {
		this.renderer = gameRenderer;
		this.minecraft = minecraft;
		this.target = new TextureTarget(16, 16, false);
		this.target.setFilterMode(9729);
		this.target.setClearColor(1.0F, 1.0F, 1.0F, 1.0F);
		this.target.clear();
	}

	public void loadShader(ResourceProvider resourceProvider) {
		if (this.shader != null) {
			this.shader.close();
		}

		try {
			this.shader = new ShaderInstance(resourceProvider, "lightmap", DefaultVertexFormat.BLIT_SCREEN);
		} catch (IOException var3) {
			LOGGER.error("Failed to load lightmap shader", (Throwable)var3);
			this.shader = null;
		}
	}

	public void close() {
		if (this.shader != null) {
			this.shader.close();
			this.shader = null;
		}

		this.target.destroyBuffers();
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
		RenderSystem.setShaderTexture(2, this.target.getColorTextureId());
	}

	private float getDarknessGamma(float f) {
		MobEffectInstance mobEffectInstance = this.minecraft.player.getEffect(MobEffects.DARKNESS);
		return mobEffectInstance != null ? mobEffectInstance.getBlendFactor(this.minecraft.player, f) : 0.0F;
	}

	private float calculateDarknessScale(LivingEntity livingEntity, float f, float g) {
		float h = 0.45F * f;
		return Math.max(0.0F, Mth.cos(((float)livingEntity.tickCount - g) * (float) Math.PI * 0.025F) * h);
	}

	public void updateLightTexture(float f) {
		if (this.updateLightTexture && this.shader != null) {
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
				float o = clientLevel.dimensionType().ambientLight();
				boolean bl = clientLevel.effects().forceBrightLightmap();
				float p = this.minecraft.options.gamma().get().floatValue();
				this.shader.safeGetUniform("AmbientLightFactor").set(o);
				this.shader.safeGetUniform("SkyFactor").set(h);
				this.shader.safeGetUniform("BlockFactor").set(n);
				this.shader.safeGetUniform("UseBrightLightmap").set(bl ? 1 : 0);
				this.shader.safeGetUniform("SkyLightColor").set(vector3f);
				this.shader.safeGetUniform("NightVisionFactor").set(m);
				this.shader.safeGetUniform("DarknessScale").set(k);
				this.shader.safeGetUniform("DarkenWorldFactor").set(this.renderer.getDarkenWorldAmount(f));
				this.shader.safeGetUniform("BrightnessFactor").set(Math.max(0.0F, p - j));
				this.shader.apply();
				this.target.bindWrite(true);
				BufferBuilder bufferBuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
				bufferBuilder.addVertex(0.0F, 0.0F, 0.0F);
				bufferBuilder.addVertex(1.0F, 0.0F, 0.0F);
				bufferBuilder.addVertex(1.0F, 1.0F, 0.0F);
				bufferBuilder.addVertex(0.0F, 1.0F, 0.0F);
				BufferUploader.draw(bufferBuilder.buildOrThrow());
				this.shader.clear();
				this.target.unbindWrite();
				this.minecraft.getProfiler().pop();
			}
		}
	}

	public static float getBrightness(DimensionType dimensionType, int i) {
		return getBrightness(dimensionType.ambientLight(), i);
	}

	public static float getBrightness(float f, int i) {
		float g = (float)i / 15.0F;
		float h = g / (4.0F - 3.0F * g);
		return Mth.lerp(f, h, 1.0F);
	}

	public static int pack(int i, int j) {
		return i << 4 | j << 20;
	}

	public static int block(int i) {
		return i >>> 4 & 15;
	}

	public static int sky(int i) {
		return i >>> 20 & 15;
	}

	public static int lightCoordsWithEmission(int i, int j) {
		int k = Math.max(sky(i), j);
		int l = Math.max(block(i), j);
		return pack(l, k);
	}
}

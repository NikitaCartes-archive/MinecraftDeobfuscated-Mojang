package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class LogoOverlay extends Overlay {
	private static final ResourceLocation LOGO_TEXTURE = new ResourceLocation("textures/gui/mojang_logo.png");
	private static final ResourceLocation TEXT_TEXTURE = new ResourceLocation("textures/gui/mojang_text.png");
	private LogoOverlay.Stage stage = LogoOverlay.Stage.INIT;
	private long textFadeInStart = -1L;
	private float logoFlyInProgress;
	private long lastTick;
	private SoundInstance logoSound;
	private final Minecraft minecraft;
	private final ReloadInstance reload;
	private final Consumer<Optional<Throwable>> onFinish;
	private float currentProgress;
	private final LogoOverlay.Effect effect;

	public LogoOverlay(Minecraft minecraft, ReloadInstance reloadInstance, Consumer<Optional<Throwable>> consumer) {
		this.minecraft = minecraft;
		this.reload = reloadInstance;
		this.onFinish = consumer;
		LogoOverlay.Effect[] effects = LogoOverlay.Effect.values();
		this.effect = effects[(int)(System.currentTimeMillis() % (long)effects.length)];
	}

	@Override
	public void render(int i, int j, float f) {
		int k = this.minecraft.getWindow().getGuiScaledWidth();
		int l = this.minecraft.getWindow().getGuiScaledHeight();
		RenderSystem.enableTexture();
		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.disableAlphaTest();
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.clear(16640, Minecraft.ON_OSX);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		long m = Util.getMillis();
		long n = m - this.lastTick;
		this.lastTick = m;
		if (this.stage == LogoOverlay.Stage.INIT) {
			this.stage = LogoOverlay.Stage.FLY;
			this.logoFlyInProgress = 10.0F;
			this.textFadeInStart = -1L;
		} else {
			if (this.stage == LogoOverlay.Stage.FLY) {
				this.logoFlyInProgress -= (float)n / 500.0F;
				if (this.logoFlyInProgress <= 0.0F) {
					this.stage = LogoOverlay.Stage.WAIT_FOR_LOAD;
				}
			} else if (this.stage == LogoOverlay.Stage.WAIT_FOR_LOAD) {
				if (this.reload.isDone()) {
					this.textFadeInStart = m;
					this.logoSound = new SimpleSoundInstance(
						SoundEvents.AWESOME_INTRO.getLocation(), SoundSource.MASTER, 0.25F, 1.0F, false, 0, SoundInstance.Attenuation.NONE, 0.0F, 0.0F, 0.0F, true
					);
					this.minecraft.getSoundManager().play(this.logoSound);
					this.stage = LogoOverlay.Stage.TEXT;
				}
			} else if (!this.minecraft.getSoundManager().isActive(this.logoSound)) {
				this.minecraft.setOverlay(null);

				try {
					this.reload.checkExceptions();
					this.onFinish.accept(Optional.empty());
				} catch (Throwable var16) {
					this.onFinish.accept(Optional.of(var16));
				}

				if (this.minecraft.screen != null) {
					this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
				}

				this.stage = LogoOverlay.Stage.INIT;
			}

			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
			int o = Mth.clamp((int)(m - this.textFadeInStart), 0, 255);
			if (this.textFadeInStart != -1L) {
				this.minecraft.getTextureManager().bind(TEXT_TEXTURE);
				this.blit(bufferBuilder, k / 2, l - l / 8, 208, 38, o);
			}

			tesselator.end();
			RenderSystem.pushMatrix();
			RenderSystem.translatef((float)k / 2.0F, (float)l / 2.0F, 0.0F);
			switch (this.effect) {
				case CLASSIC: {
					float g = 20.0F * this.logoFlyInProgress;
					float h = 100.0F * Mth.sin(this.logoFlyInProgress);
					RenderSystem.rotatef(g, 0.0F, 0.0F, 1.0F);
					RenderSystem.translatef(h, 0.0F, 0.0F);
					float p = 1.0F / (2.0F * this.logoFlyInProgress + 1.0F);
					RenderSystem.rotatef(1.5F * this.logoFlyInProgress, 0.0F, 0.0F, 1.0F);
					RenderSystem.scalef(p, p, 1.0F);
					break;
				}
				case SPRING: {
					float g = 40.0F * ((float)Math.exp((double)(this.logoFlyInProgress / 3.0F)) - 1.0F) * Mth.sin(this.logoFlyInProgress);
					RenderSystem.translatef(g, 0.0F, 0.0F);
					break;
				}
				case SLOWDOWN: {
					float g = (float)Math.exp((double)this.logoFlyInProgress) - 1.0F;
					RenderSystem.rotatef(g, 1.0F, 0.0F, 0.0F);
					break;
				}
				case REVERSE: {
					float g = Mth.cos(this.logoFlyInProgress / 10.0F * (float) Math.PI);
					RenderSystem.scalef(g, g, 1.0F);
					break;
				}
				case GROW: {
					float g = (1.0F - this.logoFlyInProgress / 10.0F) * 0.75F;
					float h = 2.0F * Mth.sin(g * (float) Math.PI);
					RenderSystem.scalef(h, h, 1.0F);
				}
			}

			this.minecraft.getTextureManager().bind(LOGO_TEXTURE);
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
			this.blit(bufferBuilder, 0, 0, 78, 76, 255);
			tesselator.end();
			float g = this.reload.getActualProgress();
			this.currentProgress = Mth.clamp(this.currentProgress * 0.99F + g * 0.00999999F, 0.0F, 1.0F);
			this.drawProgressBar(-39, 38, 39, 48, this.currentProgress != 1.0F ? 1.0F : 0.0F);
			RenderSystem.popMatrix();
		}
	}

	private void blit(BufferBuilder bufferBuilder, int i, int j, int k, int l, int m) {
		int n = k / 2;
		int o = l / 2;
		bufferBuilder.vertex((double)(i - n), (double)(j + o), 0.0).uv(0.0F, 1.0F).color(255, 255, 255, m).endVertex();
		bufferBuilder.vertex((double)(i + n), (double)(j + o), 0.0).uv(1.0F, 1.0F).color(255, 255, 255, m).endVertex();
		bufferBuilder.vertex((double)(i + n), (double)(j - o), 0.0).uv(1.0F, 0.0F).color(255, 255, 255, m).endVertex();
		bufferBuilder.vertex((double)(i - n), (double)(j - o), 0.0).uv(0.0F, 0.0F).color(255, 255, 255, m).endVertex();
	}

	private void drawProgressBar(int i, int j, int k, int l, float f) {
		int m = Mth.ceil((float)(k - i - 1) * this.currentProgress);
		fill(i - 1, j - 1, k + 1, l + 1, 0xFF000000 | Math.round((1.0F - f) * 255.0F) << 16 | Math.round((1.0F - f) * 255.0F) << 8 | Math.round((1.0F - f) * 255.0F));
		fill(i, j, k, l, -1);
		fill(
			i + 1,
			j + 1,
			i + m,
			l - 1,
			0xFF000000 | (int)Mth.lerp(1.0F - f, 226.0F, 255.0F) << 16 | (int)Mth.lerp(1.0F - f, 40.0F, 255.0F) << 8 | (int)Mth.lerp(1.0F - f, 55.0F, 255.0F)
		);
	}

	@Environment(EnvType.CLIENT)
	static enum Effect {
		CLASSIC,
		SPRING,
		SLOWDOWN,
		REVERSE,
		GROW;
	}

	@Environment(EnvType.CLIENT)
	static enum Stage {
		INIT,
		FLY,
		WAIT_FOR_LOAD,
		TEXT;
	}
}

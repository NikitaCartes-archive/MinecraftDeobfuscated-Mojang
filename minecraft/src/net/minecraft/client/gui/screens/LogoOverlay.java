package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class LogoOverlay extends Overlay {
	private static final int TEXT_WIDTH = 208;
	private static final int TEXT_HEIGHT = 38;
	private static final int LOGO_WIDTH = 39;
	private static final int LOGO_HEIGHT = 38;
	private static final ResourceLocation LOGO_TEXTURE = new ResourceLocation("textures/gui/mojang_logo.png");
	private static final ResourceLocation TEXT_TEXTURE = new ResourceLocation("textures/gui/mojang_text.png");
	private static final float SMOOTHING = 0.99F;
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
	public void render(PoseStack poseStack, int i, int j, float f) {
		int k = this.minecraft.getWindow().getGuiScaledWidth();
		int l = this.minecraft.getWindow().getGuiScaledHeight();
		RenderSystem.enableBlend();
		RenderSystem.blendEquation(32774);
		RenderSystem.blendFunc(770, 1);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
		RenderSystem.clear(16640, Minecraft.ON_OSX);
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
						SoundEvents.AWESOME_INTRO.getLocation(), SoundSource.MASTER, 0.25F, 1.0F, false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true
					);
					this.minecraft.getSoundManager().play(this.logoSound);
					this.stage = LogoOverlay.Stage.TEXT;
				}
			} else if (!this.minecraft.getSoundManager().isActive(this.logoSound)) {
				this.minecraft.setOverlay(null);

				try {
					this.reload.checkExceptions();
					this.onFinish.accept(Optional.empty());
				} catch (Throwable var17) {
					this.onFinish.accept(Optional.of(var17));
				}

				if (this.minecraft.screen != null) {
					this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
				}

				this.stage = LogoOverlay.Stage.INIT;
			}

			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			int o = Mth.clamp((int)(m - this.textFadeInStart), 0, 255);
			if (this.textFadeInStart != -1L) {
				RenderSystem.setShaderTexture(0, TEXT_TEXTURE);
				this.blit(poseStack, bufferBuilder, k / 2, l - l / 8, 208, 38, o);
			}

			tesselator.end();
			poseStack.pushPose();
			poseStack.translate((double)((float)k / 2.0F), (double)((float)l / 2.0F), 0.0);
			switch (this.effect) {
				case CLASSIC: {
					float g = 20.0F * this.logoFlyInProgress;
					float h = 100.0F * Mth.sin(this.logoFlyInProgress);
					poseStack.mulPose(Vector3f.ZP.rotationDegrees(g));
					poseStack.translate((double)h, 0.0, 0.0);
					float p = 1.0F / (2.0F * this.logoFlyInProgress + 1.0F);
					poseStack.mulPose(Vector3f.ZP.rotationDegrees(1.5F * this.logoFlyInProgress));
					poseStack.scale(p, p, 1.0F);
					break;
				}
				case SPRING: {
					float g = 40.0F * ((float)Math.exp((double)(this.logoFlyInProgress / 3.0F)) - 1.0F) * Mth.sin(this.logoFlyInProgress);
					poseStack.translate((double)g, 0.0, 0.0);
					break;
				}
				case SLOWDOWN: {
					float g = (float)Math.exp((double)this.logoFlyInProgress) - 1.0F;
					poseStack.mulPose(Vector3f.XP.rotationDegrees(g));
					break;
				}
				case REVERSE: {
					float g = Mth.cos(this.logoFlyInProgress / 10.0F * (float) Math.PI);
					poseStack.scale(g, g, 1.0F);
					break;
				}
				case GROW: {
					float g = (1.0F - this.logoFlyInProgress / 10.0F) * 0.75F;
					float h = 2.0F * Mth.sin(g * (float) Math.PI);
					poseStack.scale(h, h, 1.0F);
				}
			}

			RenderSystem.setShaderTexture(0, LOGO_TEXTURE);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			this.blit(poseStack, bufferBuilder, 0, 0, 78, 76, 255);
			tesselator.end();
			float g = this.reload.getActualProgress();
			this.currentProgress = Mth.clamp(this.currentProgress * 0.99F + g * 0.00999999F, 0.0F, 1.0F);
			this.drawProgressBar(poseStack, -39, 38, 39, 48, this.currentProgress != 1.0F ? 1.0F : 0.0F);
			poseStack.popPose();
		}
	}

	private void blit(PoseStack poseStack, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m) {
		int n = k / 2;
		int o = l / 2;
		Matrix4f matrix4f = poseStack.last().pose();
		bufferBuilder.vertex(matrix4f, (float)(i - n), (float)(j + o), 0.0F).uv(0.0F, 1.0F).color(255, 255, 255, m).endVertex();
		bufferBuilder.vertex(matrix4f, (float)(i + n), (float)(j + o), 0.0F).uv(1.0F, 1.0F).color(255, 255, 255, m).endVertex();
		bufferBuilder.vertex(matrix4f, (float)(i + n), (float)(j - o), 0.0F).uv(1.0F, 0.0F).color(255, 255, 255, m).endVertex();
		bufferBuilder.vertex(matrix4f, (float)(i - n), (float)(j - o), 0.0F).uv(0.0F, 0.0F).color(255, 255, 255, m).endVertex();
	}

	private void drawProgressBar(PoseStack poseStack, int i, int j, int k, int l, float f) {
		int m = Mth.ceil((float)(k - i - 2) * this.currentProgress);
		int n = Math.round(f * 255.0F);
		int o = FastColor.ARGB32.color(n, 255, 255, 255);
		fill(poseStack, i + 2, j + 2, i + m, l - 2, o);
		fill(poseStack, i + 1, j, k - 1, j + 1, o);
		fill(poseStack, i + 1, l, k - 1, l - 1, o);
		fill(poseStack, i, j, i + 1, l, o);
		fill(poseStack, k, j, k - 1, l, o);
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

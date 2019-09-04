package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPack;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class LoadingOverlay extends Overlay {
	private static final ResourceLocation MOJANG_LOGO_LOCATION = new ResourceLocation("textures/gui/title/mojang.png");
	private final Minecraft minecraft;
	private final ReloadInstance reload;
	private final Runnable onFinish;
	private final boolean fadeIn;
	private float currentProgress;
	private long fadeOutStart = -1L;
	private long fadeInStart = -1L;

	public LoadingOverlay(Minecraft minecraft, ReloadInstance reloadInstance, Runnable runnable, boolean bl) {
		this.minecraft = minecraft;
		this.reload = reloadInstance;
		this.onFinish = runnable;
		this.fadeIn = bl;
	}

	public static void registerTextures(Minecraft minecraft) {
		minecraft.getTextureManager().register(MOJANG_LOGO_LOCATION, new LoadingOverlay.LogoTexture());
	}

	@Override
	public void render(int i, int j, float f) {
		int k = this.minecraft.window.getGuiScaledWidth();
		int l = this.minecraft.window.getGuiScaledHeight();
		long m = Util.getMillis();
		if (this.fadeIn && (this.reload.isApplying() || this.minecraft.screen != null) && this.fadeInStart == -1L) {
			this.fadeInStart = m;
		}

		float g = this.fadeOutStart > -1L ? (float)(m - this.fadeOutStart) / 1000.0F : -1.0F;
		float h = this.fadeInStart > -1L ? (float)(m - this.fadeInStart) / 500.0F : -1.0F;
		float o;
		if (g >= 1.0F) {
			if (this.minecraft.screen != null) {
				this.minecraft.screen.render(0, 0, f);
			}

			int n = Mth.ceil((1.0F - Mth.clamp(g - 1.0F, 0.0F, 1.0F)) * 255.0F);
			fill(0, 0, k, l, 16777215 | n << 24);
			o = 1.0F - Mth.clamp(g - 1.0F, 0.0F, 1.0F);
		} else if (this.fadeIn) {
			if (this.minecraft.screen != null && h < 1.0F) {
				this.minecraft.screen.render(i, j, f);
			}

			int n = Mth.ceil(Mth.clamp((double)h, 0.15, 1.0) * 255.0);
			fill(0, 0, k, l, 16777215 | n << 24);
			o = Mth.clamp(h, 0.0F, 1.0F);
		} else {
			fill(0, 0, k, l, -1);
			o = 1.0F;
		}

		int n = (this.minecraft.window.getGuiScaledWidth() - 256) / 2;
		int p = (this.minecraft.window.getGuiScaledHeight() - 256) / 2;
		this.minecraft.getTextureManager().bind(MOJANG_LOGO_LOCATION);
		RenderSystem.enableBlend();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, o);
		this.blit(n, p, 0, 0, 256, 256);
		float q = this.reload.getActualProgress();
		this.currentProgress = this.currentProgress * 0.95F + q * 0.050000012F;
		if (g < 1.0F) {
			this.drawProgressBar(k / 2 - 150, l / 4 * 3, k / 2 + 150, l / 4 * 3 + 10, this.currentProgress, 1.0F - Mth.clamp(g, 0.0F, 1.0F));
		}

		if (g >= 2.0F) {
			this.minecraft.setOverlay(null);
		}

		if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || h >= 2.0F)) {
			this.reload.checkExceptions();
			this.fadeOutStart = Util.getMillis();
			this.onFinish.run();
			if (this.minecraft.screen != null) {
				this.minecraft.screen.init(this.minecraft, this.minecraft.window.getGuiScaledWidth(), this.minecraft.window.getGuiScaledHeight());
			}
		}
	}

	private void drawProgressBar(int i, int j, int k, int l, float f, float g) {
		int m = Mth.ceil((float)(k - i - 2) * f);
		fill(i - 1, j - 1, k + 1, l + 1, 0xFF000000 | Math.round((1.0F - g) * 255.0F) << 16 | Math.round((1.0F - g) * 255.0F) << 8 | Math.round((1.0F - g) * 255.0F));
		fill(i, j, k, l, -1);
		fill(
			i + 1,
			j + 1,
			i + m,
			l - 1,
			0xFF000000 | (int)Mth.lerp(1.0F - g, 226.0F, 255.0F) << 16 | (int)Mth.lerp(1.0F - g, 40.0F, 255.0F) << 8 | (int)Mth.lerp(1.0F - g, 55.0F, 255.0F)
		);
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	static class LogoTexture extends SimpleTexture {
		public LogoTexture() {
			super(LoadingOverlay.MOJANG_LOGO_LOCATION);
		}

		@Override
		protected SimpleTexture.TextureImage getTextureImage(ResourceManager resourceManager) {
			Minecraft minecraft = Minecraft.getInstance();
			VanillaPack vanillaPack = minecraft.getClientPackSource().getVanillaPack();

			try {
				InputStream inputStream = vanillaPack.getResource(PackType.CLIENT_RESOURCES, LoadingOverlay.MOJANG_LOGO_LOCATION);
				Throwable var5 = null;

				SimpleTexture.TextureImage var6;
				try {
					var6 = new SimpleTexture.TextureImage(null, NativeImage.read(inputStream));
				} catch (Throwable var16) {
					var5 = var16;
					throw var16;
				} finally {
					if (inputStream != null) {
						if (var5 != null) {
							try {
								inputStream.close();
							} catch (Throwable var15) {
								var5.addSuppressed(var15);
							}
						} else {
							inputStream.close();
						}
					}
				}

				return var6;
			} catch (IOException var18) {
				return new SimpleTexture.TextureImage(var18);
			}
		}
	}
}

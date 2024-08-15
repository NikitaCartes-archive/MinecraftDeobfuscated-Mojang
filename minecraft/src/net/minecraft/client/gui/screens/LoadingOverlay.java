package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class LoadingOverlay extends Overlay {
	public static final ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/title/mojangstudios.png");
	private static final int LOGO_BACKGROUND_COLOR = ARGB.color(255, 239, 50, 61);
	private static final int LOGO_BACKGROUND_COLOR_DARK = ARGB.color(255, 0, 0, 0);
	private static final IntSupplier BRAND_BACKGROUND = () -> Minecraft.getInstance().options.darkMojangStudiosBackground().get()
			? LOGO_BACKGROUND_COLOR_DARK
			: LOGO_BACKGROUND_COLOR;
	private static final int LOGO_SCALE = 240;
	private static final float LOGO_QUARTER_FLOAT = 60.0F;
	private static final int LOGO_QUARTER = 60;
	private static final int LOGO_HALF = 120;
	private static final float LOGO_OVERLAP = 0.0625F;
	private static final float SMOOTHING = 0.95F;
	public static final long FADE_OUT_TIME = 1000L;
	public static final long FADE_IN_TIME = 500L;
	private final Minecraft minecraft;
	private final ReloadInstance reload;
	private final Consumer<Optional<Throwable>> onFinish;
	private final boolean fadeIn;
	private float currentProgress;
	private long fadeOutStart = -1L;
	private long fadeInStart = -1L;

	public LoadingOverlay(Minecraft minecraft, ReloadInstance reloadInstance, Consumer<Optional<Throwable>> consumer, boolean bl) {
		this.minecraft = minecraft;
		this.reload = reloadInstance;
		this.onFinish = consumer;
		this.fadeIn = bl;
	}

	public static void registerTextures(Minecraft minecraft) {
		minecraft.getTextureManager().register(MOJANG_STUDIOS_LOGO_LOCATION, new LoadingOverlay.LogoTexture());
	}

	private static int replaceAlpha(int i, int j) {
		return i & 16777215 | j << 24;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		int k = guiGraphics.guiWidth();
		int l = guiGraphics.guiHeight();
		long m = Util.getMillis();
		if (this.fadeIn && this.fadeInStart == -1L) {
			this.fadeInStart = m;
		}

		float g = this.fadeOutStart > -1L ? (float)(m - this.fadeOutStart) / 1000.0F : -1.0F;
		float h = this.fadeInStart > -1L ? (float)(m - this.fadeInStart) / 500.0F : -1.0F;
		float o;
		if (g >= 1.0F) {
			if (this.minecraft.screen != null) {
				this.minecraft.screen.render(guiGraphics, 0, 0, f);
			}

			int n = Mth.ceil((1.0F - Mth.clamp(g - 1.0F, 0.0F, 1.0F)) * 255.0F);
			guiGraphics.fill(RenderType.guiOverlay(), 0, 0, k, l, replaceAlpha(BRAND_BACKGROUND.getAsInt(), n));
			o = 1.0F - Mth.clamp(g - 1.0F, 0.0F, 1.0F);
		} else if (this.fadeIn) {
			if (this.minecraft.screen != null && h < 1.0F) {
				this.minecraft.screen.render(guiGraphics, i, j, f);
			}

			int n = Mth.ceil(Mth.clamp((double)h, 0.15, 1.0) * 255.0);
			guiGraphics.fill(RenderType.guiOverlay(), 0, 0, k, l, replaceAlpha(BRAND_BACKGROUND.getAsInt(), n));
			o = Mth.clamp(h, 0.0F, 1.0F);
		} else {
			int n = BRAND_BACKGROUND.getAsInt();
			float p = (float)(n >> 16 & 0xFF) / 255.0F;
			float q = (float)(n >> 8 & 0xFF) / 255.0F;
			float r = (float)(n & 0xFF) / 255.0F;
			GlStateManager._clearColor(p, q, r, 1.0F);
			GlStateManager._clear(16384);
			o = 1.0F;
		}

		int n = (int)((double)guiGraphics.guiWidth() * 0.5);
		int s = (int)((double)guiGraphics.guiHeight() * 0.5);
		double d = Math.min((double)guiGraphics.guiWidth() * 0.75, (double)guiGraphics.guiHeight()) * 0.25;
		int t = (int)(d * 0.5);
		double e = d * 4.0;
		int u = (int)(e * 0.5);
		int v = ARGB.white(o);
		guiGraphics.blit(resourceLocation -> RenderType.mojangLogo(), MOJANG_STUDIOS_LOGO_LOCATION, n - u, s - t, -0.0625F, 0.0F, u, (int)d, 120, 60, 120, 120, v);
		guiGraphics.blit(resourceLocation -> RenderType.mojangLogo(), MOJANG_STUDIOS_LOGO_LOCATION, n, s - t, 0.0625F, 60.0F, u, (int)d, 120, 60, 120, 120, v);
		int w = (int)((double)guiGraphics.guiHeight() * 0.8325);
		float x = this.reload.getActualProgress();
		this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + x * 0.050000012F, 0.0F, 1.0F);
		if (g < 1.0F) {
			this.drawProgressBar(guiGraphics, k / 2 - u, w - 5, k / 2 + u, w + 5, 1.0F - Mth.clamp(g, 0.0F, 1.0F));
		}

		if (g >= 2.0F) {
			this.minecraft.setOverlay(null);
		}

		if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || h >= 2.0F)) {
			try {
				this.reload.checkExceptions();
				this.onFinish.accept(Optional.empty());
			} catch (Throwable var24) {
				this.onFinish.accept(Optional.of(var24));
			}

			this.fadeOutStart = Util.getMillis();
			if (this.minecraft.screen != null) {
				this.minecraft.screen.init(this.minecraft, guiGraphics.guiWidth(), guiGraphics.guiHeight());
			}
		}
	}

	private void drawProgressBar(GuiGraphics guiGraphics, int i, int j, int k, int l, float f) {
		int m = Mth.ceil((float)(k - i - 2) * this.currentProgress);
		int n = Math.round(f * 255.0F);
		int o = ARGB.color(n, 255, 255, 255);
		guiGraphics.fill(i + 2, j + 2, i + m, l - 2, o);
		guiGraphics.fill(i + 1, j, k - 1, j + 1, o);
		guiGraphics.fill(i + 1, l, k - 1, l - 1, o);
		guiGraphics.fill(i, j, i + 1, l, o);
		guiGraphics.fill(k, j, k - 1, l, o);
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	static class LogoTexture extends SimpleTexture {
		public LogoTexture() {
			super(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION);
		}

		@Override
		protected SimpleTexture.TextureImage getTextureImage(ResourceManager resourceManager) {
			VanillaPackResources vanillaPackResources = Minecraft.getInstance().getVanillaPackResources();
			IoSupplier<InputStream> ioSupplier = vanillaPackResources.getResource(PackType.CLIENT_RESOURCES, LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION);
			if (ioSupplier == null) {
				return new SimpleTexture.TextureImage(new FileNotFoundException(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION.toString()));
			} else {
				try {
					InputStream inputStream = ioSupplier.get();

					SimpleTexture.TextureImage var5;
					try {
						var5 = new SimpleTexture.TextureImage(new TextureMetadataSection(true, true), NativeImage.read(inputStream));
					} catch (Throwable var8) {
						if (inputStream != null) {
							try {
								inputStream.close();
							} catch (Throwable var7) {
								var8.addSuppressed(var7);
							}
						}

						throw var8;
					}

					if (inputStream != null) {
						inputStream.close();
					}

					return var5;
				} catch (IOException var9) {
					return new SimpleTexture.TextureImage(var9);
				}
			}
		}
	}
}

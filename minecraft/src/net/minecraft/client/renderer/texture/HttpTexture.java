package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class HttpTexture extends SimpleTexture {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int SKIN_WIDTH = 64;
	private static final int SKIN_HEIGHT = 64;
	private static final int LEGACY_SKIN_HEIGHT = 32;
	@Nullable
	private final File file;
	private final String urlString;
	private final boolean processLegacySkin;
	@Nullable
	private final Runnable onDownloaded;
	@Nullable
	private CompletableFuture<?> future;
	private boolean uploaded;

	public HttpTexture(@Nullable File file, String string, ResourceLocation resourceLocation, boolean bl, @Nullable Runnable runnable) {
		super(resourceLocation);
		this.file = file;
		this.urlString = string;
		this.processLegacySkin = bl;
		this.onDownloaded = runnable;
	}

	private void loadCallback(NativeImage nativeImage) {
		if (this.onDownloaded != null) {
			this.onDownloaded.run();
		}

		Minecraft.getInstance().execute(() -> {
			this.uploaded = true;
			if (!RenderSystem.isOnRenderThread()) {
				RenderSystem.recordRenderCall(() -> this.upload(nativeImage));
			} else {
				this.upload(nativeImage);
			}
		});
	}

	private void upload(NativeImage nativeImage) {
		TextureUtil.prepareImage(this.getId(), nativeImage.getWidth(), nativeImage.getHeight());
		nativeImage.upload(0, 0, 0, true);
	}

	@Override
	public void load(ResourceManager resourceManager) throws IOException {
		Minecraft.getInstance().execute(() -> {
			if (!this.uploaded) {
				try {
					super.load(resourceManager);
				} catch (IOException var3x) {
					LOGGER.warn("Failed to load texture: {}", this.location, var3x);
				}

				this.uploaded = true;
			}
		});
		if (this.future == null) {
			NativeImage nativeImage;
			if (this.file != null && this.file.isFile()) {
				LOGGER.debug("Loading http texture from local cache ({})", this.file);
				FileInputStream fileInputStream = new FileInputStream(this.file);
				nativeImage = this.load(fileInputStream);
			} else {
				nativeImage = null;
			}

			if (nativeImage != null) {
				this.loadCallback(nativeImage);
			} else {
				this.future = CompletableFuture.runAsync(() -> {
					HttpURLConnection httpURLConnection = null;
					LOGGER.debug("Downloading http texture from {} to {}", this.urlString, this.file);

					try {
						httpURLConnection = (HttpURLConnection)new URL(this.urlString).openConnection(Minecraft.getInstance().getProxy());
						httpURLConnection.setDoInput(true);
						httpURLConnection.setDoOutput(false);
						httpURLConnection.connect();
						if (httpURLConnection.getResponseCode() / 100 == 2) {
							InputStream inputStream;
							if (this.file != null) {
								FileUtils.copyInputStreamToFile(httpURLConnection.getInputStream(), this.file);
								inputStream = new FileInputStream(this.file);
							} else {
								inputStream = httpURLConnection.getInputStream();
							}

							Minecraft.getInstance().execute(() -> {
								NativeImage nativeImagex = this.load(inputStream);
								if (nativeImagex != null) {
									this.loadCallback(nativeImagex);
								}
							});
							return;
						}
					} catch (Exception var6) {
						LOGGER.error("Couldn't download http texture", (Throwable)var6);
						return;
					} finally {
						if (httpURLConnection != null) {
							httpURLConnection.disconnect();
						}
					}
				}, Util.backgroundExecutor().forName("downloadTexture"));
			}
		}
	}

	@Nullable
	private NativeImage load(InputStream inputStream) {
		NativeImage nativeImage = null;

		try {
			nativeImage = NativeImage.read(inputStream);
			if (this.processLegacySkin) {
				nativeImage = this.processLegacySkin(nativeImage);
			}
		} catch (Exception var4) {
			LOGGER.warn("Error while loading the skin texture", (Throwable)var4);
		}

		return nativeImage;
	}

	@Nullable
	private NativeImage processLegacySkin(NativeImage nativeImage) {
		int i = nativeImage.getHeight();
		int j = nativeImage.getWidth();
		if (j == 64 && (i == 32 || i == 64)) {
			boolean bl = i == 32;
			if (bl) {
				NativeImage nativeImage2 = new NativeImage(64, 64, true);
				nativeImage2.copyFrom(nativeImage);
				nativeImage.close();
				nativeImage = nativeImage2;
				nativeImage2.fillRect(0, 32, 64, 32, 0);
				nativeImage2.copyRect(4, 16, 16, 32, 4, 4, true, false);
				nativeImage2.copyRect(8, 16, 16, 32, 4, 4, true, false);
				nativeImage2.copyRect(0, 20, 24, 32, 4, 12, true, false);
				nativeImage2.copyRect(4, 20, 16, 32, 4, 12, true, false);
				nativeImage2.copyRect(8, 20, 8, 32, 4, 12, true, false);
				nativeImage2.copyRect(12, 20, 16, 32, 4, 12, true, false);
				nativeImage2.copyRect(44, 16, -8, 32, 4, 4, true, false);
				nativeImage2.copyRect(48, 16, -8, 32, 4, 4, true, false);
				nativeImage2.copyRect(40, 20, 0, 32, 4, 12, true, false);
				nativeImage2.copyRect(44, 20, -8, 32, 4, 12, true, false);
				nativeImage2.copyRect(48, 20, -16, 32, 4, 12, true, false);
				nativeImage2.copyRect(52, 20, -8, 32, 4, 12, true, false);
			}

			setNoAlpha(nativeImage, 0, 0, 32, 16);
			if (bl) {
				doNotchTransparencyHack(nativeImage, 32, 0, 64, 32);
			}

			setNoAlpha(nativeImage, 0, 16, 64, 32);
			setNoAlpha(nativeImage, 16, 48, 48, 64);
			return nativeImage;
		} else {
			nativeImage.close();
			LOGGER.warn("Discarding incorrectly sized ({}x{}) skin texture from {}", j, i, this.urlString);
			return null;
		}
	}

	private static void doNotchTransparencyHack(NativeImage nativeImage, int i, int j, int k, int l) {
		for (int m = i; m < k; m++) {
			for (int n = j; n < l; n++) {
				int o = nativeImage.getPixel(m, n);
				if (ARGB.alpha(o) < 128) {
					return;
				}
			}
		}

		for (int m = i; m < k; m++) {
			for (int nx = j; nx < l; nx++) {
				nativeImage.setPixel(m, nx, nativeImage.getPixel(m, nx) & 16777215);
			}
		}
	}

	private static void setNoAlpha(NativeImage nativeImage, int i, int j, int k, int l) {
		for (int m = i; m < k; m++) {
			for (int n = j; n < l; n++) {
				nativeImage.setPixel(m, n, ARGB.opaque(nativeImage.getPixel(m, n)));
			}
		}
	}
}

package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.HttpTextureProcessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class HttpTexture extends SimpleTexture {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
	@Nullable
	private final File file;
	private final String urlString;
	@Nullable
	private final HttpTextureProcessor processor;
	@Nullable
	private Thread thread;
	private volatile boolean uploaded;

	public HttpTexture(@Nullable File file, String string, ResourceLocation resourceLocation, @Nullable HttpTextureProcessor httpTextureProcessor) {
		super(resourceLocation);
		this.file = file;
		this.urlString = string;
		this.processor = httpTextureProcessor;
	}

	private void uploadImage(NativeImage nativeImage) {
		TextureUtil.prepareImage(this.getId(), nativeImage.getWidth(), nativeImage.getHeight());
		nativeImage.upload(0, 0, 0, false);
	}

	public void loadCallback(NativeImage nativeImage) {
		if (this.processor != null) {
			this.processor.onTextureDownloaded();
		}

		synchronized (this) {
			this.uploadImage(nativeImage);
			this.uploaded = true;
		}
	}

	@Override
	public void load(ResourceManager resourceManager) throws IOException {
		if (!this.uploaded) {
			synchronized (this) {
				super.load(resourceManager);
				this.uploaded = true;
			}
		}

		if (this.thread == null) {
			if (this.file != null && this.file.isFile()) {
				LOGGER.debug("Loading http texture from local cache ({})", this.file);
				NativeImage nativeImage = null;

				try {
					try {
						nativeImage = NativeImage.read(new FileInputStream(this.file));
						if (this.processor != null) {
							nativeImage = this.processor.process(nativeImage);
						}

						this.loadCallback(nativeImage);
					} catch (IOException var8) {
						LOGGER.error("Couldn't load skin {}", this.file, var8);
						this.startDownloadThread();
					}
				} finally {
					if (nativeImage != null) {
						nativeImage.close();
					}
				}
			} else {
				this.startDownloadThread();
			}
		}
	}

	protected void startDownloadThread() {
		this.thread = new Thread("Texture Downloader #" + UNIQUE_THREAD_ID.incrementAndGet()) {
			public void run() {
				HttpURLConnection httpURLConnection = null;
				HttpTexture.LOGGER.debug("Downloading http texture from {} to {}", HttpTexture.this.urlString, HttpTexture.this.file);

				try {
					httpURLConnection = (HttpURLConnection)new URL(HttpTexture.this.urlString).openConnection(Minecraft.getInstance().getProxy());
					httpURLConnection.setDoInput(true);
					httpURLConnection.setDoOutput(false);
					httpURLConnection.connect();
					if (httpURLConnection.getResponseCode() / 100 == 2) {
						InputStream inputStream;
						if (HttpTexture.this.file != null) {
							FileUtils.copyInputStreamToFile(httpURLConnection.getInputStream(), HttpTexture.this.file);
							inputStream = new FileInputStream(HttpTexture.this.file);
						} else {
							inputStream = httpURLConnection.getInputStream();
						}

						Minecraft.getInstance().execute(() -> {
							NativeImage nativeImage = null;

							try {
								nativeImage = NativeImage.read(inputStream);
								if (HttpTexture.this.processor != null) {
									nativeImage = HttpTexture.this.processor.process(nativeImage);
								}

								HttpTexture.this.loadCallback(nativeImage);
							} catch (IOException var7x) {
								HttpTexture.LOGGER.warn("Error while loading the skin texture", (Throwable)var7x);
							} finally {
								if (nativeImage != null) {
									nativeImage.close();
								}

								IOUtils.closeQuietly(inputStream);
							}
						});
						return;
					}
				} catch (Exception var6) {
					HttpTexture.LOGGER.error("Couldn't download http texture", (Throwable)var6);
					return;
				} finally {
					if (httpURLConnection != null) {
						httpURLConnection.disconnect();
					}
				}
			}
		};
		this.thread.setDaemon(true);
		this.thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
		this.thread.start();
	}
}

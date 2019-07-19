/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.HttpTextureProcessor;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class HttpTexture
extends SimpleTexture {
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void loadCallback(NativeImage nativeImage) {
        if (this.processor != null) {
            this.processor.onTextureDownloaded();
        }
        HttpTexture httpTexture = this;
        synchronized (httpTexture) {
            this.uploadImage(nativeImage);
            this.uploaded = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void load(ResourceManager resourceManager) throws IOException {
        if (!this.uploaded) {
            HttpTexture httpTexture = this;
            synchronized (httpTexture) {
                super.load(resourceManager);
                this.uploaded = true;
            }
        }
        if (this.thread == null) {
            if (this.file != null && this.file.isFile()) {
                LOGGER.debug("Loading http texture from local cache ({})", (Object)this.file);
                try (NativeImage nativeImage = null;){
                    nativeImage = NativeImage.read(new FileInputStream(this.file));
                    if (this.processor != null) {
                        nativeImage = this.processor.process(nativeImage);
                    }
                    this.loadCallback(nativeImage);
                }
            } else {
                this.startDownloadThread();
            }
        }
    }

    protected void startDownloadThread() {
        this.thread = new Thread("Texture Downloader #" + UNIQUE_THREAD_ID.incrementAndGet()){

            @Override
            public void run() {
                HttpURLConnection httpURLConnection = null;
                LOGGER.debug("Downloading http texture from {} to {}", (Object)HttpTexture.this.urlString, (Object)HttpTexture.this.file);
                try {
                    InputStream inputStream;
                    httpURLConnection = (HttpURLConnection)new URL(HttpTexture.this.urlString).openConnection(Minecraft.getInstance().getProxy());
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(false);
                    httpURLConnection.connect();
                    if (httpURLConnection.getResponseCode() / 100 != 2) {
                        return;
                    }
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
                        } catch (IOException iOException) {
                            LOGGER.warn("Error while loading the skin texture", (Throwable)iOException);
                        } finally {
                            if (nativeImage != null) {
                                nativeImage.close();
                            }
                            IOUtils.closeQuietly(inputStream);
                        }
                    });
                } catch (Exception exception) {
                    LOGGER.error("Couldn't download http texture", (Throwable)exception);
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


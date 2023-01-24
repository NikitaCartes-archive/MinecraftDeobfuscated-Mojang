package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

@Environment(EnvType.CLIENT)
public class LazyLoadedImage {
	private final ResourceLocation id;
	private final Resource resource;
	private final AtomicReference<NativeImage> image = new AtomicReference();
	private final AtomicInteger referenceCount;

	public LazyLoadedImage(ResourceLocation resourceLocation, Resource resource, int i) {
		this.id = resourceLocation;
		this.resource = resource;
		this.referenceCount = new AtomicInteger(i);
	}

	public NativeImage get() throws IOException {
		NativeImage nativeImage = (NativeImage)this.image.get();
		if (nativeImage == null) {
			synchronized (this) {
				nativeImage = (NativeImage)this.image.get();
				if (nativeImage == null) {
					try {
						InputStream inputStream = this.resource.open();

						try {
							nativeImage = NativeImage.read(inputStream);
							this.image.set(nativeImage);
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
					} catch (IOException var9) {
						throw new IOException("Failed to load image " + this.id, var9);
					}
				}
			}
		}

		return nativeImage;
	}

	public void release() {
		int i = this.referenceCount.decrementAndGet();
		if (i <= 0) {
			NativeImage nativeImage = (NativeImage)this.image.getAndSet(null);
			if (nativeImage != null) {
				nativeImage.close();
			}
		}
	}
}

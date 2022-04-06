package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SimpleTexture extends AbstractTexture {
	static final Logger LOGGER = LogUtils.getLogger();
	protected final ResourceLocation location;

	public SimpleTexture(ResourceLocation resourceLocation) {
		this.location = resourceLocation;
	}

	@Override
	public void load(ResourceManager resourceManager) throws IOException {
		SimpleTexture.TextureImage textureImage = this.getTextureImage(resourceManager);
		textureImage.throwIfError();
		TextureMetadataSection textureMetadataSection = textureImage.getTextureMetadata();
		boolean bl;
		boolean bl2;
		if (textureMetadataSection != null) {
			bl = textureMetadataSection.isBlur();
			bl2 = textureMetadataSection.isClamp();
		} else {
			bl = false;
			bl2 = false;
		}

		NativeImage nativeImage = textureImage.getImage();
		if (!RenderSystem.isOnRenderThreadOrInit()) {
			RenderSystem.recordRenderCall(() -> this.doLoad(nativeImage, bl, bl2));
		} else {
			this.doLoad(nativeImage, bl, bl2);
		}
	}

	private void doLoad(NativeImage nativeImage, boolean bl, boolean bl2) {
		TextureUtil.prepareImage(this.getId(), 0, nativeImage.getWidth(), nativeImage.getHeight());
		nativeImage.upload(0, 0, 0, 0, 0, nativeImage.getWidth(), nativeImage.getHeight(), bl, bl2, false, true);
	}

	protected SimpleTexture.TextureImage getTextureImage(ResourceManager resourceManager) {
		return SimpleTexture.TextureImage.load(resourceManager, this.location);
	}

	@Environment(EnvType.CLIENT)
	protected static class TextureImage implements Closeable {
		@Nullable
		private final TextureMetadataSection metadata;
		@Nullable
		private final NativeImage image;
		@Nullable
		private final IOException exception;

		public TextureImage(IOException iOException) {
			this.exception = iOException;
			this.metadata = null;
			this.image = null;
		}

		public TextureImage(@Nullable TextureMetadataSection textureMetadataSection, NativeImage nativeImage) {
			this.exception = null;
			this.metadata = textureMetadataSection;
			this.image = nativeImage;
		}

		public static SimpleTexture.TextureImage load(ResourceManager resourceManager, ResourceLocation resourceLocation) {
			try {
				Resource resource = resourceManager.getResourceOrThrow(resourceLocation);
				InputStream inputStream = resource.open();

				NativeImage nativeImage;
				try {
					nativeImage = NativeImage.read(inputStream);
				} catch (Throwable var9) {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (Throwable var7) {
							var9.addSuppressed(var7);
						}
					}

					throw var9;
				}

				if (inputStream != null) {
					inputStream.close();
				}

				TextureMetadataSection textureMetadataSection = null;

				try {
					textureMetadataSection = (TextureMetadataSection)resource.metadata().getSection(TextureMetadataSection.SERIALIZER).orElse(null);
				} catch (RuntimeException var8) {
					SimpleTexture.LOGGER.warn("Failed reading metadata of: {}", resourceLocation, var8);
				}

				return new SimpleTexture.TextureImage(textureMetadataSection, nativeImage);
			} catch (IOException var10) {
				return new SimpleTexture.TextureImage(var10);
			}
		}

		@Nullable
		public TextureMetadataSection getTextureMetadata() {
			return this.metadata;
		}

		public NativeImage getImage() throws IOException {
			if (this.exception != null) {
				throw this.exception;
			} else {
				return this.image;
			}
		}

		public void close() {
			if (this.image != null) {
				this.image.close();
			}
		}

		public void throwIfError() throws IOException {
			if (this.exception != null) {
				throw this.exception;
			}
		}
	}
}

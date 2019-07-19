package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class SimpleTexture extends AbstractTexture {
	private static final Logger LOGGER = LogManager.getLogger();
	protected final ResourceLocation location;

	public SimpleTexture(ResourceLocation resourceLocation) {
		this.location = resourceLocation;
	}

	@Override
	public void load(ResourceManager resourceManager) throws IOException {
		SimpleTexture.TextureImage textureImage = this.getTextureImage(resourceManager);
		Throwable var3 = null;

		try {
			boolean bl = false;
			boolean bl2 = false;
			textureImage.throwIfError();
			TextureMetadataSection textureMetadataSection = textureImage.getTextureMetadata();
			if (textureMetadataSection != null) {
				bl = textureMetadataSection.isBlur();
				bl2 = textureMetadataSection.isClamp();
			}

			this.bind();
			TextureUtil.prepareImage(this.getId(), 0, textureImage.getImage().getWidth(), textureImage.getImage().getHeight());
			textureImage.getImage().upload(0, 0, 0, 0, 0, textureImage.getImage().getWidth(), textureImage.getImage().getHeight(), bl, bl2, false);
		} catch (Throwable var14) {
			var3 = var14;
			throw var14;
		} finally {
			if (textureImage != null) {
				if (var3 != null) {
					try {
						textureImage.close();
					} catch (Throwable var13) {
						var3.addSuppressed(var13);
					}
				} else {
					textureImage.close();
				}
			}
		}
	}

	protected SimpleTexture.TextureImage getTextureImage(ResourceManager resourceManager) {
		return SimpleTexture.TextureImage.load(resourceManager, this.location);
	}

	@Environment(EnvType.CLIENT)
	public static class TextureImage implements Closeable {
		private final TextureMetadataSection metadata;
		private final NativeImage image;
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
				Resource resource = resourceManager.getResource(resourceLocation);
				Throwable var3 = null;

				SimpleTexture.TextureImage runtimeException;
				try {
					NativeImage nativeImage = NativeImage.read(resource.getInputStream());
					TextureMetadataSection textureMetadataSection = null;

					try {
						textureMetadataSection = resource.getMetadata(TextureMetadataSection.SERIALIZER);
					} catch (RuntimeException var17) {
						SimpleTexture.LOGGER.warn("Failed reading metadata of: {}", resourceLocation, var17);
					}

					runtimeException = new SimpleTexture.TextureImage(textureMetadataSection, nativeImage);
				} catch (Throwable var18) {
					var3 = var18;
					throw var18;
				} finally {
					if (resource != null) {
						if (var3 != null) {
							try {
								resource.close();
							} catch (Throwable var16) {
								var3.addSuppressed(var16);
							}
						} else {
							resource.close();
						}
					}
				}

				return runtimeException;
			} catch (IOException var20) {
				return new SimpleTexture.TextureImage(var20);
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

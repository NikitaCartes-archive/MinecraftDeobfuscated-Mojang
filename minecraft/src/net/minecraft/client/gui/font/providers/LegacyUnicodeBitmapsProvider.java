package net.minecraft.client.gui.font.providers;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class LegacyUnicodeBitmapsProvider implements GlyphProvider {
	private static final Logger LOGGER = LogManager.getLogger();
	private final ResourceManager resourceManager;
	private final byte[] sizes;
	private final String texturePattern;
	private final Map<ResourceLocation, NativeImage> textures = Maps.<ResourceLocation, NativeImage>newHashMap();

	public LegacyUnicodeBitmapsProvider(ResourceManager resourceManager, byte[] bs, String string) {
		this.resourceManager = resourceManager;
		this.sizes = bs;
		this.texturePattern = string;

		for (int i = 0; i < 256; i++) {
			char c = (char)(i * 256);
			ResourceLocation resourceLocation = this.getSheetLocation(c);

			try {
				Resource resource = this.resourceManager.getResource(resourceLocation);
				Throwable var8 = null;

				try (NativeImage nativeImage = NativeImage.read(NativeImage.Format.RGBA, resource.getInputStream())) {
					if (nativeImage.getWidth() == 256 && nativeImage.getHeight() == 256) {
						for (int j = 0; j < 256; j++) {
							byte b = bs[c + j];
							if (b != 0 && getLeft(b) > getRight(b)) {
								bs[c + j] = 0;
							}
						}
						continue;
					}
				} catch (Throwable var41) {
					var8 = var41;
					throw var41;
				} finally {
					if (resource != null) {
						if (var8 != null) {
							try {
								resource.close();
							} catch (Throwable var37) {
								var8.addSuppressed(var37);
							}
						} else {
							resource.close();
						}
					}
				}
			} catch (IOException var43) {
			}

			Arrays.fill(bs, c, c + 256, (byte)0);
		}
	}

	@Override
	public void close() {
		this.textures.values().forEach(NativeImage::close);
	}

	private ResourceLocation getSheetLocation(char c) {
		ResourceLocation resourceLocation = new ResourceLocation(String.format(this.texturePattern, String.format("%02x", c / 256)));
		return new ResourceLocation(resourceLocation.getNamespace(), "textures/" + resourceLocation.getPath());
	}

	@Nullable
	@Override
	public RawGlyph getGlyph(char c) {
		byte b = this.sizes[c];
		if (b != 0) {
			NativeImage nativeImage = (NativeImage)this.textures.computeIfAbsent(this.getSheetLocation(c), this::loadTexture);
			if (nativeImage != null) {
				int i = getLeft(b);
				return new LegacyUnicodeBitmapsProvider.Glyph(c % 16 * 16 + i, (c & 255) / 16 * 16, getRight(b) - i, 16, nativeImage);
			}
		}

		return null;
	}

	@Nullable
	private NativeImage loadTexture(ResourceLocation resourceLocation) {
		try {
			Resource resource = this.resourceManager.getResource(resourceLocation);
			Throwable var3 = null;

			NativeImage var4;
			try {
				var4 = NativeImage.read(NativeImage.Format.RGBA, resource.getInputStream());
			} catch (Throwable var14) {
				var3 = var14;
				throw var14;
			} finally {
				if (resource != null) {
					if (var3 != null) {
						try {
							resource.close();
						} catch (Throwable var13) {
							var3.addSuppressed(var13);
						}
					} else {
						resource.close();
					}
				}
			}

			return var4;
		} catch (IOException var16) {
			LOGGER.error("Couldn't load texture {}", resourceLocation, var16);
			return null;
		}
	}

	private static int getLeft(byte b) {
		return b >> 4 & 15;
	}

	private static int getRight(byte b) {
		return (b & 15) + 1;
	}

	@Environment(EnvType.CLIENT)
	public static class Builder implements GlyphProviderBuilder {
		private final ResourceLocation metadata;
		private final String texturePattern;

		public Builder(ResourceLocation resourceLocation, String string) {
			this.metadata = resourceLocation;
			this.texturePattern = string;
		}

		public static GlyphProviderBuilder fromJson(JsonObject jsonObject) {
			return new LegacyUnicodeBitmapsProvider.Builder(
				new ResourceLocation(GsonHelper.getAsString(jsonObject, "sizes")), GsonHelper.getAsString(jsonObject, "template")
			);
		}

		@Nullable
		@Override
		public GlyphProvider create(ResourceManager resourceManager) {
			try {
				Resource resource = Minecraft.getInstance().getResourceManager().getResource(this.metadata);
				Throwable var3 = null;

				LegacyUnicodeBitmapsProvider var5;
				try {
					byte[] bs = new byte[65536];
					resource.getInputStream().read(bs);
					var5 = new LegacyUnicodeBitmapsProvider(resourceManager, bs, this.texturePattern);
				} catch (Throwable var15) {
					var3 = var15;
					throw var15;
				} finally {
					if (resource != null) {
						if (var3 != null) {
							try {
								resource.close();
							} catch (Throwable var14) {
								var3.addSuppressed(var14);
							}
						} else {
							resource.close();
						}
					}
				}

				return var5;
			} catch (IOException var17) {
				LegacyUnicodeBitmapsProvider.LOGGER.error("Cannot load {}, unicode glyphs will not render correctly", this.metadata);
				return null;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static class Glyph implements RawGlyph {
		private final int width;
		private final int height;
		private final int sourceX;
		private final int sourceY;
		private final NativeImage source;

		private Glyph(int i, int j, int k, int l, NativeImage nativeImage) {
			this.width = k;
			this.height = l;
			this.sourceX = i;
			this.sourceY = j;
			this.source = nativeImage;
		}

		@Override
		public float getOversample() {
			return 2.0F;
		}

		@Override
		public int getPixelWidth() {
			return this.width;
		}

		@Override
		public int getPixelHeight() {
			return this.height;
		}

		@Override
		public float getAdvance() {
			return (float)(this.width / 2 + 1);
		}

		@Override
		public void upload(int i, int j) {
			this.source.upload(0, i, j, this.sourceX, this.sourceY, this.width, this.height, false);
		}

		@Override
		public boolean isColored() {
			return this.source.format().components() > 1;
		}

		@Override
		public float getShadowOffset() {
			return 0.5F;
		}

		@Override
		public float getBoldOffset() {
			return 0.5F;
		}
	}
}

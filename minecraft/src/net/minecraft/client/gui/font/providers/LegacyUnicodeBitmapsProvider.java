package net.minecraft.client.gui.font.providers;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.IllegalFormatException;
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
	static final Logger LOGGER = LogManager.getLogger();
	private static final int UNICODE_SHEETS = 256;
	private static final int CHARS_PER_SHEET = 256;
	private static final int TEXTURE_SIZE = 256;
	private final ResourceManager resourceManager;
	private final byte[] sizes;
	private final String texturePattern;
	private final Map<ResourceLocation, NativeImage> textures = Maps.<ResourceLocation, NativeImage>newHashMap();

	public LegacyUnicodeBitmapsProvider(ResourceManager resourceManager, byte[] bs, String string) {
		this.resourceManager = resourceManager;
		this.sizes = bs;
		this.texturePattern = string;

		for (int i = 0; i < 256; i++) {
			int j = i * 256;
			ResourceLocation resourceLocation = this.getSheetLocation(j);

			try {
				Resource resource = this.resourceManager.getResource(resourceLocation);

				label90: {
					label89:
					try (NativeImage nativeImage = NativeImage.read(NativeImage.Format.RGBA, resource.getInputStream())) {
						if (nativeImage.getWidth() == 256 && nativeImage.getHeight() == 256) {
							int k = 0;

							while (true) {
								if (k >= 256) {
									break label89;
								}

								byte b = bs[j + k];
								if (b != 0 && getLeft(b) > getRight(b)) {
									bs[j + k] = 0;
								}

								k++;
							}
						}
						break label90;
					} catch (Throwable var14) {
						if (resource != null) {
							try {
								resource.close();
							} catch (Throwable var11) {
								var14.addSuppressed(var11);
							}
						}

						throw var14;
					}

					if (resource != null) {
						resource.close();
					}
					continue;
				}

				if (resource != null) {
					resource.close();
				}
			} catch (IOException var15) {
			}

			Arrays.fill(bs, j, j + 256, (byte)0);
		}
	}

	@Override
	public void close() {
		this.textures.values().forEach(NativeImage::close);
	}

	private ResourceLocation getSheetLocation(int i) {
		ResourceLocation resourceLocation = new ResourceLocation(String.format(this.texturePattern, String.format("%02x", i / 256)));
		return new ResourceLocation(resourceLocation.getNamespace(), "textures/" + resourceLocation.getPath());
	}

	@Nullable
	@Override
	public RawGlyph getGlyph(int i) {
		if (i >= 0 && i <= 65535) {
			byte b = this.sizes[i];
			if (b != 0) {
				NativeImage nativeImage = (NativeImage)this.textures.computeIfAbsent(this.getSheetLocation(i), this::loadTexture);
				if (nativeImage != null) {
					int j = getLeft(b);
					return new LegacyUnicodeBitmapsProvider.Glyph(i % 16 * 16 + j, (i & 0xFF) / 16 * 16, getRight(b) - j, 16, nativeImage);
				}
			}

			return null;
		} else {
			return null;
		}
	}

	@Override
	public IntSet getSupportedGlyphs() {
		IntSet intSet = new IntOpenHashSet();

		for (int i = 0; i < 65535; i++) {
			if (this.sizes[i] != 0) {
				intSet.add(i);
			}
		}

		return intSet;
	}

	@Nullable
	private NativeImage loadTexture(ResourceLocation resourceLocation) {
		try {
			Resource resource = this.resourceManager.getResource(resourceLocation);

			NativeImage var3;
			try {
				var3 = NativeImage.read(NativeImage.Format.RGBA, resource.getInputStream());
			} catch (Throwable var6) {
				if (resource != null) {
					try {
						resource.close();
					} catch (Throwable var5) {
						var6.addSuppressed(var5);
					}
				}

				throw var6;
			}

			if (resource != null) {
				resource.close();
			}

			return var3;
		} catch (IOException var7) {
			LOGGER.error("Couldn't load texture {}", resourceLocation, var7);
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
			return new LegacyUnicodeBitmapsProvider.Builder(new ResourceLocation(GsonHelper.getAsString(jsonObject, "sizes")), getTemplate(jsonObject));
		}

		private static String getTemplate(JsonObject jsonObject) {
			String string = GsonHelper.getAsString(jsonObject, "template");

			try {
				String.format(string, "");
				return string;
			} catch (IllegalFormatException var3) {
				throw new JsonParseException("Invalid legacy unicode template supplied, expected single '%s': " + string);
			}
		}

		@Nullable
		@Override
		public GlyphProvider create(ResourceManager resourceManager) {
			try {
				Resource resource = Minecraft.getInstance().getResourceManager().getResource(this.metadata);

				LegacyUnicodeBitmapsProvider var4;
				try {
					byte[] bs = new byte[65536];
					resource.getInputStream().read(bs);
					var4 = new LegacyUnicodeBitmapsProvider(resourceManager, bs, this.texturePattern);
				} catch (Throwable var6) {
					if (resource != null) {
						try {
							resource.close();
						} catch (Throwable var5) {
							var6.addSuppressed(var5);
						}
					}

					throw var6;
				}

				if (resource != null) {
					resource.close();
				}

				return var4;
			} catch (IOException var7) {
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

		Glyph(int i, int j, int k, int l, NativeImage nativeImage) {
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
			this.source.upload(0, i, j, this.sourceX, this.sourceY, this.width, this.height, false, false);
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

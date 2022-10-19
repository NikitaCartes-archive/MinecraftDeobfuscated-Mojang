package net.minecraft.client.gui.font.providers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class LegacyUnicodeBitmapsProvider implements GlyphProvider {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final int UNICODE_SHEETS = 256;
	private static final int CODEPOINTS_PER_SHEET = 256;
	private static final int TEXTURE_SIZE = 256;
	private static final byte NO_GLYPH = 0;
	private static final int TOTAL_CODEPOINTS = 65536;
	private final byte[] sizes;
	private final LegacyUnicodeBitmapsProvider.Sheet[] sheets = new LegacyUnicodeBitmapsProvider.Sheet[256];

	public LegacyUnicodeBitmapsProvider(ResourceManager resourceManager, byte[] bs, String string) {
		this.sizes = bs;
		Set<ResourceLocation> set = new HashSet();

		for (int i = 0; i < 256; i++) {
			int j = i * 256;
			set.add(getSheetLocation(string, j));
		}

		String string2 = getCommonSearchPrefix(set);
		Map<ResourceLocation, CompletableFuture<NativeImage>> map = new HashMap();
		resourceManager.listResources(string2, set::contains)
			.forEach((resourceLocationx, resource) -> map.put(resourceLocationx, CompletableFuture.supplyAsync(() -> {
					try {
						InputStream inputStream = resource.open();

						NativeImage var3;
						try {
							var3 = NativeImage.read(NativeImage.Format.RGBA, inputStream);
						} catch (Throwable var6x) {
							if (inputStream != null) {
								try {
									inputStream.close();
								} catch (Throwable var5x) {
									var6x.addSuppressed(var5x);
								}
							}

							throw var6x;
						}

						if (inputStream != null) {
							inputStream.close();
						}

						return var3;
					} catch (IOException var7x) {
						LOGGER.error("Failed to read resource {} from pack {}", resourceLocationx, resource.sourcePackId());
						return null;
					}
				}, Util.backgroundExecutor())));
		List<CompletableFuture<?>> list = new ArrayList(256);

		for (int k = 0; k < 256; k++) {
			int l = k * 256;
			int m = k;
			ResourceLocation resourceLocation = getSheetLocation(string, l);
			CompletableFuture<NativeImage> completableFuture = (CompletableFuture<NativeImage>)map.get(resourceLocation);
			if (completableFuture != null) {
				list.add(completableFuture.thenAcceptAsync(nativeImage -> {
					if (nativeImage != null) {
						if (nativeImage.getWidth() == 256 && nativeImage.getHeight() == 256) {
							for (int kx = 0; kx < 256; kx++) {
								byte b = bs[l + kx];
								if (b != 0 && getLeft(b) > getRight(b)) {
									bs[l + kx] = 0;
								}
							}

							this.sheets[m] = new LegacyUnicodeBitmapsProvider.Sheet(bs, nativeImage);
						} else {
							nativeImage.close();
							Arrays.fill(bs, l, l + 256, (byte)0);
						}
					}
				}, Util.backgroundExecutor()));
			}
		}

		CompletableFuture.allOf((CompletableFuture[])list.toArray(CompletableFuture[]::new)).join();
	}

	private static String getCommonSearchPrefix(Set<ResourceLocation> set) {
		String string = StringUtils.getCommonPrefix((String[])set.stream().map(ResourceLocation::getPath).toArray(String[]::new));
		int i = string.lastIndexOf("/");
		return i == -1 ? "" : string.substring(0, i);
	}

	@Override
	public void close() {
		for (LegacyUnicodeBitmapsProvider.Sheet sheet : this.sheets) {
			if (sheet != null) {
				sheet.close();
			}
		}
	}

	private static ResourceLocation getSheetLocation(String string, int i) {
		ResourceLocation resourceLocation = new ResourceLocation(String.format(Locale.ROOT, string, String.format("%02x", i / 256)));
		return resourceLocation.withPrefix("textures/");
	}

	@Nullable
	@Override
	public GlyphInfo getGlyph(int i) {
		if (i >= 0 && i < this.sizes.length) {
			int j = i / 256;
			LegacyUnicodeBitmapsProvider.Sheet sheet = this.sheets[j];
			return sheet != null ? sheet.getGlyph(i) : null;
		} else {
			return null;
		}
	}

	@Override
	public IntSet getSupportedGlyphs() {
		IntSet intSet = new IntOpenHashSet();

		for (int i = 0; i < this.sizes.length; i++) {
			if (this.sizes[i] != 0) {
				intSet.add(i);
			}
		}

		return intSet;
	}

	static int getLeft(byte b) {
		return b >> 4 & 15;
	}

	static int getRight(byte b) {
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
				String.format(Locale.ROOT, string, "");
				return string;
			} catch (IllegalFormatException var3) {
				throw new JsonParseException("Invalid legacy unicode template supplied, expected single '%s': " + string);
			}
		}

		@Nullable
		@Override
		public GlyphProvider create(ResourceManager resourceManager) {
			try {
				InputStream inputStream = Minecraft.getInstance().getResourceManager().open(this.metadata);

				LegacyUnicodeBitmapsProvider var4;
				try {
					byte[] bs = inputStream.readNBytes(65536);
					var4 = new LegacyUnicodeBitmapsProvider(resourceManager, bs, this.texturePattern);
				} catch (Throwable var6) {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (Throwable var5) {
							var6.addSuppressed(var5);
						}
					}

					throw var6;
				}

				if (inputStream != null) {
					inputStream.close();
				}

				return var4;
			} catch (IOException var7) {
				LegacyUnicodeBitmapsProvider.LOGGER.error("Cannot load {}, unicode glyphs will not render correctly", this.metadata);
				return null;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static record Glyph(int sourceX, int sourceY, int width, int height, NativeImage source) implements GlyphInfo {

		@Override
		public float getAdvance() {
			return (float)(this.width / 2 + 1);
		}

		@Override
		public float getShadowOffset() {
			return 0.5F;
		}

		@Override
		public float getBoldOffset() {
			return 0.5F;
		}

		@Override
		public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
			return (BakedGlyph)function.apply(new SheetGlyphInfo() {
				@Override
				public float getOversample() {
					return 2.0F;
				}

				@Override
				public int getPixelWidth() {
					return Glyph.this.width;
				}

				@Override
				public int getPixelHeight() {
					return Glyph.this.height;
				}

				@Override
				public void upload(int i, int j) {
					Glyph.this.source.upload(0, i, j, Glyph.this.sourceX, Glyph.this.sourceY, Glyph.this.width, Glyph.this.height, false, false);
				}

				@Override
				public boolean isColored() {
					return Glyph.this.source.format().components() > 1;
				}
			});
		}
	}

	@Environment(EnvType.CLIENT)
	static class Sheet implements AutoCloseable {
		private final byte[] sizes;
		private final NativeImage source;

		Sheet(byte[] bs, NativeImage nativeImage) {
			this.sizes = bs;
			this.source = nativeImage;
		}

		public void close() {
			this.source.close();
		}

		@Nullable
		public GlyphInfo getGlyph(int i) {
			byte b = this.sizes[i];
			if (b != 0) {
				int j = LegacyUnicodeBitmapsProvider.getLeft(b);
				return new LegacyUnicodeBitmapsProvider.Glyph(i % 16 * 16 + j, (i & 0xFF) / 16 * 16, LegacyUnicodeBitmapsProvider.getRight(b) - j, 16, this.source);
			} else {
				return null;
			}
		}
	}
}

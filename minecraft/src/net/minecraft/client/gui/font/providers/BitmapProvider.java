package net.minecraft.client.gui.font.providers;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class BitmapProvider implements GlyphProvider {
	static final Logger LOGGER = LogUtils.getLogger();
	private final NativeImage image;
	private final CodepointMap<BitmapProvider.Glyph> glyphs;

	BitmapProvider(NativeImage nativeImage, CodepointMap<BitmapProvider.Glyph> codepointMap) {
		this.image = nativeImage;
		this.glyphs = codepointMap;
	}

	@Override
	public void close() {
		this.image.close();
	}

	@Nullable
	@Override
	public GlyphInfo getGlyph(int i) {
		return this.glyphs.get(i);
	}

	@Override
	public IntSet getSupportedGlyphs() {
		return IntSets.unmodifiable(this.glyphs.keySet());
	}

	@Environment(EnvType.CLIENT)
	public static class Builder implements GlyphProviderBuilder {
		private final ResourceLocation texture;
		private final List<int[]> chars;
		private final int height;
		private final int ascent;

		public Builder(ResourceLocation resourceLocation, int i, int j, List<int[]> list) {
			this.texture = resourceLocation.withPrefix("textures/");
			this.chars = list;
			this.height = i;
			this.ascent = j;
		}

		public static BitmapProvider.Builder fromJson(JsonObject jsonObject) {
			int i = GsonHelper.getAsInt(jsonObject, "height", 8);
			int j = GsonHelper.getAsInt(jsonObject, "ascent");
			if (j > i) {
				throw new JsonParseException("Ascent " + j + " higher than height " + i);
			} else {
				List<int[]> list = Lists.<int[]>newArrayList();
				JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "chars");

				for (int k = 0; k < jsonArray.size(); k++) {
					String string = GsonHelper.convertToString(jsonArray.get(k), "chars[" + k + "]");
					int[] is = string.codePoints().toArray();
					if (k > 0) {
						int l = ((int[])list.get(0)).length;
						if (is.length != l) {
							throw new JsonParseException("Elements of chars have to be the same length (found: " + is.length + ", expected: " + l + "), pad with \\u0000");
						}
					}

					list.add(is);
				}

				if (!list.isEmpty() && ((int[])list.get(0)).length != 0) {
					return new BitmapProvider.Builder(new ResourceLocation(GsonHelper.getAsString(jsonObject, "file")), i, j, list);
				} else {
					throw new JsonParseException("Expected to find data in chars, found none.");
				}
			}
		}

		@Override
		public Either<GlyphProviderBuilder.Loader, GlyphProviderBuilder.Reference> build() {
			return Either.left(this::load);
		}

		private GlyphProvider load(ResourceManager resourceManager) throws IOException {
			InputStream inputStream = resourceManager.open(this.texture);

			BitmapProvider var21;
			try {
				NativeImage nativeImage = NativeImage.read(NativeImage.Format.RGBA, inputStream);
				int i = nativeImage.getWidth();
				int j = nativeImage.getHeight();
				int k = i / ((int[])this.chars.get(0)).length;
				int l = j / this.chars.size();
				float f = (float)this.height / (float)l;
				CodepointMap<BitmapProvider.Glyph> codepointMap = new CodepointMap<>(BitmapProvider.Glyph[]::new, BitmapProvider.Glyph[][]::new);

				for (int m = 0; m < this.chars.size(); m++) {
					int n = 0;

					for (int o : (int[])this.chars.get(m)) {
						int p = n++;
						if (o != 0) {
							int q = this.getActualGlyphWidth(nativeImage, k, l, p, m);
							BitmapProvider.Glyph glyph = codepointMap.put(
								o, new BitmapProvider.Glyph(f, nativeImage, p * k, m * l, k, l, (int)(0.5 + (double)((float)q * f)) + 1, this.ascent)
							);
							if (glyph != null) {
								BitmapProvider.LOGGER.warn("Codepoint '{}' declared multiple times in {}", Integer.toHexString(o), this.texture);
							}
						}
					}
				}

				var21 = new BitmapProvider(nativeImage, codepointMap);
			} catch (Throwable var20) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var19) {
						var20.addSuppressed(var19);
					}
				}

				throw var20;
			}

			if (inputStream != null) {
				inputStream.close();
			}

			return var21;
		}

		private int getActualGlyphWidth(NativeImage nativeImage, int i, int j, int k, int l) {
			int m;
			for (m = i - 1; m >= 0; m--) {
				int n = k * i + m;

				for (int o = 0; o < j; o++) {
					int p = l * j + o;
					if (nativeImage.getLuminanceOrAlpha(n, p) != 0) {
						return m + 1;
					}
				}
			}

			return m + 1;
		}
	}

	@Environment(EnvType.CLIENT)
	static record Glyph(float scale, NativeImage image, int offsetX, int offsetY, int width, int height, int advance, int ascent) implements GlyphInfo {

		@Override
		public float getAdvance() {
			return (float)this.advance;
		}

		@Override
		public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
			return (BakedGlyph)function.apply(new SheetGlyphInfo() {
				@Override
				public float getOversample() {
					return 1.0F / Glyph.this.scale;
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
				public float getBearingY() {
					return SheetGlyphInfo.super.getBearingY() + 7.0F - (float)Glyph.this.ascent;
				}

				@Override
				public void upload(int i, int j) {
					Glyph.this.image.upload(0, i, j, Glyph.this.offsetX, Glyph.this.offsetY, Glyph.this.width, Glyph.this.height, false, false);
				}

				@Override
				public boolean isColored() {
					return Glyph.this.image.format().components() > 1;
				}
			});
		}
	}
}

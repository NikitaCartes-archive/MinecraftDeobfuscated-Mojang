package com.mojang.blaze3d.font;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Function;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class TrueTypeGlyphProvider implements GlyphProvider {
	@Nullable
	private ByteBuffer fontMemory;
	@Nullable
	private STBTTFontinfo font;
	final float oversample;
	private final IntSet skip = new IntArraySet();
	final float shiftX;
	final float shiftY;
	final float pointScale;
	final float ascent;

	public TrueTypeGlyphProvider(ByteBuffer byteBuffer, STBTTFontinfo sTBTTFontinfo, float f, float g, float h, float i, String string) {
		this.fontMemory = byteBuffer;
		this.font = sTBTTFontinfo;
		this.oversample = g;
		string.codePoints().forEach(this.skip::add);
		this.shiftX = h * g;
		this.shiftY = i * g;
		this.pointScale = STBTruetype.stbtt_ScaleForPixelHeight(sTBTTFontinfo, f * g);

		try (MemoryStack memoryStack = MemoryStack.stackPush()) {
			IntBuffer intBuffer = memoryStack.mallocInt(1);
			IntBuffer intBuffer2 = memoryStack.mallocInt(1);
			IntBuffer intBuffer3 = memoryStack.mallocInt(1);
			STBTruetype.stbtt_GetFontVMetrics(sTBTTFontinfo, intBuffer, intBuffer2, intBuffer3);
			this.ascent = (float)intBuffer.get(0) * this.pointScale;
		}
	}

	@Nullable
	@Override
	public GlyphInfo getGlyph(int i) {
		STBTTFontinfo sTBTTFontinfo = this.validateFontOpen();
		if (this.skip.contains(i)) {
			return null;
		} else {
			GlyphInfo.SpaceGlyphInfo var14;
			try (MemoryStack memoryStack = MemoryStack.stackPush()) {
				int j = STBTruetype.stbtt_FindGlyphIndex(sTBTTFontinfo, i);
				if (j == 0) {
					return null;
				}

				IntBuffer intBuffer = memoryStack.mallocInt(1);
				IntBuffer intBuffer2 = memoryStack.mallocInt(1);
				IntBuffer intBuffer3 = memoryStack.mallocInt(1);
				IntBuffer intBuffer4 = memoryStack.mallocInt(1);
				IntBuffer intBuffer5 = memoryStack.mallocInt(1);
				IntBuffer intBuffer6 = memoryStack.mallocInt(1);
				STBTruetype.stbtt_GetGlyphHMetrics(sTBTTFontinfo, j, intBuffer5, intBuffer6);
				STBTruetype.stbtt_GetGlyphBitmapBoxSubpixel(
					sTBTTFontinfo, j, this.pointScale, this.pointScale, this.shiftX, this.shiftY, intBuffer, intBuffer2, intBuffer3, intBuffer4
				);
				float f = (float)intBuffer5.get(0) * this.pointScale;
				int k = intBuffer3.get(0) - intBuffer.get(0);
				int l = intBuffer4.get(0) - intBuffer2.get(0);
				if (k > 0 && l > 0) {
					return new TrueTypeGlyphProvider.Glyph(
						intBuffer.get(0), intBuffer3.get(0), -intBuffer2.get(0), -intBuffer4.get(0), f, (float)intBuffer6.get(0) * this.pointScale, j
					);
				}

				var14 = () -> f / this.oversample;
			}

			return var14;
		}
	}

	STBTTFontinfo validateFontOpen() {
		if (this.fontMemory != null && this.font != null) {
			return this.font;
		} else {
			throw new IllegalArgumentException("Provider already closed");
		}
	}

	@Override
	public void close() {
		if (this.font != null) {
			this.font.free();
			this.font = null;
		}

		MemoryUtil.memFree(this.fontMemory);
		this.fontMemory = null;
	}

	@Override
	public IntSet getSupportedGlyphs() {
		return (IntSet)IntStream.range(0, 65535).filter(i -> !this.skip.contains(i)).collect(IntOpenHashSet::new, IntCollection::add, IntCollection::addAll);
	}

	@Environment(EnvType.CLIENT)
	class Glyph implements GlyphInfo {
		final int width;
		final int height;
		final float bearingX;
		final float bearingY;
		private final float advance;
		final int index;

		Glyph(int i, int j, int k, int l, float f, float g, int m) {
			this.width = j - i;
			this.height = k - l;
			this.advance = f / TrueTypeGlyphProvider.this.oversample;
			this.bearingX = (g + (float)i + TrueTypeGlyphProvider.this.shiftX) / TrueTypeGlyphProvider.this.oversample;
			this.bearingY = (TrueTypeGlyphProvider.this.ascent - (float)k + TrueTypeGlyphProvider.this.shiftY) / TrueTypeGlyphProvider.this.oversample;
			this.index = m;
		}

		@Override
		public float getAdvance() {
			return this.advance;
		}

		@Override
		public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
			return (BakedGlyph)function.apply(
				new SheetGlyphInfo() {
					@Override
					public int getPixelWidth() {
						return Glyph.this.width;
					}

					@Override
					public int getPixelHeight() {
						return Glyph.this.height;
					}

					@Override
					public float getOversample() {
						return TrueTypeGlyphProvider.this.oversample;
					}

					@Override
					public float getBearingX() {
						return Glyph.this.bearingX;
					}

					@Override
					public float getBearingY() {
						return Glyph.this.bearingY;
					}

					@Override
					public void upload(int i, int j) {
						STBTTFontinfo sTBTTFontinfo = TrueTypeGlyphProvider.this.validateFontOpen();
						NativeImage nativeImage = new NativeImage(NativeImage.Format.LUMINANCE, Glyph.this.width, Glyph.this.height, false);
						nativeImage.copyFromFont(
							sTBTTFontinfo,
							Glyph.this.index,
							Glyph.this.width,
							Glyph.this.height,
							TrueTypeGlyphProvider.this.pointScale,
							TrueTypeGlyphProvider.this.pointScale,
							TrueTypeGlyphProvider.this.shiftX,
							TrueTypeGlyphProvider.this.shiftY,
							0,
							0
						);
						nativeImage.upload(0, i, j, 0, 0, Glyph.this.width, Glyph.this.height, false, true);
					}

					@Override
					public boolean isColored() {
						return false;
					}
				}
			);
		}
	}
}

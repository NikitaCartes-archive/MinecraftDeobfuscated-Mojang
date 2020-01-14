package com.mojang.blaze3d.font;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class TrueTypeGlyphProvider implements GlyphProvider {
	private final ByteBuffer fontMemory;
	private final STBTTFontinfo font;
	private final float oversample;
	private final CharSet skip = new CharArraySet();
	private final float shiftX;
	private final float shiftY;
	private final float pointScale;
	private final float ascent;

	public TrueTypeGlyphProvider(ByteBuffer byteBuffer, STBTTFontinfo sTBTTFontinfo, float f, float g, float h, float i, String string) {
		this.fontMemory = byteBuffer;
		this.font = sTBTTFontinfo;
		this.oversample = g;
		string.chars().forEach(ix -> this.skip.add((char)(ix & 65535)));
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
	public TrueTypeGlyphProvider.Glyph getGlyph(char c) {
		if (this.skip.contains(c)) {
			return null;
		} else {
			TrueTypeGlyphProvider.Glyph var13;
			try (MemoryStack memoryStack = MemoryStack.stackPush()) {
				IntBuffer intBuffer = memoryStack.mallocInt(1);
				IntBuffer intBuffer2 = memoryStack.mallocInt(1);
				IntBuffer intBuffer3 = memoryStack.mallocInt(1);
				IntBuffer intBuffer4 = memoryStack.mallocInt(1);
				int i = STBTruetype.stbtt_FindGlyphIndex(this.font, c);
				if (i == 0) {
					return null;
				}

				STBTruetype.stbtt_GetGlyphBitmapBoxSubpixel(
					this.font, i, this.pointScale, this.pointScale, this.shiftX, this.shiftY, intBuffer, intBuffer2, intBuffer3, intBuffer4
				);
				int j = intBuffer3.get(0) - intBuffer.get(0);
				int k = intBuffer4.get(0) - intBuffer2.get(0);
				if (j == 0 || k == 0) {
					return null;
				}

				IntBuffer intBuffer5 = memoryStack.mallocInt(1);
				IntBuffer intBuffer6 = memoryStack.mallocInt(1);
				STBTruetype.stbtt_GetGlyphHMetrics(this.font, i, intBuffer5, intBuffer6);
				var13 = new TrueTypeGlyphProvider.Glyph(
					intBuffer.get(0),
					intBuffer3.get(0),
					-intBuffer2.get(0),
					-intBuffer4.get(0),
					(float)intBuffer5.get(0) * this.pointScale,
					(float)intBuffer6.get(0) * this.pointScale,
					i
				);
			}

			return var13;
		}
	}

	@Override
	public void close() {
		this.font.free();
		MemoryUtil.memFree(this.fontMemory);
	}

	@Environment(EnvType.CLIENT)
	class Glyph implements RawGlyph {
		private final int width;
		private final int height;
		private final float bearingX;
		private final float bearingY;
		private final float advance;
		private final int index;

		private Glyph(int i, int j, int k, int l, float f, float g, int m) {
			this.width = j - i;
			this.height = k - l;
			this.advance = f / TrueTypeGlyphProvider.this.oversample;
			this.bearingX = (g + (float)i + TrueTypeGlyphProvider.this.shiftX) / TrueTypeGlyphProvider.this.oversample;
			this.bearingY = (TrueTypeGlyphProvider.this.ascent - (float)k + TrueTypeGlyphProvider.this.shiftY) / TrueTypeGlyphProvider.this.oversample;
			this.index = m;
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
		public float getOversample() {
			return TrueTypeGlyphProvider.this.oversample;
		}

		@Override
		public float getAdvance() {
			return this.advance;
		}

		@Override
		public float getBearingX() {
			return this.bearingX;
		}

		@Override
		public float getBearingY() {
			return this.bearingY;
		}

		@Override
		public void upload(int i, int j) {
			NativeImage nativeImage = new NativeImage(NativeImage.Format.LUMINANCE, this.width, this.height, false);
			nativeImage.copyFromFont(
				TrueTypeGlyphProvider.this.font,
				this.index,
				this.width,
				this.height,
				TrueTypeGlyphProvider.this.pointScale,
				TrueTypeGlyphProvider.this.pointScale,
				TrueTypeGlyphProvider.this.shiftX,
				TrueTypeGlyphProvider.this.shiftY,
				0,
				0
			);
			nativeImage.upload(0, i, j, 0, 0, this.width, this.height, false, true);
		}

		@Override
		public boolean isColored() {
			return false;
		}
	}
}

package com.mojang.blaze3d.font;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Locale;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;

@Environment(EnvType.CLIENT)
public class TrueTypeGlyphProvider implements GlyphProvider {
	@Nullable
	private ByteBuffer fontMemory;
	@Nullable
	private FT_Face face;
	final float oversample;
	private final CodepointMap<TrueTypeGlyphProvider.GlyphEntry> glyphs = new CodepointMap<>(
		TrueTypeGlyphProvider.GlyphEntry[]::new, TrueTypeGlyphProvider.GlyphEntry[][]::new
	);

	public TrueTypeGlyphProvider(ByteBuffer byteBuffer, FT_Face fT_Face, float f, float g, float h, float i, String string) {
		this.fontMemory = byteBuffer;
		this.face = fT_Face;
		this.oversample = g;
		IntSet intSet = new IntArraySet();
		string.codePoints().forEach(intSet::add);
		int j = Math.round(f * g);
		FreeType.FT_Set_Pixel_Sizes(fT_Face, j, j);
		float k = h * g;
		float l = -i * g;

		try (MemoryStack memoryStack = MemoryStack.stackPush()) {
			FT_Vector fT_Vector = FreeTypeUtil.setVector(FT_Vector.malloc(memoryStack), k, l);
			FreeType.FT_Set_Transform(fT_Face, null, fT_Vector);
			IntBuffer intBuffer = memoryStack.mallocInt(1);
			int m = (int)FreeType.FT_Get_First_Char(fT_Face, intBuffer);

			while (true) {
				int n = intBuffer.get(0);
				if (n == 0) {
					return;
				}

				if (!intSet.contains(m)) {
					this.glyphs.put(m, new TrueTypeGlyphProvider.GlyphEntry(n));
				}

				m = (int)FreeType.FT_Get_Next_Char(fT_Face, (long)m, intBuffer);
			}
		}
	}

	@Nullable
	@Override
	public GlyphInfo getGlyph(int i) {
		TrueTypeGlyphProvider.GlyphEntry glyphEntry = this.glyphs.get(i);
		return glyphEntry != null ? this.getOrLoadGlyphInfo(i, glyphEntry) : null;
	}

	private GlyphInfo getOrLoadGlyphInfo(int i, TrueTypeGlyphProvider.GlyphEntry glyphEntry) {
		GlyphInfo glyphInfo = glyphEntry.glyph;
		if (glyphInfo == null) {
			FT_Face fT_Face = this.validateFontOpen();
			synchronized (fT_Face) {
				glyphInfo = glyphEntry.glyph;
				if (glyphInfo == null) {
					glyphInfo = this.loadGlyph(i, fT_Face, glyphEntry.index);
					glyphEntry.glyph = glyphInfo;
				}
			}
		}

		return glyphInfo;
	}

	private GlyphInfo loadGlyph(int i, FT_Face fT_Face, int j) {
		int k = FreeType.FT_Load_Glyph(fT_Face, j, 4194312);
		if (k != 0) {
			FreeTypeUtil.assertError(k, String.format(Locale.ROOT, "Loading glyph U+%06X", i));
		}

		FT_GlyphSlot fT_GlyphSlot = fT_Face.glyph();
		if (fT_GlyphSlot == null) {
			throw new NullPointerException(String.format(Locale.ROOT, "Glyph U+%06X not initialized", i));
		} else {
			float f = FreeTypeUtil.x(fT_GlyphSlot.advance());
			FT_Bitmap fT_Bitmap = fT_GlyphSlot.bitmap();
			int l = fT_GlyphSlot.bitmap_left();
			int m = fT_GlyphSlot.bitmap_top();
			int n = fT_Bitmap.width();
			int o = fT_Bitmap.rows();
			return (GlyphInfo)(n > 0 && o > 0 ? new TrueTypeGlyphProvider.Glyph((float)l, (float)m, n, o, f, j) : () -> f / this.oversample);
		}
	}

	FT_Face validateFontOpen() {
		if (this.fontMemory != null && this.face != null) {
			return this.face;
		} else {
			throw new IllegalStateException("Provider already closed");
		}
	}

	@Override
	public void close() {
		if (this.face != null) {
			synchronized (FreeTypeUtil.LIBRARY_LOCK) {
				FreeTypeUtil.checkError(FreeType.FT_Done_Face(this.face), "Deleting face");
			}

			this.face = null;
		}

		MemoryUtil.memFree(this.fontMemory);
		this.fontMemory = null;
	}

	@Override
	public IntSet getSupportedGlyphs() {
		return this.glyphs.keySet();
	}

	@Environment(EnvType.CLIENT)
	class Glyph implements GlyphInfo {
		final int width;
		final int height;
		final float bearingX;
		final float bearingY;
		private final float advance;
		final int index;

		Glyph(final float f, final float g, final int i, final int j, final float h, final int k) {
			this.width = i;
			this.height = j;
			this.advance = h / TrueTypeGlyphProvider.this.oversample;
			this.bearingX = f / TrueTypeGlyphProvider.this.oversample;
			this.bearingY = g / TrueTypeGlyphProvider.this.oversample;
			this.index = k;
		}

		@Override
		public float getAdvance() {
			return this.advance;
		}

		@Override
		public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
			return (BakedGlyph)function.apply(new SheetGlyphInfo() {
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
				public float getBearingLeft() {
					return Glyph.this.bearingX;
				}

				@Override
				public float getBearingTop() {
					return Glyph.this.bearingY;
				}

				@Override
				public void upload(int i, int j) {
					FT_Face fT_Face = TrueTypeGlyphProvider.this.validateFontOpen();
					NativeImage nativeImage = new NativeImage(NativeImage.Format.LUMINANCE, Glyph.this.width, Glyph.this.height, false);
					if (nativeImage.copyFromFont(fT_Face, Glyph.this.index)) {
						nativeImage.upload(0, i, j, 0, 0, Glyph.this.width, Glyph.this.height, false, true);
					} else {
						nativeImage.close();
					}
				}

				@Override
				public boolean isColored() {
					return false;
				}
			});
		}
	}

	@Environment(EnvType.CLIENT)
	static class GlyphEntry {
		final int index;
		@Nullable
		volatile GlyphInfo glyph;

		GlyphEntry(int i) {
			this.index = i;
		}
	}
}

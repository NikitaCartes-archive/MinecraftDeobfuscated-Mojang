package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringDecomposer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class Font {
	private static final float EFFECT_DEPTH = 0.01F;
	private static final Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);
	public static final int ALPHA_CUTOFF = 8;
	public final int lineHeight = 9;
	public final RandomSource random = RandomSource.create();
	private final Function<ResourceLocation, FontSet> fonts;
	final boolean filterFishyGlyphs;
	private final StringSplitter splitter;

	public Font(Function<ResourceLocation, FontSet> function, boolean bl) {
		this.fonts = function;
		this.filterFishyGlyphs = bl;
		this.splitter = new StringSplitter((i, style) -> this.getFontSet(style.getFont()).getGlyphInfo(i, this.filterFishyGlyphs).getAdvance(style.isBold()));
	}

	FontSet getFontSet(ResourceLocation resourceLocation) {
		return (FontSet)this.fonts.apply(resourceLocation);
	}

	public String bidirectionalShaping(String string) {
		try {
			Bidi bidi = new Bidi(new ArabicShaping(8).shape(string), 127);
			bidi.setReorderingMode(0);
			return bidi.writeReordered(2);
		} catch (ArabicShapingException var3) {
			return string;
		}
	}

	public int drawInBatch(
		String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, Font.DisplayMode displayMode, int j, int k
	) {
		return this.drawInBatch(string, f, g, i, bl, matrix4f, multiBufferSource, displayMode, j, k, this.isBidirectional());
	}

	public int drawInBatch(
		String string,
		float f,
		float g,
		int i,
		boolean bl,
		Matrix4f matrix4f,
		MultiBufferSource multiBufferSource,
		Font.DisplayMode displayMode,
		int j,
		int k,
		boolean bl2
	) {
		return this.drawInternal(string, f, g, i, bl, matrix4f, multiBufferSource, displayMode, j, k, bl2);
	}

	public int drawInBatch(
		Component component, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, Font.DisplayMode displayMode, int j, int k
	) {
		return this.drawInBatch(component.getVisualOrderText(), f, g, i, bl, matrix4f, multiBufferSource, displayMode, j, k);
	}

	public int drawInBatch(
		FormattedCharSequence formattedCharSequence,
		float f,
		float g,
		int i,
		boolean bl,
		Matrix4f matrix4f,
		MultiBufferSource multiBufferSource,
		Font.DisplayMode displayMode,
		int j,
		int k
	) {
		return this.drawInternal(formattedCharSequence, f, g, i, bl, matrix4f, multiBufferSource, displayMode, j, k);
	}

	public void drawInBatch8xOutline(
		FormattedCharSequence formattedCharSequence, float f, float g, int i, int j, Matrix4f matrix4f, MultiBufferSource multiBufferSource, int k
	) {
		int l = adjustColor(j);
		Font.StringRenderOutput stringRenderOutput = new Font.StringRenderOutput(this, multiBufferSource, 0.0F, 0.0F, l, false, matrix4f, Font.DisplayMode.NORMAL, k);

		for (int m = -1; m <= 1; m++) {
			for (int n = -1; n <= 1; n++) {
				if (m != 0 || n != 0) {
					float[] fs = new float[]{f};
					int o = m;
					int p = n;
					formattedCharSequence.accept((lx, style, mx) -> {
						boolean bl = style.isBold();
						FontSet fontSet = this.getFontSet(style.getFont());
						GlyphInfo glyphInfo = fontSet.getGlyphInfo(mx, this.filterFishyGlyphs);
						stringRenderOutput.x = fs[0] + (float)o * glyphInfo.getShadowOffset();
						stringRenderOutput.y = g + (float)p * glyphInfo.getShadowOffset();
						fs[0] += glyphInfo.getAdvance(bl);
						return stringRenderOutput.accept(lx, style.withColor(l), mx);
					});
				}
			}
		}

		stringRenderOutput.renderCharacters();
		Font.StringRenderOutput stringRenderOutput2 = new Font.StringRenderOutput(
			this, multiBufferSource, f, g, adjustColor(i), false, matrix4f, Font.DisplayMode.POLYGON_OFFSET, k
		);
		formattedCharSequence.accept(stringRenderOutput2);
		stringRenderOutput2.finish(f);
	}

	private static int adjustColor(int i) {
		return (i & -67108864) == 0 ? ARGB.opaque(i) : i;
	}

	private int drawInternal(
		String string,
		float f,
		float g,
		int i,
		boolean bl,
		Matrix4f matrix4f,
		MultiBufferSource multiBufferSource,
		Font.DisplayMode displayMode,
		int j,
		int k,
		boolean bl2
	) {
		if (bl2) {
			string = this.bidirectionalShaping(string);
		}

		i = adjustColor(i);
		Matrix4f matrix4f2 = new Matrix4f(matrix4f);
		if (bl) {
			this.renderText(string, f, g, i, true, matrix4f, multiBufferSource, displayMode, j, k);
			matrix4f2.translate(SHADOW_OFFSET);
		}

		f = this.renderText(string, f, g, i, false, matrix4f2, multiBufferSource, displayMode, j, k);
		return (int)f + (bl ? 1 : 0);
	}

	private int drawInternal(
		FormattedCharSequence formattedCharSequence,
		float f,
		float g,
		int i,
		boolean bl,
		Matrix4f matrix4f,
		MultiBufferSource multiBufferSource,
		Font.DisplayMode displayMode,
		int j,
		int k
	) {
		i = adjustColor(i);
		Matrix4f matrix4f2 = new Matrix4f(matrix4f);
		if (bl) {
			this.renderText(formattedCharSequence, f, g, i, true, matrix4f, multiBufferSource, displayMode, j, k);
			matrix4f2.translate(SHADOW_OFFSET);
		}

		f = this.renderText(formattedCharSequence, f, g, i, false, matrix4f2, multiBufferSource, displayMode, j, k);
		return (int)f + (bl ? 1 : 0);
	}

	private float renderText(
		String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, Font.DisplayMode displayMode, int j, int k
	) {
		Font.StringRenderOutput stringRenderOutput = new Font.StringRenderOutput(this, multiBufferSource, f, g, i, j, bl, matrix4f, displayMode, k);
		StringDecomposer.iterateFormatted(string, Style.EMPTY, stringRenderOutput);
		return stringRenderOutput.finish(f);
	}

	private float renderText(
		FormattedCharSequence formattedCharSequence,
		float f,
		float g,
		int i,
		boolean bl,
		Matrix4f matrix4f,
		MultiBufferSource multiBufferSource,
		Font.DisplayMode displayMode,
		int j,
		int k
	) {
		Font.StringRenderOutput stringRenderOutput = new Font.StringRenderOutput(this, multiBufferSource, f, g, i, j, bl, matrix4f, displayMode, k);
		formattedCharSequence.accept(stringRenderOutput);
		return stringRenderOutput.finish(f);
	}

	public int width(String string) {
		return Mth.ceil(this.splitter.stringWidth(string));
	}

	public int width(FormattedText formattedText) {
		return Mth.ceil(this.splitter.stringWidth(formattedText));
	}

	public int width(FormattedCharSequence formattedCharSequence) {
		return Mth.ceil(this.splitter.stringWidth(formattedCharSequence));
	}

	public String plainSubstrByWidth(String string, int i, boolean bl) {
		return bl ? this.splitter.plainTailByWidth(string, i, Style.EMPTY) : this.splitter.plainHeadByWidth(string, i, Style.EMPTY);
	}

	public String plainSubstrByWidth(String string, int i) {
		return this.splitter.plainHeadByWidth(string, i, Style.EMPTY);
	}

	public FormattedText substrByWidth(FormattedText formattedText, int i) {
		return this.splitter.headByWidth(formattedText, i, Style.EMPTY);
	}

	public int wordWrapHeight(String string, int i) {
		return 9 * this.splitter.splitLines(string, i, Style.EMPTY).size();
	}

	public int wordWrapHeight(FormattedText formattedText, int i) {
		return 9 * this.splitter.splitLines(formattedText, i, Style.EMPTY).size();
	}

	public List<FormattedCharSequence> split(FormattedText formattedText, int i) {
		return Language.getInstance().getVisualOrder(this.splitter.splitLines(formattedText, i, Style.EMPTY));
	}

	public boolean isBidirectional() {
		return Language.getInstance().isDefaultRightToLeft();
	}

	public StringSplitter getSplitter() {
		return this.splitter;
	}

	@Environment(EnvType.CLIENT)
	public static enum DisplayMode {
		NORMAL,
		SEE_THROUGH,
		POLYGON_OFFSET;
	}

	@Environment(EnvType.CLIENT)
	class StringRenderOutput implements FormattedCharSink {
		final MultiBufferSource bufferSource;
		private final boolean dropShadow;
		private final float dimFactor;
		private final int color;
		private final int backgroundColor;
		private final Matrix4f pose;
		private final Font.DisplayMode mode;
		private final int packedLightCoords;
		float x;
		float y;
		private final List<BakedGlyph.GlyphInstance> glyphInstances;
		@Nullable
		private List<BakedGlyph.Effect> effects;

		private void addEffect(BakedGlyph.Effect effect) {
			if (this.effects == null) {
				this.effects = Lists.<BakedGlyph.Effect>newArrayList();
			}

			this.effects.add(effect);
		}

		public StringRenderOutput(
			final Font font,
			final MultiBufferSource multiBufferSource,
			final float f,
			final float g,
			final int i,
			final boolean bl,
			final Matrix4f matrix4f,
			final Font.DisplayMode displayMode,
			final int j
		) {
			this(font, multiBufferSource, f, g, i, 0, bl, matrix4f, displayMode, j);
		}

		public StringRenderOutput(
			final Font font,
			final MultiBufferSource multiBufferSource,
			final float f,
			final float g,
			final int i,
			final int j,
			final boolean bl,
			final Matrix4f matrix4f,
			final Font.DisplayMode displayMode,
			final int k
		) {
			this.this$0 = font;
			this.glyphInstances = new ArrayList();
			this.bufferSource = multiBufferSource;
			this.x = f;
			this.y = g;
			this.dropShadow = bl;
			this.dimFactor = bl ? 0.25F : 1.0F;
			this.color = ARGB.scaleRGB(i, this.dimFactor);
			this.backgroundColor = j;
			this.pose = matrix4f;
			this.mode = displayMode;
			this.packedLightCoords = k;
		}

		@Override
		public boolean accept(int i, Style style, int j) {
			FontSet fontSet = this.this$0.getFontSet(style.getFont());
			GlyphInfo glyphInfo = fontSet.getGlyphInfo(j, this.this$0.filterFishyGlyphs);
			BakedGlyph bakedGlyph = style.isObfuscated() && j != 32 ? fontSet.getRandomGlyph(glyphInfo) : fontSet.getGlyph(j);
			boolean bl = style.isBold();
			TextColor textColor = style.getColor();
			int k = textColor != null ? ARGB.color(ARGB.alpha(this.color), ARGB.scaleRGB(textColor.getValue(), this.dimFactor)) : this.color;
			float f = glyphInfo.getAdvance(bl);
			float g = i == 0 ? this.x - 1.0F : this.x;
			if (!(bakedGlyph instanceof EmptyGlyph)) {
				float h = bl ? glyphInfo.getBoldOffset() : 0.0F;
				float l = this.dropShadow ? glyphInfo.getShadowOffset() : 0.0F;
				this.glyphInstances.add(new BakedGlyph.GlyphInstance(this.x + l, this.y + l, k, bakedGlyph, style, h));
			}

			float h = this.dropShadow ? 1.0F : 0.0F;
			if (style.isStrikethrough()) {
				this.addEffect(new BakedGlyph.Effect(g + h, this.y + h + 4.5F, this.x + h + f, this.y + h + 4.5F - 1.0F, 0.01F, k));
			}

			if (style.isUnderlined()) {
				this.addEffect(new BakedGlyph.Effect(g + h, this.y + h + 9.0F, this.x + h + f, this.y + h + 9.0F - 1.0F, 0.01F, k));
			}

			this.x += f;
			return true;
		}

		float finish(float f) {
			BakedGlyph bakedGlyph = null;
			if (this.backgroundColor != 0) {
				BakedGlyph.Effect effect = new BakedGlyph.Effect(f - 1.0F, this.y + 9.0F, this.x, this.y - 1.0F, -0.01F, this.backgroundColor);
				bakedGlyph = this.this$0.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
				VertexConsumer vertexConsumer = this.bufferSource.getBuffer(bakedGlyph.renderType(this.mode));
				bakedGlyph.renderEffect(effect, this.pose, vertexConsumer, this.packedLightCoords);
			}

			this.renderCharacters();
			if (this.effects != null) {
				if (bakedGlyph == null) {
					bakedGlyph = this.this$0.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
				}

				VertexConsumer vertexConsumer2 = this.bufferSource.getBuffer(bakedGlyph.renderType(this.mode));

				for (BakedGlyph.Effect effect2 : this.effects) {
					bakedGlyph.renderEffect(effect2, this.pose, vertexConsumer2, this.packedLightCoords);
				}
			}

			return this.x;
		}

		void renderCharacters() {
			for (BakedGlyph.GlyphInstance glyphInstance : this.glyphInstances) {
				BakedGlyph bakedGlyph = glyphInstance.glyph();
				VertexConsumer vertexConsumer = this.bufferSource.getBuffer(bakedGlyph.renderType(this.mode));
				bakedGlyph.renderChar(glyphInstance, this.pose, vertexConsumer, this.packedLightCoords);
			}
		}
	}
}

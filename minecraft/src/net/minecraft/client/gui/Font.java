package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringDecomposer;
import net.minecraft.voting.rules.Rules;
import net.minecraft.voting.rules.actual.CodepointStyleRule;
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

	public int drawShadow(PoseStack poseStack, String string, float f, float g, int i) {
		return this.drawInternal(string, f, g, i, poseStack.last().pose(), true, this.isBidirectional());
	}

	public int drawShadow(PoseStack poseStack, String string, float f, float g, int i, boolean bl) {
		return this.drawInternal(string, f, g, i, poseStack.last().pose(), true, bl);
	}

	public int draw(PoseStack poseStack, String string, float f, float g, int i) {
		return this.drawInternal(string, f, g, i, poseStack.last().pose(), false, this.isBidirectional());
	}

	public int drawShadow(PoseStack poseStack, FormattedCharSequence formattedCharSequence, float f, float g, int i) {
		return this.drawInternal(formattedCharSequence, f, g, i, poseStack.last().pose(), true);
	}

	public int drawShadow(PoseStack poseStack, Component component, float f, float g, int i) {
		return this.drawInternal(component.getVisualOrderText(), f, g, i, poseStack.last().pose(), true);
	}

	public int draw(PoseStack poseStack, FormattedCharSequence formattedCharSequence, float f, float g, int i) {
		return this.drawInternal(formattedCharSequence, f, g, i, poseStack.last().pose(), false);
	}

	public int draw(PoseStack poseStack, Component component, float f, float g, int i) {
		return this.drawInternal(component.getVisualOrderText(), f, g, i, poseStack.last().pose(), false);
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

	private int drawInternal(String string, float f, float g, int i, Matrix4f matrix4f, boolean bl, boolean bl2) {
		if (string == null) {
			return 0;
		} else {
			MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			int j = this.drawInBatch(string, f, g, i, bl, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880, bl2);
			bufferSource.endBatch();
			return j;
		}
	}

	private int drawInternal(FormattedCharSequence formattedCharSequence, float f, float g, int i, Matrix4f matrix4f, boolean bl) {
		MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		int j = this.drawInBatch(formattedCharSequence, f, g, i, bl, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
		bufferSource.endBatch();
		return j;
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
		Font.StringRenderOutput stringRenderOutput = new Font.StringRenderOutput(multiBufferSource, 0.0F, 0.0F, l, false, matrix4f, Font.DisplayMode.NORMAL, k);

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

		Font.StringRenderOutput stringRenderOutput2 = new Font.StringRenderOutput(
			multiBufferSource, f, g, adjustColor(i), false, matrix4f, Font.DisplayMode.POLYGON_OFFSET, k
		);
		formattedCharSequence.accept(stringRenderOutput2);
		stringRenderOutput2.finish(0, f);
	}

	private static int adjustColor(int i) {
		return (i & -67108864) == 0 ? i | 0xFF000000 : i;
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
		Font.StringRenderOutput stringRenderOutput = new Font.StringRenderOutput(multiBufferSource, f, g, i, bl, matrix4f, displayMode, k);
		StringDecomposer.iterateFormatted(string, Style.EMPTY, stringRenderOutput);
		return stringRenderOutput.finish(j, f);
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
		Font.StringRenderOutput stringRenderOutput = new Font.StringRenderOutput(multiBufferSource, f, g, i, bl, matrix4f, displayMode, k);
		formattedCharSequence.accept(stringRenderOutput);
		return stringRenderOutput.finish(j, f);
	}

	void renderChar(
		BakedGlyph bakedGlyph,
		boolean bl,
		boolean bl2,
		boolean bl3,
		float f,
		float g,
		float h,
		Matrix4f matrix4f,
		VertexConsumer vertexConsumer,
		float i,
		float j,
		float k,
		float l,
		int m
	) {
		bakedGlyph.render(bl3, bl2, g, h, matrix4f, vertexConsumer, i, j, k, l, m);
		if (bl) {
			bakedGlyph.render(bl3, bl2, g + f, h, matrix4f, vertexConsumer, i, j, k, l, m);
		}
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

	public void drawWordWrap(PoseStack poseStack, FormattedText formattedText, int i, int j, int k, int l) {
		Matrix4f matrix4f = poseStack.last().pose();

		for (FormattedCharSequence formattedCharSequence : this.split(formattedText, k)) {
			this.drawInternal(formattedCharSequence, (float)i, (float)j, l, matrix4f, false);
			j += 9;
		}
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
		private final float r;
		private final float g;
		private final float b;
		private final float a;
		private final Matrix4f pose;
		private final Font.DisplayMode mode;
		private final int packedLightCoords;
		float x;
		float y;
		@Nullable
		private List<BakedGlyph.Effect> effects;

		private void addEffect(BakedGlyph.Effect effect) {
			if (this.effects == null) {
				this.effects = Lists.<BakedGlyph.Effect>newArrayList();
			}

			this.effects.add(effect);
		}

		public StringRenderOutput(MultiBufferSource multiBufferSource, float f, float g, int i, boolean bl, Matrix4f matrix4f, Font.DisplayMode displayMode, int j) {
			this.bufferSource = multiBufferSource;
			this.x = f;
			this.y = g;
			this.dropShadow = bl;
			this.dimFactor = bl ? 0.25F : 1.0F;
			this.r = (float)(i >> 16 & 0xFF) / 255.0F * this.dimFactor;
			this.g = (float)(i >> 8 & 0xFF) / 255.0F * this.dimFactor;
			this.b = (float)(i & 0xFF) / 255.0F * this.dimFactor;
			this.a = (float)(i >> 24 & 0xFF) / 255.0F;
			this.pose = matrix4f;
			this.mode = displayMode;
			this.packedLightCoords = j;
		}

		@Override
		public boolean accept(int i, Style style, int j) {
			j = Rules.CODEPOINT_REPLACE.getChange(j);
			CodepointStyleRule.CodepointChange codepointChange = Rules.CODEPOINT_STYLE.getChange(j);
			if (codepointChange == CodepointStyleRule.CodepointChange.HIDE) {
				return true;
			} else {
				style = adjustStyle(style, codepointChange);
				FontSet fontSet = Font.this.getFontSet(style.getFont());
				GlyphInfo glyphInfo = fontSet.getGlyphInfo(j, Font.this.filterFishyGlyphs);
				BakedGlyph bakedGlyph = style.isObfuscated() && j != 32 ? fontSet.getRandomGlyph(glyphInfo) : fontSet.getGlyph(j);
				boolean bl = style.isBold();
				float f = this.a;
				TextColor textColor = style.getColor();
				float g;
				float h;
				float l;
				if (textColor != null) {
					int k = textColor.getValue();
					g = (float)(k >> 16 & 0xFF) / 255.0F * this.dimFactor;
					h = (float)(k >> 8 & 0xFF) / 255.0F * this.dimFactor;
					l = (float)(k & 0xFF) / 255.0F * this.dimFactor;
				} else {
					g = this.r;
					h = this.g;
					l = this.b;
				}

				if (codepointChange != CodepointStyleRule.CodepointChange.BLANK && !(bakedGlyph instanceof EmptyGlyph)) {
					float m = bl ? glyphInfo.getBoldOffset() : 0.0F;
					float n = this.dropShadow ? glyphInfo.getShadowOffset() : 0.0F;
					VertexConsumer vertexConsumer = this.bufferSource.getBuffer(bakedGlyph.renderType(this.mode));
					Font.this.renderChar(
						bakedGlyph, bl, style.isItalic(), style.isReversed(), m, this.x + n, this.y + n, this.pose, vertexConsumer, g, h, l, f, this.packedLightCoords
					);
				}

				float m = glyphInfo.getAdvance(bl);
				float n = this.dropShadow ? 1.0F : 0.0F;
				if (style.isStrikethrough()) {
					this.addEffect(new BakedGlyph.Effect(this.x + n - 1.0F, this.y + n + 4.5F, this.x + n + m, this.y + n + 4.5F - 1.0F, 0.01F, g, h, l, f));
				}

				if (style.isUnderlined()) {
					this.addEffect(new BakedGlyph.Effect(this.x + n - 1.0F, this.y + n + 9.0F, this.x + n + m, this.y + n + 9.0F - 1.0F, 0.01F, g, h, l, f));
				}

				this.x += m;
				return true;
			}
		}

		private static Style adjustStyle(Style style, @Nullable CodepointStyleRule.CodepointChange codepointChange) {
			if (codepointChange == null) {
				return style;
			} else {
				return switch (codepointChange) {
					case RED -> style.withColor(ChatFormatting.RED);
					case AQUA -> style.withColor(ChatFormatting.AQUA);
					case BLACK -> style.withColor(ChatFormatting.BLACK);
					case BLUE -> style.withColor(ChatFormatting.BLUE);
					case BOLD -> style.withBold(true);
					case DARK_AQUA -> style.withColor(ChatFormatting.DARK_AQUA);
					case DARK_BLUE -> style.withColor(ChatFormatting.DARK_BLUE);
					case DARK_GRAY -> style.withColor(ChatFormatting.DARK_GRAY);
					case DARK_GREEN -> style.withColor(ChatFormatting.DARK_GREEN);
					case DARK_PURPLE -> style.withColor(ChatFormatting.DARK_PURPLE);
					case DARK_RED -> style.withColor(ChatFormatting.DARK_RED);
					case GOLD -> style.withColor(ChatFormatting.GOLD);
					case GRAY -> style.withColor(ChatFormatting.GRAY);
					case GREEN -> style.withColor(ChatFormatting.GREEN);
					case ILLAGER -> style.withFont(Minecraft.ILLAGERALT_FONT);
					case ITALIC -> style.withItalic(true);
					case LIGHT_PURPLE -> style.withColor(ChatFormatting.LIGHT_PURPLE);
					case SGA -> style.withFont(Minecraft.ALT_FONT);
					case OBFUSCATED -> style.withObfuscated(true);
					case STRIKETHROUGH -> style.withStrikethrough(true);
					case THIN -> style.withFont(Minecraft.UNIFORM_FONT);
					case UNDERLINE -> style.withUnderlined(true);
					case WHITE -> style.withColor(ChatFormatting.WHITE);
					case YELLOW -> style.withColor(ChatFormatting.YELLOW);
					case HIDE, BLANK -> style;
				};
			}
		}

		public float finish(int i, float f) {
			if (i != 0) {
				float g = (float)(i >> 24 & 0xFF) / 255.0F;
				float h = (float)(i >> 16 & 0xFF) / 255.0F;
				float j = (float)(i >> 8 & 0xFF) / 255.0F;
				float k = (float)(i & 0xFF) / 255.0F;
				this.addEffect(new BakedGlyph.Effect(f - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F, 0.01F, h, j, k, g));
			}

			if (this.effects != null) {
				BakedGlyph bakedGlyph = Font.this.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
				VertexConsumer vertexConsumer = this.bufferSource.getBuffer(bakedGlyph.renderType(this.mode));

				for (BakedGlyph.Effect effect : this.effects) {
					bakedGlyph.renderEffect(effect, this.pose, vertexConsumer, this.packedLightCoords);
				}
			}

			return this.x;
		}
	}
}

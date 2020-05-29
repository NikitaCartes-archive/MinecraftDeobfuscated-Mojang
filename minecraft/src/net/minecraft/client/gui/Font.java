package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.StringDecomposer;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class Font {
	private static final Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);
	public final int lineHeight = 9;
	public final Random random = new Random();
	private final Function<ResourceLocation, FontSet> fonts;
	private final StringSplitter splitter;

	public Font(Function<ResourceLocation, FontSet> function) {
		this.fonts = function;
		this.splitter = new StringSplitter((i, style) -> this.getFontSet(style.getFont()).getGlyphInfo(i).getAdvance(style.isBold()));
	}

	private FontSet getFontSet(ResourceLocation resourceLocation) {
		return (FontSet)this.fonts.apply(resourceLocation);
	}

	public int drawShadow(PoseStack poseStack, String string, float f, float g, int i) {
		return this.drawInternal(string, f, g, i, poseStack.last().pose(), true, this.isBidirectional());
	}

	public int drawShadow(PoseStack poseStack, String string, float f, float g, int i, boolean bl) {
		RenderSystem.enableAlphaTest();
		return this.drawInternal(string, f, g, i, poseStack.last().pose(), true, bl);
	}

	public int draw(PoseStack poseStack, String string, float f, float g, int i) {
		RenderSystem.enableAlphaTest();
		return this.drawInternal(string, f, g, i, poseStack.last().pose(), false, this.isBidirectional());
	}

	public int drawShadow(PoseStack poseStack, FormattedText formattedText, float f, float g, int i) {
		RenderSystem.enableAlphaTest();
		return this.drawInternal(formattedText, f, g, i, poseStack.last().pose(), true);
	}

	public int draw(PoseStack poseStack, FormattedText formattedText, float f, float g, int i) {
		RenderSystem.enableAlphaTest();
		return this.drawInternal(formattedText, f, g, i, poseStack.last().pose(), false);
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
			int j = this.drawInBatch(string, f, g, i, bl, matrix4f, bufferSource, false, 0, 15728880, bl2);
			bufferSource.endBatch();
			return j;
		}
	}

	private int drawInternal(FormattedText formattedText, float f, float g, int i, Matrix4f matrix4f, boolean bl) {
		MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		int j = this.drawInBatch(formattedText, f, g, i, bl, matrix4f, bufferSource, false, 0, 15728880);
		bufferSource.endBatch();
		return j;
	}

	public int drawInBatch(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k) {
		return this.drawInBatch(string, f, g, i, bl, matrix4f, multiBufferSource, bl2, j, k, this.isBidirectional());
	}

	public int drawInBatch(
		String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k, boolean bl3
	) {
		return this.drawInternal(string, f, g, i, bl, matrix4f, multiBufferSource, bl2, j, k, bl3);
	}

	public int drawInBatch(
		FormattedText formattedText, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k
	) {
		return this.drawInternal(formattedText, f, g, i, bl, matrix4f, multiBufferSource, bl2, j, k);
	}

	private static int adjustColor(int i) {
		return (i & -67108864) == 0 ? i | 0xFF000000 : i;
	}

	private int drawInternal(
		String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k, boolean bl3
	) {
		if (bl3) {
			string = this.bidirectionalShaping(string);
		}

		i = adjustColor(i);
		Matrix4f matrix4f2 = matrix4f.copy();
		if (bl) {
			this.renderText(string, f, g, i, true, matrix4f, multiBufferSource, bl2, j, k);
			matrix4f2.translate(SHADOW_OFFSET);
		}

		f = this.renderText(string, f, g, i, false, matrix4f2, multiBufferSource, bl2, j, k);
		return (int)f + (bl ? 1 : 0);
	}

	private int drawInternal(
		FormattedText formattedText, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k
	) {
		i = adjustColor(i);
		Matrix4f matrix4f2 = matrix4f.copy();
		if (bl) {
			this.renderText(formattedText, f, g, i, true, matrix4f, multiBufferSource, bl2, j, k);
			matrix4f2.translate(SHADOW_OFFSET);
		}

		f = this.renderText(formattedText, f, g, i, false, matrix4f2, multiBufferSource, bl2, j, k);
		return (int)f + (bl ? 1 : 0);
	}

	private float renderText(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k) {
		Font.StringRenderOutput stringRenderOutput = new Font.StringRenderOutput(multiBufferSource, f, g, i, bl, matrix4f, bl2, k);
		StringDecomposer.iterateFormatted(string, Style.EMPTY, stringRenderOutput);
		return stringRenderOutput.finish(j, f);
	}

	private float renderText(
		FormattedText formattedText, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k
	) {
		Font.StringRenderOutput stringRenderOutput = new Font.StringRenderOutput(multiBufferSource, f, g, i, bl, matrix4f, bl2, k);
		StringDecomposer.iterateFormatted(formattedText, Style.EMPTY, stringRenderOutput);
		return stringRenderOutput.finish(j, f);
	}

	private void renderChar(
		BakedGlyph bakedGlyph,
		boolean bl,
		boolean bl2,
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
		bakedGlyph.render(bl2, g, h, matrix4f, vertexConsumer, i, j, k, l, m);
		if (bl) {
			bakedGlyph.render(bl2, g + f, h, matrix4f, vertexConsumer, i, j, k, l, m);
		}
	}

	public int width(String string) {
		return Mth.ceil(this.splitter.stringWidth(string));
	}

	public int width(FormattedText formattedText) {
		return Mth.ceil(this.splitter.stringWidth(formattedText));
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

	public void drawWordWrap(FormattedText formattedText, int i, int j, int k, int l) {
		Matrix4f matrix4f = Transformation.identity().getMatrix();

		for (FormattedText formattedText2 : this.split(formattedText, k)) {
			this.drawInternal(formattedText2, (float)i, (float)j, l, matrix4f, false);
			j += 9;
		}
	}

	public int wordWrapHeight(String string, int i) {
		return 9 * this.splitter.splitLines(string, i, Style.EMPTY).size();
	}

	public List<FormattedText> split(FormattedText formattedText, int i) {
		return this.splitter.splitLines(formattedText, i, Style.EMPTY);
	}

	public boolean isBidirectional() {
		return Language.getInstance().requiresReordering();
	}

	public StringSplitter getSplitter() {
		return this.splitter;
	}

	@Environment(EnvType.CLIENT)
	class StringRenderOutput implements StringDecomposer.Output {
		final MultiBufferSource bufferSource;
		private final boolean dropShadow;
		private final float dimFactor;
		private final float r;
		private final float g;
		private final float b;
		private final float a;
		private final Matrix4f pose;
		private final boolean seeThrough;
		private final int packedLightCoords;
		private float x;
		private float y;
		@Nullable
		private List<BakedGlyph.Effect> effects;

		private void addEffect(BakedGlyph.Effect effect) {
			if (this.effects == null) {
				this.effects = Lists.<BakedGlyph.Effect>newArrayList();
			}

			this.effects.add(effect);
		}

		public StringRenderOutput(MultiBufferSource multiBufferSource, float f, float g, int i, boolean bl, Matrix4f matrix4f, boolean bl2, int j) {
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
			this.seeThrough = bl2;
			this.packedLightCoords = j;
		}

		@Override
		public boolean onChar(int i, Style style, int j) {
			FontSet fontSet = Font.this.getFontSet(style.getFont());
			GlyphInfo glyphInfo = fontSet.getGlyphInfo(j);
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

			if (!(bakedGlyph instanceof EmptyGlyph)) {
				float m = bl ? glyphInfo.getBoldOffset() : 0.0F;
				float n = this.dropShadow ? glyphInfo.getShadowOffset() : 0.0F;
				VertexConsumer vertexConsumer = this.bufferSource.getBuffer(bakedGlyph.renderType(this.seeThrough));
				Font.this.renderChar(bakedGlyph, bl, style.isItalic(), m, this.x + n, this.y + n, this.pose, vertexConsumer, g, h, l, f, this.packedLightCoords);
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
				VertexConsumer vertexConsumer = this.bufferSource.getBuffer(bakedGlyph.renderType(this.seeThrough));

				for (BakedGlyph.Effect effect : this.effects) {
					bakedGlyph.renderEffect(effect, this.pose, vertexConsumer, this.packedLightCoords);
				}
			}

			return this.x;
		}
	}
}

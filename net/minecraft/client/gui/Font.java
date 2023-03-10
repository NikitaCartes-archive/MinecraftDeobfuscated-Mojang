/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringDecomposer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class Font {
    private static final float EFFECT_DEPTH = 0.01f;
    private static final Vector3f SHADOW_OFFSET = new Vector3f(0.0f, 0.0f, 0.03f);
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
        return this.fonts.apply(resourceLocation);
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
        } catch (ArabicShapingException arabicShapingException) {
            return string;
        }
    }

    private int drawInternal(String string, float f, float g, int i, Matrix4f matrix4f, boolean bl, boolean bl2) {
        if (string == null) {
            return 0;
        }
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        int j = this.drawInBatch(string, f, g, i, bl, matrix4f, bufferSource, DisplayMode.NORMAL, 0, 0xF000F0, bl2);
        bufferSource.endBatch();
        return j;
    }

    private int drawInternal(FormattedCharSequence formattedCharSequence, float f, float g, int i, Matrix4f matrix4f, boolean bl) {
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        int j = this.drawInBatch(formattedCharSequence, f, g, i, bl, matrix4f, (MultiBufferSource)bufferSource, DisplayMode.NORMAL, 0, 0xF000F0);
        bufferSource.endBatch();
        return j;
    }

    public int drawInBatch(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, DisplayMode displayMode, int j, int k) {
        return this.drawInBatch(string, f, g, i, bl, matrix4f, multiBufferSource, displayMode, j, k, this.isBidirectional());
    }

    public int drawInBatch(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, DisplayMode displayMode, int j, int k, boolean bl2) {
        return this.drawInternal(string, f, g, i, bl, matrix4f, multiBufferSource, displayMode, j, k, bl2);
    }

    public int drawInBatch(Component component, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, DisplayMode displayMode, int j, int k) {
        return this.drawInBatch(component.getVisualOrderText(), f, g, i, bl, matrix4f, multiBufferSource, displayMode, j, k);
    }

    public int drawInBatch(FormattedCharSequence formattedCharSequence, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, DisplayMode displayMode, int j, int k) {
        return this.drawInternal(formattedCharSequence, f, g, i, bl, matrix4f, multiBufferSource, displayMode, j, k);
    }

    public void drawInBatch8xOutline(FormattedCharSequence formattedCharSequence, float f, float g, int i, int j, Matrix4f matrix4f, MultiBufferSource multiBufferSource, int k) {
        int l2 = Font.adjustColor(j);
        StringRenderOutput stringRenderOutput = new StringRenderOutput(multiBufferSource, 0.0f, 0.0f, l2, false, matrix4f, DisplayMode.NORMAL, k);
        for (int m2 = -1; m2 <= 1; ++m2) {
            for (int n = -1; n <= 1; ++n) {
                if (m2 == 0 && n == 0) continue;
                float[] fs = new float[]{f};
                int o = m2;
                int p = n;
                formattedCharSequence.accept((l, style, m) -> {
                    boolean bl = style.isBold();
                    FontSet fontSet = this.getFontSet(style.getFont());
                    GlyphInfo glyphInfo = fontSet.getGlyphInfo(m, this.filterFishyGlyphs);
                    stringRenderOutput.x = fs[0] + (float)o * glyphInfo.getShadowOffset();
                    stringRenderOutput.y = g + (float)p * glyphInfo.getShadowOffset();
                    fs[0] = fs[0] + glyphInfo.getAdvance(bl);
                    return stringRenderOutput.accept(l, style.withColor(l2), m);
                });
            }
        }
        StringRenderOutput stringRenderOutput2 = new StringRenderOutput(multiBufferSource, f, g, Font.adjustColor(i), false, matrix4f, DisplayMode.POLYGON_OFFSET, k);
        formattedCharSequence.accept(stringRenderOutput2);
        stringRenderOutput2.finish(0, f);
    }

    private static int adjustColor(int i) {
        if ((i & 0xFC000000) == 0) {
            return i | 0xFF000000;
        }
        return i;
    }

    private int drawInternal(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, DisplayMode displayMode, int j, int k, boolean bl2) {
        if (bl2) {
            string = this.bidirectionalShaping(string);
        }
        i = Font.adjustColor(i);
        Matrix4f matrix4f2 = new Matrix4f(matrix4f);
        if (bl) {
            this.renderText(string, f, g, i, true, matrix4f, multiBufferSource, displayMode, j, k);
            matrix4f2.translate(SHADOW_OFFSET);
        }
        f = this.renderText(string, f, g, i, false, matrix4f2, multiBufferSource, displayMode, j, k);
        return (int)f + (bl ? 1 : 0);
    }

    private int drawInternal(FormattedCharSequence formattedCharSequence, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, DisplayMode displayMode, int j, int k) {
        i = Font.adjustColor(i);
        Matrix4f matrix4f2 = new Matrix4f(matrix4f);
        if (bl) {
            this.renderText(formattedCharSequence, f, g, i, true, matrix4f, multiBufferSource, displayMode, j, k);
            matrix4f2.translate(SHADOW_OFFSET);
        }
        f = this.renderText(formattedCharSequence, f, g, i, false, matrix4f2, multiBufferSource, displayMode, j, k);
        return (int)f + (bl ? 1 : 0);
    }

    private float renderText(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, DisplayMode displayMode, int j, int k) {
        StringRenderOutput stringRenderOutput = new StringRenderOutput(multiBufferSource, f, g, i, bl, matrix4f, displayMode, k);
        StringDecomposer.iterateFormatted(string, Style.EMPTY, (FormattedCharSink)stringRenderOutput);
        return stringRenderOutput.finish(j, f);
    }

    private float renderText(FormattedCharSequence formattedCharSequence, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, DisplayMode displayMode, int j, int k) {
        StringRenderOutput stringRenderOutput = new StringRenderOutput(multiBufferSource, f, g, i, bl, matrix4f, displayMode, k);
        formattedCharSequence.accept(stringRenderOutput);
        return stringRenderOutput.finish(j, f);
    }

    void renderChar(BakedGlyph bakedGlyph, boolean bl, boolean bl2, float f, float g, float h, Matrix4f matrix4f, VertexConsumer vertexConsumer, float i, float j, float k, float l, int m) {
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
            this.drawInternal(formattedCharSequence, i, j, l, matrix4f, false);
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

    @Environment(value=EnvType.CLIENT)
    public static enum DisplayMode {
        NORMAL,
        SEE_THROUGH,
        POLYGON_OFFSET;

    }

    @Environment(value=EnvType.CLIENT)
    class StringRenderOutput
    implements FormattedCharSink {
        final MultiBufferSource bufferSource;
        private final boolean dropShadow;
        private final float dimFactor;
        private final float r;
        private final float g;
        private final float b;
        private final float a;
        private final Matrix4f pose;
        private final DisplayMode mode;
        private final int packedLightCoords;
        float x;
        float y;
        @Nullable
        private List<BakedGlyph.Effect> effects;

        private void addEffect(BakedGlyph.Effect effect) {
            if (this.effects == null) {
                this.effects = Lists.newArrayList();
            }
            this.effects.add(effect);
        }

        public StringRenderOutput(MultiBufferSource multiBufferSource, float f, float g, int i, boolean bl, Matrix4f matrix4f, DisplayMode displayMode, int j) {
            this.bufferSource = multiBufferSource;
            this.x = f;
            this.y = g;
            this.dropShadow = bl;
            this.dimFactor = bl ? 0.25f : 1.0f;
            this.r = (float)(i >> 16 & 0xFF) / 255.0f * this.dimFactor;
            this.g = (float)(i >> 8 & 0xFF) / 255.0f * this.dimFactor;
            this.b = (float)(i & 0xFF) / 255.0f * this.dimFactor;
            this.a = (float)(i >> 24 & 0xFF) / 255.0f;
            this.pose = matrix4f;
            this.mode = displayMode;
            this.packedLightCoords = j;
        }

        @Override
        public boolean accept(int i, Style style, int j) {
            float n;
            float l;
            float h;
            float g;
            FontSet fontSet = Font.this.getFontSet(style.getFont());
            GlyphInfo glyphInfo = fontSet.getGlyphInfo(j, Font.this.filterFishyGlyphs);
            BakedGlyph bakedGlyph = style.isObfuscated() && j != 32 ? fontSet.getRandomGlyph(glyphInfo) : fontSet.getGlyph(j);
            boolean bl = style.isBold();
            float f = this.a;
            TextColor textColor = style.getColor();
            if (textColor != null) {
                int k = textColor.getValue();
                g = (float)(k >> 16 & 0xFF) / 255.0f * this.dimFactor;
                h = (float)(k >> 8 & 0xFF) / 255.0f * this.dimFactor;
                l = (float)(k & 0xFF) / 255.0f * this.dimFactor;
            } else {
                g = this.r;
                h = this.g;
                l = this.b;
            }
            if (!(bakedGlyph instanceof EmptyGlyph)) {
                float m = bl ? glyphInfo.getBoldOffset() : 0.0f;
                n = this.dropShadow ? glyphInfo.getShadowOffset() : 0.0f;
                VertexConsumer vertexConsumer = this.bufferSource.getBuffer(bakedGlyph.renderType(this.mode));
                Font.this.renderChar(bakedGlyph, bl, style.isItalic(), m, this.x + n, this.y + n, this.pose, vertexConsumer, g, h, l, f, this.packedLightCoords);
            }
            float m = glyphInfo.getAdvance(bl);
            float f2 = n = this.dropShadow ? 1.0f : 0.0f;
            if (style.isStrikethrough()) {
                this.addEffect(new BakedGlyph.Effect(this.x + n - 1.0f, this.y + n + 4.5f, this.x + n + m, this.y + n + 4.5f - 1.0f, 0.01f, g, h, l, f));
            }
            if (style.isUnderlined()) {
                this.addEffect(new BakedGlyph.Effect(this.x + n - 1.0f, this.y + n + 9.0f, this.x + n + m, this.y + n + 9.0f - 1.0f, 0.01f, g, h, l, f));
            }
            this.x += m;
            return true;
        }

        public float finish(int i, float f) {
            if (i != 0) {
                float g = (float)(i >> 24 & 0xFF) / 255.0f;
                float h = (float)(i >> 16 & 0xFF) / 255.0f;
                float j = (float)(i >> 8 & 0xFF) / 255.0f;
                float k = (float)(i & 0xFF) / 255.0f;
                this.addEffect(new BakedGlyph.Effect(f - 1.0f, this.y + 9.0f, this.x + 1.0f, this.y - 1.0f, 0.01f, h, j, k, g));
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


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.StringDecomposer;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class Font {
    private static final Vector3f SHADOW_OFFSET = new Vector3f(0.0f, 0.0f, 0.03f);
    public final int lineHeight = 9;
    public final Random random = new Random();
    private final Function<ResourceLocation, FontSet> fonts;
    private boolean bidirectional;
    private final StringSplitter splitter;

    public Font(Function<ResourceLocation, FontSet> function) {
        this.fonts = function;
        this.splitter = new StringSplitter((i, style) -> this.getFontSet(style.getFont()).getGlyphInfo(i).getAdvance(style.isBold()));
    }

    private FontSet getFontSet(ResourceLocation resourceLocation) {
        return this.fonts.apply(resourceLocation);
    }

    public int drawShadow(PoseStack poseStack, String string, float f, float g, int i) {
        return this.drawInternal(string, f, g, i, poseStack.last().pose(), true, this.bidirectional);
    }

    public int draw(PoseStack poseStack, String string, float f, float g, int i) {
        RenderSystem.enableAlphaTest();
        return this.drawInternal(string, f, g, i, poseStack.last().pose(), false, this.bidirectional);
    }

    public int drawShadow(PoseStack poseStack, Component component, float f, float g, int i) {
        RenderSystem.enableAlphaTest();
        return this.drawInternal(component, f, g, i, poseStack.last().pose(), true);
    }

    public int draw(PoseStack poseStack, Component component, float f, float g, int i) {
        RenderSystem.enableAlphaTest();
        return this.drawInternal(component, f, g, i, poseStack.last().pose(), false);
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
        int j = this.drawInBatch(string, f, g, i, bl, matrix4f, bufferSource, false, 0, 0xF000F0, bl2);
        bufferSource.endBatch();
        return j;
    }

    private int drawInternal(Component component, float f, float g, int i, Matrix4f matrix4f, boolean bl) {
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        int j = this.drawInBatch(component, f, g, i, bl, matrix4f, (MultiBufferSource)bufferSource, false, 0, 0xF000F0);
        bufferSource.endBatch();
        return j;
    }

    public int drawInBatch(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k) {
        return this.drawInBatch(string, f, g, i, bl, matrix4f, multiBufferSource, bl2, j, k, this.bidirectional);
    }

    public int drawInBatch(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k, boolean bl3) {
        return this.drawInternal(string, f, g, i, bl, matrix4f, multiBufferSource, bl2, j, k, bl3);
    }

    public int drawInBatch(Component component, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k) {
        return this.drawInternal(component, f, g, i, bl, matrix4f, multiBufferSource, bl2, j, k);
    }

    private static int adjustColor(int i) {
        if ((i & 0xFC000000) == 0) {
            return i | 0xFF000000;
        }
        return i;
    }

    private int drawInternal(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k, boolean bl3) {
        if (bl3) {
            string = this.bidirectionalShaping(string);
        }
        i = Font.adjustColor(i);
        if (bl) {
            this.renderText(string, f, g, i, true, matrix4f, multiBufferSource, bl2, j, k);
        }
        Matrix4f matrix4f2 = matrix4f.copy();
        matrix4f2.translate(SHADOW_OFFSET);
        f = this.renderText(string, f, g, i, false, matrix4f2, multiBufferSource, bl2, j, k);
        return (int)f + (bl ? 1 : 0);
    }

    private int drawInternal(Component component, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k) {
        i = Font.adjustColor(i);
        if (bl) {
            this.renderText(component, f, g, i, true, matrix4f, multiBufferSource, bl2, j, k);
        }
        Matrix4f matrix4f2 = matrix4f.copy();
        matrix4f2.translate(SHADOW_OFFSET);
        f = this.renderText(component, f, g, i, false, matrix4f2, multiBufferSource, bl2, j, k);
        return (int)f + (bl ? 1 : 0);
    }

    private float renderText(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k) {
        StringRenderOutput stringRenderOutput = new StringRenderOutput(multiBufferSource, f, g, i, bl, matrix4f, bl2, k);
        StringDecomposer.iterateFormatted(string, Style.EMPTY, (StringDecomposer.Output)stringRenderOutput);
        return stringRenderOutput.finish(j, f);
    }

    private float renderText(Component component, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k) {
        StringRenderOutput stringRenderOutput = new StringRenderOutput(multiBufferSource, f, g, i, bl, matrix4f, bl2, k);
        StringDecomposer.iterateFormatted(component, Style.EMPTY, (StringDecomposer.Output)stringRenderOutput);
        return stringRenderOutput.finish(j, f);
    }

    private void renderChar(BakedGlyph bakedGlyph, boolean bl, boolean bl2, float f, float g, float h, Matrix4f matrix4f, VertexConsumer vertexConsumer, float i, float j, float k, float l, int m) {
        bakedGlyph.render(bl2, g, h, matrix4f, vertexConsumer, i, j, k, l, m);
        if (bl) {
            bakedGlyph.render(bl2, g + f, h, matrix4f, vertexConsumer, i, j, k, l, m);
        }
    }

    public int width(String string) {
        return Mth.ceil(this.splitter.stringWidth(string));
    }

    public int width(Component component) {
        return Mth.ceil(this.splitter.stringWidth(component));
    }

    public String plainSubstrByWidth(String string, int i, boolean bl) {
        return bl ? this.splitter.plainTailByWidth(string, i, Style.EMPTY) : this.splitter.plainHeadByWidth(string, i, Style.EMPTY);
    }

    public String plainSubstrByWidth(String string, int i) {
        return this.splitter.plainHeadByWidth(string, i, Style.EMPTY);
    }

    public MutableComponent substrByWidth(Component component, int i) {
        return this.splitter.headByWidth(component, i, Style.EMPTY);
    }

    public void drawWordWrap(Component component, int i, int j, int k, int l) {
        Matrix4f matrix4f = Transformation.identity().getMatrix();
        for (Component component2 : this.split(component, k)) {
            this.drawInternal(component2, i, j, l, matrix4f, false);
            j += 9;
        }
    }

    public int wordWrapHeight(String string, int i) {
        return 9 * this.splitter.splitLines(string, i, Style.EMPTY).size();
    }

    public void setBidirectional(boolean bl) {
        this.bidirectional = bl;
    }

    public List<Component> split(Component component, int i) {
        return this.splitter.splitLines(component, i, Style.EMPTY);
    }

    public boolean isBidirectional() {
        return this.bidirectional;
    }

    public StringSplitter getSplitter() {
        return this.splitter;
    }

    @Environment(value=EnvType.CLIENT)
    class StringRenderOutput
    implements StringDecomposer.Output {
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
                this.effects = Lists.newArrayList();
            }
            this.effects.add(effect);
        }

        public StringRenderOutput(MultiBufferSource multiBufferSource, float f, float g, int i, boolean bl, Matrix4f matrix4f, boolean bl2, int j) {
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
            this.seeThrough = bl2;
            this.packedLightCoords = j;
        }

        @Override
        public boolean onChar(int i, Style style, int j) {
            float n;
            float l;
            float h;
            float g;
            FontSet fontSet = Font.this.getFontSet(style.getFont());
            GlyphInfo glyphInfo = fontSet.getGlyphInfo(j);
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
                VertexConsumer vertexConsumer = this.bufferSource.getBuffer(bakedGlyph.renderType(this.seeThrough));
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
                this.addEffect(new BakedGlyph.Effect(f - 1.0f, this.y + 9.0f, this.x + 1.0f, this.y - 1.0f, -0.01f, h, j, k, g));
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


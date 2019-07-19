/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

@Environment(value=EnvType.CLIENT)
public abstract class GuiComponent {
    public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/options_background.png");
    public static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    protected int blitOffset;

    protected void hLine(int i, int j, int k, int l) {
        if (j < i) {
            int m = i;
            i = j;
            j = m;
        }
        GuiComponent.fill(i, k, j + 1, k + 1, l);
    }

    protected void vLine(int i, int j, int k, int l) {
        if (k < j) {
            int m = j;
            j = k;
            k = m;
        }
        GuiComponent.fill(i, j + 1, i + 1, k, l);
    }

    public static void fill(int i, int j, int k, int l, int m) {
        int n;
        if (i < k) {
            n = i;
            i = k;
            k = n;
        }
        if (j < l) {
            n = j;
            j = l;
            l = n;
        }
        float f = (float)(m >> 24 & 0xFF) / 255.0f;
        float g = (float)(m >> 16 & 0xFF) / 255.0f;
        float h = (float)(m >> 8 & 0xFF) / 255.0f;
        float o = (float)(m & 0xFF) / 255.0f;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color4f(g, h, o, f);
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(i, l, 0.0).endVertex();
        bufferBuilder.vertex(k, l, 0.0).endVertex();
        bufferBuilder.vertex(k, j, 0.0).endVertex();
        bufferBuilder.vertex(i, j, 0.0).endVertex();
        tesselator.end();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    protected void fillGradient(int i, int j, int k, int l, int m, int n) {
        float f = (float)(m >> 24 & 0xFF) / 255.0f;
        float g = (float)(m >> 16 & 0xFF) / 255.0f;
        float h = (float)(m >> 8 & 0xFF) / 255.0f;
        float o = (float)(m & 0xFF) / 255.0f;
        float p = (float)(n >> 24 & 0xFF) / 255.0f;
        float q = (float)(n >> 16 & 0xFF) / 255.0f;
        float r = (float)(n >> 8 & 0xFF) / 255.0f;
        float s = (float)(n & 0xFF) / 255.0f;
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(k, j, this.blitOffset).color(g, h, o, f).endVertex();
        bufferBuilder.vertex(i, j, this.blitOffset).color(g, h, o, f).endVertex();
        bufferBuilder.vertex(i, l, this.blitOffset).color(q, r, s, p).endVertex();
        bufferBuilder.vertex(k, l, this.blitOffset).color(q, r, s, p).endVertex();
        tesselator.end();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableTexture();
    }

    public void drawCenteredString(Font font, String string, int i, int j, int k) {
        font.drawShadow(string, i - font.width(string) / 2, j, k);
    }

    public void drawRightAlignedString(Font font, String string, int i, int j, int k) {
        font.drawShadow(string, i - font.width(string), j, k);
    }

    public void drawString(Font font, String string, int i, int j, int k) {
        font.drawShadow(string, i, j, k);
    }

    public static void blit(int i, int j, int k, int l, int m, TextureAtlasSprite textureAtlasSprite) {
        GuiComponent.innerBlit(i, i + l, j, j + m, k, textureAtlasSprite.getU0(), textureAtlasSprite.getU1(), textureAtlasSprite.getV0(), textureAtlasSprite.getV1());
    }

    public void blit(int i, int j, int k, int l, int m, int n) {
        GuiComponent.blit(i, j, this.blitOffset, k, l, m, n, 256, 256);
    }

    public static void blit(int i, int j, int k, float f, float g, int l, int m, int n, int o) {
        GuiComponent.innerBlit(i, i + l, j, j + m, k, l, m, f, g, o, n);
    }

    public static void blit(int i, int j, int k, int l, float f, float g, int m, int n, int o, int p) {
        GuiComponent.innerBlit(i, i + k, j, j + l, 0, m, n, f, g, o, p);
    }

    public static void blit(int i, int j, float f, float g, int k, int l, int m, int n) {
        GuiComponent.blit(i, j, k, l, f, g, k, l, m, n);
    }

    private static void innerBlit(int i, int j, int k, int l, int m, int n, int o, float f, float g, int p, int q) {
        GuiComponent.innerBlit(i, j, k, l, m, (f + 0.0f) / (float)p, (f + (float)n) / (float)p, (g + 0.0f) / (float)q, (g + (float)o) / (float)q);
    }

    protected static void innerBlit(int i, int j, int k, int l, int m, float f, float g, float h, float n) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(i, l, m).uv(f, n).endVertex();
        bufferBuilder.vertex(j, l, m).uv(g, n).endVertex();
        bufferBuilder.vertex(j, k, m).uv(g, h).endVertex();
        bufferBuilder.vertex(i, k, m).uv(f, h).endVertex();
        tesselator.end();
    }
}


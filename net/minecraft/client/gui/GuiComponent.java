/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public abstract class GuiComponent {
    public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/options_background.png");
    public static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    private int blitOffset;

    protected void hLine(PoseStack poseStack, int i, int j, int k, int l) {
        if (j < i) {
            int m = i;
            i = j;
            j = m;
        }
        GuiComponent.fill(poseStack, i, k, j + 1, k + 1, l);
    }

    protected void vLine(PoseStack poseStack, int i, int j, int k, int l) {
        if (k < j) {
            int m = j;
            j = k;
            k = m;
        }
        GuiComponent.fill(poseStack, i, j + 1, i + 1, k, l);
    }

    public static void enableScissor(int i, int j, int k, int l) {
        Window window = Minecraft.getInstance().getWindow();
        int m = window.getHeight();
        double d = window.getGuiScale();
        double e = (double)i * d;
        double f = (double)m - (double)l * d;
        double g = (double)(k - i) * d;
        double h = (double)(l - j) * d;
        RenderSystem.enableScissor((int)e, (int)f, Math.max(0, (int)g), Math.max(0, (int)h));
    }

    public static void disableScissor() {
        RenderSystem.disableScissor();
    }

    public static void fill(PoseStack poseStack, int i, int j, int k, int l, int m) {
        GuiComponent.fill(poseStack, i, j, k, l, 0, m);
    }

    public static void fill(PoseStack poseStack, int i, int j, int k, int l, int m, int n) {
        GuiComponent.innerFill(poseStack.last().pose(), i, j, k, l, m, n);
    }

    private static void innerFill(Matrix4f matrix4f, int i, int j, int k, int l, int m, int n) {
        int o;
        if (i < k) {
            o = i;
            i = k;
            k = o;
        }
        if (j < l) {
            o = j;
            j = l;
            l = o;
        }
        float f = (float)(n >> 24 & 0xFF) / 255.0f;
        float g = (float)(n >> 16 & 0xFF) / 255.0f;
        float h = (float)(n >> 8 & 0xFF) / 255.0f;
        float p = (float)(n & 0xFF) / 255.0f;
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, i, l, m).color(g, h, p, f).endVertex();
        bufferBuilder.vertex(matrix4f, k, l, m).color(g, h, p, f).endVertex();
        bufferBuilder.vertex(matrix4f, k, j, m).color(g, h, p, f).endVertex();
        bufferBuilder.vertex(matrix4f, i, j, m).color(g, h, p, f).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    protected void fillGradient(PoseStack poseStack, int i, int j, int k, int l, int m, int n) {
        GuiComponent.fillGradient(poseStack, i, j, k, l, m, n, this.blitOffset);
    }

    protected static void fillGradient(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        GuiComponent.fillGradient(poseStack.last().pose(), bufferBuilder, i, j, k, l, o, m, n);
        tesselator.end();
        RenderSystem.disableBlend();
    }

    protected static void fillGradient(Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n, int o) {
        float f = (float)(n >> 24 & 0xFF) / 255.0f;
        float g = (float)(n >> 16 & 0xFF) / 255.0f;
        float h = (float)(n >> 8 & 0xFF) / 255.0f;
        float p = (float)(n & 0xFF) / 255.0f;
        float q = (float)(o >> 24 & 0xFF) / 255.0f;
        float r = (float)(o >> 16 & 0xFF) / 255.0f;
        float s = (float)(o >> 8 & 0xFF) / 255.0f;
        float t = (float)(o & 0xFF) / 255.0f;
        bufferBuilder.vertex(matrix4f, k, j, m).color(g, h, p, f).endVertex();
        bufferBuilder.vertex(matrix4f, i, j, m).color(g, h, p, f).endVertex();
        bufferBuilder.vertex(matrix4f, i, l, m).color(r, s, t, q).endVertex();
        bufferBuilder.vertex(matrix4f, k, l, m).color(r, s, t, q).endVertex();
    }

    public static void drawCenteredString(PoseStack poseStack, Font font, String string, int i, int j, int k) {
        font.drawShadow(poseStack, string, (float)(i - font.width(string) / 2), (float)j, k);
    }

    public static void drawCenteredString(PoseStack poseStack, Font font, Component component, int i, int j, int k) {
        FormattedCharSequence formattedCharSequence = component.getVisualOrderText();
        font.drawShadow(poseStack, formattedCharSequence, (float)(i - font.width(formattedCharSequence) / 2), (float)j, k);
    }

    public static void drawCenteredString(PoseStack poseStack, Font font, FormattedCharSequence formattedCharSequence, int i, int j, int k) {
        font.drawShadow(poseStack, formattedCharSequence, (float)(i - font.width(formattedCharSequence) / 2), (float)j, k);
    }

    public static void drawString(PoseStack poseStack, Font font, String string, int i, int j, int k) {
        font.drawShadow(poseStack, string, (float)i, (float)j, k);
    }

    public static void drawString(PoseStack poseStack, Font font, FormattedCharSequence formattedCharSequence, int i, int j, int k) {
        font.drawShadow(poseStack, formattedCharSequence, (float)i, (float)j, k);
    }

    public static void drawString(PoseStack poseStack, Font font, Component component, int i, int j, int k) {
        font.drawShadow(poseStack, component, (float)i, (float)j, k);
    }

    public void blitOutlineBlack(int i, int j, BiConsumer<Integer, Integer> biConsumer) {
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        biConsumer.accept(i + 1, j);
        biConsumer.accept(i - 1, j);
        biConsumer.accept(i, j + 1);
        biConsumer.accept(i, j - 1);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        biConsumer.accept(i, j);
    }

    public static void blit(PoseStack poseStack, int i, int j, int k, int l, int m, TextureAtlasSprite textureAtlasSprite) {
        GuiComponent.innerBlit(poseStack.last().pose(), i, i + l, j, j + m, k, textureAtlasSprite.getU0(), textureAtlasSprite.getU1(), textureAtlasSprite.getV0(), textureAtlasSprite.getV1());
    }

    public static void blit(PoseStack poseStack, int i, int j, int k, int l, int m, TextureAtlasSprite textureAtlasSprite, float f, float g, float h, float n) {
        GuiComponent.innerBlit(poseStack.last().pose(), i, i + l, j, j + m, k, textureAtlasSprite.getU0(), textureAtlasSprite.getU1(), textureAtlasSprite.getV0(), textureAtlasSprite.getV1(), f, g, h, n);
    }

    public void blit(PoseStack poseStack, int i, int j, int k, int l, int m, int n) {
        GuiComponent.blit(poseStack, i, j, this.blitOffset, k, l, m, n, 256, 256);
    }

    public static void blit(PoseStack poseStack, int i, int j, int k, float f, float g, int l, int m, int n, int o) {
        GuiComponent.innerBlit(poseStack, i, i + l, j, j + m, k, l, m, f, g, n, o);
    }

    public static void blit(PoseStack poseStack, int i, int j, int k, int l, float f, float g, int m, int n, int o, int p) {
        GuiComponent.innerBlit(poseStack, i, i + k, j, j + l, 0, m, n, f, g, o, p);
    }

    public static void blit(PoseStack poseStack, int i, int j, float f, float g, int k, int l, int m, int n) {
        GuiComponent.blit(poseStack, i, j, k, l, f, g, k, l, m, n);
    }

    private static void innerBlit(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, float f, float g, int p, int q) {
        GuiComponent.innerBlit(poseStack.last().pose(), i, j, k, l, m, (f + 0.0f) / (float)p, (f + (float)n) / (float)p, (g + 0.0f) / (float)q, (g + (float)o) / (float)q);
    }

    private static void innerBlit(Matrix4f matrix4f, int i, int j, int k, int l, int m, float f, float g, float h, float n) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix4f, i, l, m).uv(f, n).endVertex();
        bufferBuilder.vertex(matrix4f, j, l, m).uv(g, n).endVertex();
        bufferBuilder.vertex(matrix4f, j, k, m).uv(g, h).endVertex();
        bufferBuilder.vertex(matrix4f, i, k, m).uv(f, h).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private static void innerBlit(Matrix4f matrix4f, int i, int j, int k, int l, int m, float f, float g, float h, float n, float o, float p, float q, float r) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferBuilder.vertex(matrix4f, i, l, m).color(o, p, q, r).uv(f, n).endVertex();
        bufferBuilder.vertex(matrix4f, j, l, m).color(o, p, q, r).uv(g, n).endVertex();
        bufferBuilder.vertex(matrix4f, j, k, m).color(o, p, q, r).uv(g, h).endVertex();
        bufferBuilder.vertex(matrix4f, i, k, m).color(o, p, q, r).uv(f, h).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public int getBlitOffset() {
        return this.blitOffset;
    }

    public void setBlitOffset(int i) {
        this.blitOffset = i;
    }

    public void blitNineSliced(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, int p, int q) {
        this.blitNineSliced(poseStack, i, j, k, l, m, m, m, m, n, o, p, q);
    }

    public void blitNineSliced(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, int p, int q, int r, int s, int t) {
        if (k == q && l == r) {
            this.blit(poseStack, i, j, s, t, k, l);
            return;
        }
        if (l == r) {
            this.blit(poseStack, i, j, s, t, m, l);
            this.blitRepeating(poseStack, i + m, j, k - o - m, l, s + m, t, q - o - m, r);
            this.blit(poseStack, i + k - o, j, s + q - o, t, o, l);
            return;
        }
        if (k == q) {
            this.blit(poseStack, i, j, s, t, k, n);
            this.blitRepeating(poseStack, i, j + n, k, l - p - n, s, t + n, q, r - p - n);
            this.blit(poseStack, i, j + l - p, s, t + r - p, k, p);
            return;
        }
        this.blit(poseStack, i, j, s, t, m, n);
        this.blitRepeating(poseStack, i + m, j, k - o - m, n, s + m, t, q - o - m, r);
        this.blit(poseStack, i + k - o, j, s + q - o, t, o, n);
        this.blit(poseStack, i, j + l - p, s, t + r - p, m, p);
        this.blitRepeating(poseStack, i + m, j + l - p, k - o - m, p, s + m, t + r - p, q - o - m, r);
        this.blit(poseStack, i + k - o, j + l - p, s + q - o, t + r - p, o, p);
        this.blitRepeating(poseStack, i, j + n, m, l - p - n, s, t + n, q, r - p - n);
        this.blitRepeating(poseStack, i + m, j + n, k - o - m, l - p - n, s + m, t + n, q - o - m, r - p - n);
        this.blitRepeating(poseStack, i + k - o, j + n, m, l - p - n, s + q - o, t + n, q, r - p - n);
    }

    public void blitRepeating(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, int p) {
        for (int q = 0; q < k; q += o) {
            int r = i + q;
            int s = Math.min(o, k - q);
            for (int t = 0; t < l; t += p) {
                int u = j + t;
                int v = Math.min(p, l - t);
                this.blit(poseStack, r, u, m, n, s, v);
            }
        }
    }
}


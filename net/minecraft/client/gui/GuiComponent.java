/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

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

    public static void fill(PoseStack poseStack, int i, int j, int k, int l, int m) {
        GuiComponent.innerFill(poseStack.last().pose(), i, j, k, l, m);
    }

    private static void innerFill(Matrix4f matrix4f, int i, int j, int k, int l, int m) {
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
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, i, l, 0.0f).color(g, h, o, f).endVertex();
        bufferBuilder.vertex(matrix4f, k, l, 0.0f).color(g, h, o, f).endVertex();
        bufferBuilder.vertex(matrix4f, k, j, 0.0f).color(g, h, o, f).endVertex();
        bufferBuilder.vertex(matrix4f, i, j, 0.0f).color(g, h, o, f).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    protected void fillGradient(PoseStack poseStack, int i, int j, int k, int l, int m, int n) {
        this.fillGradient(poseStack.last().pose(), i, j, k, l, m, n);
    }

    private void fillGradient(Matrix4f matrix4f, int i, int j, int k, int l, int m, int n) {
        float f = (float)(m >> 24 & 0xFF) / 255.0f;
        float g = (float)(m >> 16 & 0xFF) / 255.0f;
        float h = (float)(m >> 8 & 0xFF) / 255.0f;
        float o = (float)(m & 0xFF) / 255.0f;
        float p = (float)(n >> 24 & 0xFF) / 255.0f;
        float q = (float)(n >> 16 & 0xFF) / 255.0f;
        float r = (float)(n >> 8 & 0xFF) / 255.0f;
        float s = (float)(n & 0xFF) / 255.0f;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, k, j, this.blitOffset).color(g, h, o, f).endVertex();
        bufferBuilder.vertex(matrix4f, i, j, this.blitOffset).color(g, h, o, f).endVertex();
        bufferBuilder.vertex(matrix4f, i, l, this.blitOffset).color(q, r, s, p).endVertex();
        bufferBuilder.vertex(matrix4f, k, l, this.blitOffset).color(q, r, s, p).endVertex();
        tesselator.end();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public void drawCenteredString(PoseStack poseStack, Font font, String string, int i, int j, int k) {
        font.drawShadow(poseStack, string, (float)(i - font.width(string) / 2), (float)j, k);
    }

    public void drawCenteredString(PoseStack poseStack, Font font, Component component, int i, int j, int k) {
        font.drawShadow(poseStack, component, (float)(i - font.width(component) / 2), (float)j, k);
    }

    public void drawString(PoseStack poseStack, Font font, String string, int i, int j, int k) {
        font.drawShadow(poseStack, string, (float)i, (float)j, k);
    }

    public void drawString(PoseStack poseStack, Font font, Component component, int i, int j, int k) {
        font.drawShadow(poseStack, component, (float)i, (float)j, k);
    }

    public static void blit(PoseStack poseStack, int i, int j, int k, int l, int m, TextureAtlasSprite textureAtlasSprite) {
        GuiComponent.innerBlit(poseStack.last().pose(), i, i + l, j, j + m, k, textureAtlasSprite.getU0(), textureAtlasSprite.getU1(), textureAtlasSprite.getV0(), textureAtlasSprite.getV1());
    }

    public void blit(PoseStack poseStack, int i, int j, int k, int l, int m, int n) {
        GuiComponent.blit(poseStack, i, j, this.blitOffset, k, l, m, n, 256, 256);
    }

    public static void blit(PoseStack poseStack, int i, int j, int k, float f, float g, int l, int m, int n, int o) {
        GuiComponent.innerBlit(poseStack, i, i + l, j, j + m, k, l, m, f, g, o, n);
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
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix4f, i, l, m).uv(f, n).endVertex();
        bufferBuilder.vertex(matrix4f, j, l, m).uv(g, n).endVertex();
        bufferBuilder.vertex(matrix4f, j, k, m).uv(g, h).endVertex();
        bufferBuilder.vertex(matrix4f, i, k, m).uv(f, h).endVertex();
        bufferBuilder.end();
        RenderSystem.enableAlphaTest();
        BufferUploader.end(bufferBuilder);
    }

    public int getBlitOffset() {
        return this.blitOffset;
    }

    public void setBlitOffset(int i) {
        this.blitOffset = i;
    }
}


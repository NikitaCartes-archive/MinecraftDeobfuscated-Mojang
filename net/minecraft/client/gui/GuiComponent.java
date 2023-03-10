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
import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public abstract class GuiComponent {
    public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/options_background.png");
    public static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    public static final ResourceLocation LIGHT_DIRT_BACKGROUND = new ResourceLocation("textures/gui/light_dirt_background.png");
    private static final ScissorStack SCISSOR_STACK = new ScissorStack();

    protected static void hLine(PoseStack poseStack, int i, int j, int k, int l) {
        if (j < i) {
            int m = i;
            i = j;
            j = m;
        }
        GuiComponent.fill(poseStack, i, k, j + 1, k + 1, l);
    }

    protected static void vLine(PoseStack poseStack, int i, int j, int k, int l) {
        if (k < j) {
            int m = j;
            j = k;
            k = m;
        }
        GuiComponent.fill(poseStack, i, j + 1, i + 1, k, l);
    }

    public static void enableScissor(int i, int j, int k, int l) {
        GuiComponent.applyScissor(SCISSOR_STACK.push(new ScreenRectangle(i, j, k - i, l - j)));
    }

    public static void disableScissor() {
        GuiComponent.applyScissor(SCISSOR_STACK.pop());
    }

    private static void applyScissor(@Nullable ScreenRectangle screenRectangle) {
        if (screenRectangle != null) {
            Window window = Minecraft.getInstance().getWindow();
            int i = window.getHeight();
            double d = window.getGuiScale();
            double e = (double)screenRectangle.left() * d;
            double f = (double)i - (double)screenRectangle.bottom() * d;
            double g = (double)screenRectangle.width() * d;
            double h = (double)screenRectangle.height() * d;
            RenderSystem.enableScissor((int)e, (int)f, Math.max(0, (int)g), Math.max(0, (int)h));
        } else {
            RenderSystem.disableScissor();
        }
    }

    public static void fill(PoseStack poseStack, int i, int j, int k, int l, int m) {
        GuiComponent.fill(poseStack, i, j, k, l, 0, m);
    }

    public static void fill(PoseStack poseStack, int i, int j, int k, int l, int m, int n) {
        int o;
        Matrix4f matrix4f = poseStack.last().pose();
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
        float f = (float)FastColor.ARGB32.alpha(n) / 255.0f;
        float g = (float)FastColor.ARGB32.red(n) / 255.0f;
        float h = (float)FastColor.ARGB32.green(n) / 255.0f;
        float p = (float)FastColor.ARGB32.blue(n) / 255.0f;
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, i, j, m).color(g, h, p, f).endVertex();
        bufferBuilder.vertex(matrix4f, i, l, m).color(g, h, p, f).endVertex();
        bufferBuilder.vertex(matrix4f, k, l, m).color(g, h, p, f).endVertex();
        bufferBuilder.vertex(matrix4f, k, j, m).color(g, h, p, f).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    protected static void fillGradient(PoseStack poseStack, int i, int j, int k, int l, int m, int n) {
        GuiComponent.fillGradient(poseStack, i, j, k, l, m, n, 0);
    }

    protected static void fillGradient(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        GuiComponent.fillGradient(poseStack.last().pose(), bufferBuilder, i, j, k, l, o, m, n);
        tesselator.end();
        RenderSystem.disableBlend();
    }

    protected static void fillGradient(Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n, int o) {
        float f = (float)FastColor.ARGB32.alpha(n) / 255.0f;
        float g = (float)FastColor.ARGB32.red(n) / 255.0f;
        float h = (float)FastColor.ARGB32.green(n) / 255.0f;
        float p = (float)FastColor.ARGB32.blue(n) / 255.0f;
        float q = (float)FastColor.ARGB32.alpha(o) / 255.0f;
        float r = (float)FastColor.ARGB32.red(o) / 255.0f;
        float s = (float)FastColor.ARGB32.green(o) / 255.0f;
        float t = (float)FastColor.ARGB32.blue(o) / 255.0f;
        bufferBuilder.vertex(matrix4f, i, j, m).color(g, h, p, f).endVertex();
        bufferBuilder.vertex(matrix4f, i, l, m).color(r, s, t, q).endVertex();
        bufferBuilder.vertex(matrix4f, k, l, m).color(r, s, t, q).endVertex();
        bufferBuilder.vertex(matrix4f, k, j, m).color(g, h, p, f).endVertex();
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

    public static void blitOutlineBlack(int i, int j, BiConsumer<Integer, Integer> biConsumer) {
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        biConsumer.accept(i + 1, j);
        biConsumer.accept(i - 1, j);
        biConsumer.accept(i, j + 1);
        biConsumer.accept(i, j - 1);
        RenderSystem.defaultBlendFunc();
        biConsumer.accept(i, j);
    }

    public static void blit(PoseStack poseStack, int i, int j, int k, int l, int m, TextureAtlasSprite textureAtlasSprite) {
        GuiComponent.innerBlit(poseStack.last().pose(), i, i + l, j, j + m, k, textureAtlasSprite.getU0(), textureAtlasSprite.getU1(), textureAtlasSprite.getV0(), textureAtlasSprite.getV1());
    }

    public static void blit(PoseStack poseStack, int i, int j, int k, int l, int m, TextureAtlasSprite textureAtlasSprite, float f, float g, float h, float n) {
        GuiComponent.innerBlit(poseStack.last().pose(), i, i + l, j, j + m, k, textureAtlasSprite.getU0(), textureAtlasSprite.getU1(), textureAtlasSprite.getV0(), textureAtlasSprite.getV1(), f, g, h, n);
    }

    public static void renderOutline(PoseStack poseStack, int i, int j, int k, int l, int m) {
        GuiComponent.fill(poseStack, i, j, i + k, j + 1, m);
        GuiComponent.fill(poseStack, i, j + l - 1, i + k, j + l, m);
        GuiComponent.fill(poseStack, i, j + 1, i + 1, j + l - 1, m);
        GuiComponent.fill(poseStack, i + k - 1, j + 1, i + k, j + l - 1, m);
    }

    public static void blit(PoseStack poseStack, int i, int j, int k, int l, int m, int n) {
        GuiComponent.blit(poseStack, i, j, 0, k, l, m, n, 256, 256);
    }

    public static void blit(PoseStack poseStack, int i, int j, int k, float f, float g, int l, int m, int n, int o) {
        GuiComponent.blit(poseStack, i, i + l, j, j + m, k, l, m, f, g, n, o);
    }

    public static void blit(PoseStack poseStack, int i, int j, int k, int l, float f, float g, int m, int n, int o, int p) {
        GuiComponent.blit(poseStack, i, i + k, j, j + l, 0, m, n, f, g, o, p);
    }

    public static void blit(PoseStack poseStack, int i, int j, float f, float g, int k, int l, int m, int n) {
        GuiComponent.blit(poseStack, i, j, k, l, f, g, k, l, m, n);
    }

    private static void blit(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, float f, float g, int p, int q) {
        GuiComponent.innerBlit(poseStack.last().pose(), i, j, k, l, m, (f + 0.0f) / (float)p, (f + (float)n) / (float)p, (g + 0.0f) / (float)q, (g + (float)o) / (float)q);
    }

    private static void innerBlit(Matrix4f matrix4f, int i, int j, int k, int l, int m, float f, float g, float h, float n) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix4f, i, k, m).uv(f, h).endVertex();
        bufferBuilder.vertex(matrix4f, i, l, m).uv(f, n).endVertex();
        bufferBuilder.vertex(matrix4f, j, l, m).uv(g, n).endVertex();
        bufferBuilder.vertex(matrix4f, j, k, m).uv(g, h).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private static void innerBlit(Matrix4f matrix4f, int i, int j, int k, int l, int m, float f, float g, float h, float n, float o, float p, float q, float r) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.enableBlend();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferBuilder.vertex(matrix4f, i, k, m).color(o, p, q, r).uv(f, h).endVertex();
        bufferBuilder.vertex(matrix4f, i, l, m).color(o, p, q, r).uv(f, n).endVertex();
        bufferBuilder.vertex(matrix4f, j, l, m).color(o, p, q, r).uv(g, n).endVertex();
        bufferBuilder.vertex(matrix4f, j, k, m).color(o, p, q, r).uv(g, h).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void blitNineSliced(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, int p, int q) {
        GuiComponent.blitNineSliced(poseStack, i, j, k, l, m, m, m, m, n, o, p, q);
    }

    public static void blitNineSliced(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, int p, int q, int r) {
        GuiComponent.blitNineSliced(poseStack, i, j, k, l, m, n, m, n, o, p, q, r);
    }

    public static void blitNineSliced(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, int p, int q, int r, int s, int t) {
        m = Math.min(m, k / 2);
        o = Math.min(o, k / 2);
        n = Math.min(n, l / 2);
        p = Math.min(p, l / 2);
        if (k == q && l == r) {
            GuiComponent.blit(poseStack, i, j, s, t, k, l);
            return;
        }
        if (l == r) {
            GuiComponent.blit(poseStack, i, j, s, t, m, l);
            GuiComponent.blitRepeating(poseStack, i + m, j, k - o - m, l, s + m, t, q - o - m, r);
            GuiComponent.blit(poseStack, i + k - o, j, s + q - o, t, o, l);
            return;
        }
        if (k == q) {
            GuiComponent.blit(poseStack, i, j, s, t, k, n);
            GuiComponent.blitRepeating(poseStack, i, j + n, k, l - p - n, s, t + n, q, r - p - n);
            GuiComponent.blit(poseStack, i, j + l - p, s, t + r - p, k, p);
            return;
        }
        GuiComponent.blit(poseStack, i, j, s, t, m, n);
        GuiComponent.blitRepeating(poseStack, i + m, j, k - o - m, n, s + m, t, q - o - m, n);
        GuiComponent.blit(poseStack, i + k - o, j, s + q - o, t, o, n);
        GuiComponent.blit(poseStack, i, j + l - p, s, t + r - p, m, p);
        GuiComponent.blitRepeating(poseStack, i + m, j + l - p, k - o - m, p, s + m, t + r - p, q - o - m, p);
        GuiComponent.blit(poseStack, i + k - o, j + l - p, s + q - o, t + r - p, o, p);
        GuiComponent.blitRepeating(poseStack, i, j + n, m, l - p - n, s, t + n, m, r - p - n);
        GuiComponent.blitRepeating(poseStack, i + m, j + n, k - o - m, l - p - n, s + m, t + n, q - o - m, r - p - n);
        GuiComponent.blitRepeating(poseStack, i + k - o, j + n, m, l - p - n, s + q - o, t + n, o, r - p - n);
    }

    public static void blitRepeating(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, int p) {
        int q = i;
        IntIterator intIterator = GuiComponent.slices(k, o);
        while (intIterator.hasNext()) {
            int r = intIterator.nextInt();
            int s = (o - r) / 2;
            int t = j;
            IntIterator intIterator2 = GuiComponent.slices(l, p);
            while (intIterator2.hasNext()) {
                int u = intIterator2.nextInt();
                int v = (p - u) / 2;
                GuiComponent.blit(poseStack, q, t, m + s, n + v, r, u);
                t += u;
            }
            q += r;
        }
    }

    private static IntIterator slices(int i, int j) {
        int k = Mth.positiveCeilDiv(i, j);
        return new Divisor(i, k);
    }

    @Environment(value=EnvType.CLIENT)
    static class ScissorStack {
        private final Deque<ScreenRectangle> stack = new ArrayDeque<ScreenRectangle>();

        ScissorStack() {
        }

        public ScreenRectangle push(ScreenRectangle screenRectangle) {
            ScreenRectangle screenRectangle2 = this.stack.peekLast();
            if (screenRectangle2 != null) {
                ScreenRectangle screenRectangle3 = Objects.requireNonNullElse(screenRectangle.intersection(screenRectangle2), ScreenRectangle.empty());
                this.stack.addLast(screenRectangle3);
                return screenRectangle3;
            }
            this.stack.addLast(screenRectangle);
            return screenRectangle;
        }

        @Nullable
        public ScreenRectangle pop() {
            if (this.stack.isEmpty()) {
                throw new IllegalStateException("Scissor stack underflow");
            }
            this.stack.removeLast();
            return this.stack.peekLast();
        }
    }
}


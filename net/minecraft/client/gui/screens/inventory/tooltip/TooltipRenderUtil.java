/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory.tooltip;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class TooltipRenderUtil {
    public static final int MOUSE_OFFSET = 12;
    private static final int PADDING = 3;
    public static final int PADDING_LEFT = 3;
    public static final int PADDING_RIGHT = 3;
    public static final int PADDING_TOP = 3;
    public static final int PADDING_BOTTOM = 3;
    private static final int BACKGROUND_COLOR = -267386864;
    private static final int BORDER_COLOR_TOP = 0x505000FF;
    private static final int BORDER_COLOR_BOTTOM = 1344798847;

    public static void renderTooltipBackground(BlitPainter blitPainter, Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m) {
        int n = i - 3;
        int o = j - 3;
        int p = k + 3 + 3;
        int q = l + 3 + 3;
        TooltipRenderUtil.renderHorizontalLine(blitPainter, matrix4f, bufferBuilder, n, o - 1, p, m, -267386864);
        TooltipRenderUtil.renderHorizontalLine(blitPainter, matrix4f, bufferBuilder, n, o + q, p, m, -267386864);
        TooltipRenderUtil.renderRectangle(blitPainter, matrix4f, bufferBuilder, n, o, p, q, m, -267386864);
        TooltipRenderUtil.renderVerticalLine(blitPainter, matrix4f, bufferBuilder, n - 1, o, q, m, -267386864);
        TooltipRenderUtil.renderVerticalLine(blitPainter, matrix4f, bufferBuilder, n + p, o, q, m, -267386864);
        TooltipRenderUtil.renderFrameGradient(blitPainter, matrix4f, bufferBuilder, n, o + 1, p, q, m, 0x505000FF, 1344798847);
    }

    private static void renderFrameGradient(BlitPainter blitPainter, Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n, int o) {
        TooltipRenderUtil.renderVerticalLineGradient(blitPainter, matrix4f, bufferBuilder, i, j, l - 2, m, n, o);
        TooltipRenderUtil.renderVerticalLineGradient(blitPainter, matrix4f, bufferBuilder, i + k - 1, j, l - 2, m, n, o);
        TooltipRenderUtil.renderHorizontalLine(blitPainter, matrix4f, bufferBuilder, i, j - 1, k, m, n);
        TooltipRenderUtil.renderHorizontalLine(blitPainter, matrix4f, bufferBuilder, i, j - 1 + l - 1, k, m, o);
    }

    private static void renderVerticalLine(BlitPainter blitPainter, Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m) {
        blitPainter.blit(matrix4f, bufferBuilder, i, j, i + 1, j + k, l, m, m);
    }

    private static void renderVerticalLineGradient(BlitPainter blitPainter, Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n) {
        blitPainter.blit(matrix4f, bufferBuilder, i, j, i + 1, j + k, l, m, n);
    }

    private static void renderHorizontalLine(BlitPainter blitPainter, Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m) {
        blitPainter.blit(matrix4f, bufferBuilder, i, j, i + k, j + 1, l, m, m);
    }

    private static void renderRectangle(BlitPainter blitPainter, Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n) {
        blitPainter.blit(matrix4f, bufferBuilder, i, j, i + k, j + l, m, n, n);
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface BlitPainter {
        public void blit(Matrix4f var1, BufferBuilder var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9);
    }
}


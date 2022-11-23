package net.minecraft.client.gui.screens.inventory.tooltip;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class TooltipRenderUtil {
	public static final int MOUSE_OFFSET = 12;
	private static final int PADDING = 3;
	public static final int PADDING_LEFT = 3;
	public static final int PADDING_RIGHT = 3;
	public static final int PADDING_TOP = 3;
	public static final int PADDING_BOTTOM = 3;
	private static final int BACKGROUND_COLOR = -267386864;
	private static final int BORDER_COLOR_TOP = 1347420415;
	private static final int BORDER_COLOR_BOTTOM = 1344798847;

	public static void renderTooltipBackground(
		TooltipRenderUtil.BlitPainter blitPainter, Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m
	) {
		int n = i - 3;
		int o = j - 3;
		int p = k + 3 + 3;
		int q = l + 3 + 3;
		renderHorizontalLine(blitPainter, matrix4f, bufferBuilder, n, o - 1, p, m, -267386864);
		renderHorizontalLine(blitPainter, matrix4f, bufferBuilder, n, o + q, p, m, -267386864);
		renderRectangle(blitPainter, matrix4f, bufferBuilder, n, o, p, q, m, -267386864);
		renderVerticalLine(blitPainter, matrix4f, bufferBuilder, n - 1, o, q, m, -267386864);
		renderVerticalLine(blitPainter, matrix4f, bufferBuilder, n + p, o, q, m, -267386864);
		renderFrameGradient(blitPainter, matrix4f, bufferBuilder, n, o + 1, p, q, m, 1347420415, 1344798847);
	}

	private static void renderFrameGradient(
		TooltipRenderUtil.BlitPainter blitPainter, Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n, int o
	) {
		renderVerticalLineGradient(blitPainter, matrix4f, bufferBuilder, i, j, l - 2, m, n, o);
		renderVerticalLineGradient(blitPainter, matrix4f, bufferBuilder, i + k - 1, j, l - 2, m, n, o);
		renderHorizontalLine(blitPainter, matrix4f, bufferBuilder, i, j - 1, k, m, n);
		renderHorizontalLine(blitPainter, matrix4f, bufferBuilder, i, j - 1 + l - 1, k, m, o);
	}

	private static void renderVerticalLine(
		TooltipRenderUtil.BlitPainter blitPainter, Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m
	) {
		blitPainter.blit(matrix4f, bufferBuilder, i, j, i + 1, j + k, l, m, m);
	}

	private static void renderVerticalLineGradient(
		TooltipRenderUtil.BlitPainter blitPainter, Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n
	) {
		blitPainter.blit(matrix4f, bufferBuilder, i, j, i + 1, j + k, l, m, n);
	}

	private static void renderHorizontalLine(
		TooltipRenderUtil.BlitPainter blitPainter, Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m
	) {
		blitPainter.blit(matrix4f, bufferBuilder, i, j, i + k, j + 1, l, m, m);
	}

	private static void renderRectangle(
		TooltipRenderUtil.BlitPainter blitPainter, Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n
	) {
		blitPainter.blit(matrix4f, bufferBuilder, i, j, i + k, j + l, m, n, n);
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface BlitPainter {
		void blit(Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n, int o);
	}
}

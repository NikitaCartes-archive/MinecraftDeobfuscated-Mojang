package net.minecraft.client.gui.screens.inventory.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;

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

	public static void renderTooltipBackground(GuiGraphics guiGraphics, int i, int j, int k, int l, int m) {
		int n = i - 3;
		int o = j - 3;
		int p = k + 3 + 3;
		int q = l + 3 + 3;
		renderHorizontalLine(guiGraphics, n, o - 1, p, m, -267386864);
		renderHorizontalLine(guiGraphics, n, o + q, p, m, -267386864);
		renderRectangle(guiGraphics, n, o, p, q, m, -267386864);
		renderVerticalLine(guiGraphics, n - 1, o, q, m, -267386864);
		renderVerticalLine(guiGraphics, n + p, o, q, m, -267386864);
		renderFrameGradient(guiGraphics, n, o + 1, p, q, m, 1347420415, 1344798847);
	}

	private static void renderFrameGradient(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o) {
		renderVerticalLineGradient(guiGraphics, i, j, l - 2, m, n, o);
		renderVerticalLineGradient(guiGraphics, i + k - 1, j, l - 2, m, n, o);
		renderHorizontalLine(guiGraphics, i, j - 1, k, m, n);
		renderHorizontalLine(guiGraphics, i, j - 1 + l - 1, k, m, o);
	}

	private static void renderVerticalLine(GuiGraphics guiGraphics, int i, int j, int k, int l, int m) {
		guiGraphics.fill(i, j, i + 1, j + k, l, m);
	}

	private static void renderVerticalLineGradient(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n) {
		guiGraphics.fillGradient(i, j, i + 1, j + k, l, m, n);
	}

	private static void renderHorizontalLine(GuiGraphics guiGraphics, int i, int j, int k, int l, int m) {
		guiGraphics.fill(i, j, i + k, j + 1, l, m);
	}

	private static void renderRectangle(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n) {
		guiGraphics.fill(i, j, i + k, j + l, m, n);
	}
}

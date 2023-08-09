package net.minecraft.client.gui.components.debugchart;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.SampleLogger;

@Environment(EnvType.CLIENT)
public abstract class AbstractDebugChart {
	protected static final int COLOR_GREY = 14737632;
	protected static final int CHART_HEIGHT = 60;
	protected static final int LINE_WIDTH = 1;
	protected final Font font;
	protected final SampleLogger logger;

	protected AbstractDebugChart(Font font, SampleLogger sampleLogger) {
		this.font = font;
		this.logger = sampleLogger;
	}

	public int getWidth(int i) {
		return Math.min(this.logger.capacity() + 2, i);
	}

	public void drawChart(GuiGraphics guiGraphics, int i, int j) {
		int k = guiGraphics.guiHeight();
		guiGraphics.fill(RenderType.guiOverlay(), i, k - 60, i + j, k, -1873784752);
		long l = 0L;
		long m = 2147483647L;
		long n = -2147483648L;
		int o = Math.max(0, this.logger.capacity() - (j - 2));
		int p = this.logger.size() - o;

		for (int q = 0; q < p; q++) {
			int r = i + q + 1;
			long s = this.logger.get(o + q);
			m = Math.min(m, s);
			n = Math.max(n, s);
			l += s;
			int t = this.getSampleHeight((double)s);
			int u = this.getSampleColor(s);
			guiGraphics.fill(RenderType.guiOverlay(), r, k - t, r + 1, k, u);
		}

		guiGraphics.hLine(RenderType.guiOverlay(), i, i + j - 1, k - 60, -1);
		guiGraphics.hLine(RenderType.guiOverlay(), i, i + j - 1, k - 1, -1);
		guiGraphics.vLine(RenderType.guiOverlay(), i, k - 60, k, -1);
		guiGraphics.vLine(RenderType.guiOverlay(), i + j - 1, k - 60, k, -1);
		if (p > 0) {
			String string = this.toDisplayString((double)m) + " min";
			String string2 = this.toDisplayString((double)l / (double)p) + " avg";
			String string3 = this.toDisplayString((double)n) + " max";
			guiGraphics.drawString(this.font, string, i + 2, k - 60 - 9, 14737632);
			guiGraphics.drawCenteredString(this.font, string2, i + j / 2, k - 60 - 9, 14737632);
			guiGraphics.drawString(this.font, string3, i + j - this.font.width(string3) - 2, k - 60 - 9, 14737632);
		}

		this.renderAdditionalLinesAndLabels(guiGraphics, i, j, k);
	}

	protected void renderAdditionalLinesAndLabels(GuiGraphics guiGraphics, int i, int j, int k) {
	}

	protected void drawStringWithShade(GuiGraphics guiGraphics, String string, int i, int j) {
		guiGraphics.fill(RenderType.guiOverlay(), i, j, i + this.font.width(string) + 1, j + 9, -1873784752);
		guiGraphics.drawString(this.font, string, i + 1, j + 1, 14737632, false);
	}

	protected abstract String toDisplayString(double d);

	protected abstract int getSampleHeight(double d);

	protected abstract int getSampleColor(long l);

	protected int getSampleColor(double d, double e, int i, double f, int j, double g, int k) {
		d = Mth.clamp(d, e, g);
		return d < f ? FastColor.ARGB32.lerp((float)(d / (f - e)), i, j) : FastColor.ARGB32.lerp((float)((d - f) / (g - f)), j, k);
	}
}

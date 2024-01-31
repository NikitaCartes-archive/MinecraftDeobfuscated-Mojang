package net.minecraft.client.gui.components.debugchart;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.debugchart.SampleStorage;

@Environment(EnvType.CLIENT)
public abstract class AbstractDebugChart {
	protected static final int COLOR_GREY = 14737632;
	protected static final int CHART_HEIGHT = 60;
	protected static final int LINE_WIDTH = 1;
	protected final Font font;
	protected final SampleStorage sampleStorage;

	protected AbstractDebugChart(Font font, SampleStorage sampleStorage) {
		this.font = font;
		this.sampleStorage = sampleStorage;
	}

	public int getWidth(int i) {
		return Math.min(this.sampleStorage.capacity() + 2, i);
	}

	public void drawChart(GuiGraphics guiGraphics, int i, int j) {
		int k = guiGraphics.guiHeight();
		guiGraphics.fill(RenderType.guiOverlay(), i, k - 60, i + j, k, -1873784752);
		long l = 0L;
		long m = 2147483647L;
		long n = -2147483648L;
		int o = Math.max(0, this.sampleStorage.capacity() - (j - 2));
		int p = this.sampleStorage.size() - o;

		for (int q = 0; q < p; q++) {
			int r = i + q + 1;
			int s = o + q;
			long t = this.getValueForAggregation(s);
			m = Math.min(m, t);
			n = Math.max(n, t);
			l += t;
			this.drawDimensions(guiGraphics, k, r, s);
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

	protected void drawDimensions(GuiGraphics guiGraphics, int i, int j, int k) {
		this.drawMainDimension(guiGraphics, i, j, k);
		this.drawAdditionalDimensions(guiGraphics, i, j, k);
	}

	protected void drawMainDimension(GuiGraphics guiGraphics, int i, int j, int k) {
		long l = this.sampleStorage.get(k);
		int m = this.getSampleHeight((double)l);
		int n = this.getSampleColor(l);
		guiGraphics.fill(RenderType.guiOverlay(), j, i - m, j + 1, i, n);
	}

	protected void drawAdditionalDimensions(GuiGraphics guiGraphics, int i, int j, int k) {
	}

	protected long getValueForAggregation(int i) {
		return this.sampleStorage.get(i);
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
		return d < f ? FastColor.ARGB32.lerp((float)((d - e) / (f - e)), i, j) : FastColor.ARGB32.lerp((float)((d - f) / (g - f)), j, k);
	}
}

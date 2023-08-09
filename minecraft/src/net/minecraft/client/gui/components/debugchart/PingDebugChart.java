package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.SampleLogger;

@Environment(EnvType.CLIENT)
public class PingDebugChart extends AbstractDebugChart {
	private static final int RED = -65536;
	private static final int YELLOW = -256;
	private static final int GREEN = -16711936;
	private static final int CHART_TOP_VALUE = 500;

	public PingDebugChart(Font font, SampleLogger sampleLogger) {
		super(font, sampleLogger);
	}

	@Override
	protected void renderAdditionalLinesAndLabels(GuiGraphics guiGraphics, int i, int j, int k) {
		this.drawStringWithShade(guiGraphics, "500 ms", i + 1, k - 60 + 1);
	}

	@Override
	protected String toDisplayString(double d) {
		return String.format(Locale.ROOT, "%d ms", (int)Math.round(d));
	}

	@Override
	protected int getSampleHeight(double d) {
		return (int)Math.round(d * 60.0 / 500.0);
	}

	@Override
	protected int getSampleColor(long l) {
		return this.getSampleColor((double)l, 0.0, -16711936, 250.0, -256, 500.0, -65536);
	}
}

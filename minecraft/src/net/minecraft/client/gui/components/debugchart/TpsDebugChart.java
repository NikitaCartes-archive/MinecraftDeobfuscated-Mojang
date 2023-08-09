package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.SampleLogger;

@Environment(EnvType.CLIENT)
public class TpsDebugChart extends AbstractDebugChart {
	private static final int RED = -65536;
	private static final int YELLOW = -256;
	private static final int GREEN = -16711936;
	private static final int CHART_TOP_VALUE = 50;

	public TpsDebugChart(Font font, SampleLogger sampleLogger) {
		super(font, sampleLogger);
	}

	@Override
	protected void renderAdditionalLinesAndLabels(GuiGraphics guiGraphics, int i, int j, int k) {
		this.drawStringWithShade(guiGraphics, "20 TPS", i + 1, k - 60 + 1);
	}

	@Override
	protected String toDisplayString(double d) {
		return String.format(Locale.ROOT, "%d ms", (int)Math.round(toMilliseconds(d)));
	}

	@Override
	protected int getSampleHeight(double d) {
		return (int)Math.round(toMilliseconds(d) * 60.0 / 50.0);
	}

	@Override
	protected int getSampleColor(long l) {
		return this.getSampleColor(toMilliseconds((double)l), 0.0, -16711936, 25.0, -256, 50.0, -65536);
	}

	private static double toMilliseconds(double d) {
		return d / 1000000.0;
	}
}

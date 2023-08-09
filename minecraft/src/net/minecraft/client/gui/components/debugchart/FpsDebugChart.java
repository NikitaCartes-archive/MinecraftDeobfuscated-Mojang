package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.SampleLogger;

@Environment(EnvType.CLIENT)
public class FpsDebugChart extends AbstractDebugChart {
	private static final int RED = -65536;
	private static final int YELLOW = -256;
	private static final int GREEN = -16711936;
	private static final int CHART_TOP_FPS = 30;
	private static final double CHART_TOP_VALUE = 33.333333333333336;

	public FpsDebugChart(Font font, SampleLogger sampleLogger) {
		super(font, sampleLogger);
	}

	@Override
	protected void renderAdditionalLinesAndLabels(GuiGraphics guiGraphics, int i, int j, int k) {
		this.drawStringWithShade(guiGraphics, "30 FPS", i + 1, k - 60 + 1);
		this.drawStringWithShade(guiGraphics, "60 FPS", i + 1, k - 30 + 1);
		guiGraphics.hLine(RenderType.guiOverlay(), i, i + j - 1, k - 30, -1);
		int l = Minecraft.getInstance().options.framerateLimit().get();
		if (l > 0 && l <= 250) {
			guiGraphics.hLine(RenderType.guiOverlay(), i, i + j - 1, k - this.getSampleHeight(1.0E9 / (double)l) - 1, -16711681);
		}
	}

	@Override
	protected String toDisplayString(double d) {
		return String.format(Locale.ROOT, "%d ms", (int)Math.round(toMilliseconds(d)));
	}

	@Override
	protected int getSampleHeight(double d) {
		return (int)Math.round(toMilliseconds(d) * 60.0 / 33.333333333333336);
	}

	@Override
	protected int getSampleColor(long l) {
		return this.getSampleColor(toMilliseconds((double)l), 0.0, -16711936, 28.0, -256, 56.0, -65536);
	}

	private static double toMilliseconds(double d) {
		return d / 1000000.0;
	}
}

package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.SampleLogger;
import net.minecraft.util.TimeUtil;

@Environment(EnvType.CLIENT)
public class TpsDebugChart extends AbstractDebugChart {
	private static final int RED = -65536;
	private static final int YELLOW = -256;
	private static final int GREEN = -16711936;
	private final Supplier<Float> msptSupplier;

	public TpsDebugChart(Font font, SampleLogger sampleLogger, Supplier<Float> supplier) {
		super(font, sampleLogger);
		this.msptSupplier = supplier;
	}

	@Override
	protected void renderAdditionalLinesAndLabels(GuiGraphics guiGraphics, int i, int j, int k) {
		float f = (float)TimeUtil.MILLISECONDS_PER_SECOND / (Float)this.msptSupplier.get();
		this.drawStringWithShade(guiGraphics, String.format("%.1f TPS", f), i + 1, k - 60 + 1);
	}

	@Override
	protected String toDisplayString(double d) {
		return String.format(Locale.ROOT, "%d ms", (int)Math.round(toMilliseconds(d)));
	}

	@Override
	protected int getSampleHeight(double d) {
		return (int)Math.round(toMilliseconds(d) * 60.0 / (double)((Float)this.msptSupplier.get()).floatValue());
	}

	@Override
	protected int getSampleColor(long l) {
		float f = (Float)this.msptSupplier.get();
		return this.getSampleColor(toMilliseconds((double)l), 0.0, -16711936, (double)f / 2.0, -256, (double)f, -65536);
	}

	private static double toMilliseconds(double d) {
		return d / 1000000.0;
	}
}

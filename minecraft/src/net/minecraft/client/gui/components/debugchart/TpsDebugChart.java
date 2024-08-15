package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.debugchart.SampleStorage;
import net.minecraft.util.debugchart.TpsDebugDimensions;

@Environment(EnvType.CLIENT)
public class TpsDebugChart extends AbstractDebugChart {
	private static final int TICK_METHOD_COLOR = -6745839;
	private static final int TASK_COLOR = -4548257;
	private static final int OTHER_COLOR = -10547572;
	private final Supplier<Float> msptSupplier;

	public TpsDebugChart(Font font, SampleStorage sampleStorage, Supplier<Float> supplier) {
		super(font, sampleStorage);
		this.msptSupplier = supplier;
	}

	@Override
	protected void renderAdditionalLinesAndLabels(GuiGraphics guiGraphics, int i, int j, int k) {
		float f = (float)TimeUtil.MILLISECONDS_PER_SECOND / (Float)this.msptSupplier.get();
		this.drawStringWithShade(guiGraphics, String.format(Locale.ROOT, "%.1f TPS", f), i + 1, k - 60 + 1);
	}

	@Override
	protected void drawAdditionalDimensions(GuiGraphics guiGraphics, int i, int j, int k) {
		long l = this.sampleStorage.get(k, TpsDebugDimensions.TICK_SERVER_METHOD.ordinal());
		int m = this.getSampleHeight((double)l);
		guiGraphics.fill(RenderType.guiOverlay(), j, i - m, j + 1, i, -6745839);
		long n = this.sampleStorage.get(k, TpsDebugDimensions.SCHEDULED_TASKS.ordinal());
		int o = this.getSampleHeight((double)n);
		guiGraphics.fill(RenderType.guiOverlay(), j, i - m - o, j + 1, i - m, -4548257);
		long p = this.sampleStorage.get(k) - this.sampleStorage.get(k, TpsDebugDimensions.IDLE.ordinal()) - l - n;
		int q = this.getSampleHeight((double)p);
		guiGraphics.fill(RenderType.guiOverlay(), j, i - q - o - m, j + 1, i - o - m, -10547572);
	}

	@Override
	protected long getValueForAggregation(int i) {
		return this.sampleStorage.get(i) - this.sampleStorage.get(i, TpsDebugDimensions.IDLE.ordinal());
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
		return this.getSampleColor(toMilliseconds((double)l), (double)f, -16711936, (double)f * 1.125, -256, (double)f * 1.25, -65536);
	}

	private static double toMilliseconds(double d) {
		return d / 1000000.0;
	}
}

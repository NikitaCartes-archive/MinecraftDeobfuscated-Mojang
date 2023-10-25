package net.minecraft.client;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Timer {
	public float partialTick;
	public float tickDelta;
	private long lastMs;
	private final float msPerTick;
	private final FloatUnaryOperator targetMsptProvider;

	public Timer(float f, long l, FloatUnaryOperator floatUnaryOperator) {
		this.msPerTick = 1000.0F / f;
		this.lastMs = l;
		this.targetMsptProvider = floatUnaryOperator;
	}

	public int advanceTime(long l) {
		this.tickDelta = (float)(l - this.lastMs) / this.targetMsptProvider.apply(this.msPerTick);
		this.lastMs = l;
		this.partialTick = this.partialTick + this.tickDelta;
		int i = (int)this.partialTick;
		this.partialTick -= (float)i;
		return i;
	}
}

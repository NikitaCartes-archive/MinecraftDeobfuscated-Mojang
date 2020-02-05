package net.minecraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Timer {
	public float partialTick;
	public float tickDelta;
	private long lastMs;
	private final float msPerTick;

	public Timer(float f, long l) {
		this.msPerTick = 1000.0F / f;
		this.lastMs = l;
	}

	public int advanceTime(long l) {
		this.tickDelta = (float)(l - this.lastMs) / this.msPerTick;
		this.lastMs = l;
		this.partialTick = this.partialTick + this.tickDelta;
		int i = (int)this.partialTick;
		this.partialTick -= (float)i;
		return i;
	}
}

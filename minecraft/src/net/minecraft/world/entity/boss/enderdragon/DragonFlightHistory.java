package net.minecraft.world.entity.boss.enderdragon;

import java.util.Arrays;
import net.minecraft.util.Mth;

public class DragonFlightHistory {
	public static final int LENGTH = 64;
	private static final int MASK = 63;
	private final DragonFlightHistory.Sample[] samples = new DragonFlightHistory.Sample[64];
	private int head = -1;

	public DragonFlightHistory() {
		Arrays.fill(this.samples, new DragonFlightHistory.Sample(0.0, 0.0F));
	}

	public void copyFrom(DragonFlightHistory dragonFlightHistory) {
		System.arraycopy(dragonFlightHistory.samples, 0, this.samples, 0, 64);
		this.head = dragonFlightHistory.head;
	}

	public void record(double d, float f) {
		DragonFlightHistory.Sample sample = new DragonFlightHistory.Sample(d, f);
		if (this.head < 0) {
			Arrays.fill(this.samples, sample);
		}

		if (++this.head == 64) {
			this.head = 0;
		}

		this.samples[this.head] = sample;
	}

	public DragonFlightHistory.Sample get(int i) {
		return this.samples[this.head - i & 63];
	}

	public DragonFlightHistory.Sample get(int i, float f) {
		DragonFlightHistory.Sample sample = this.get(i);
		DragonFlightHistory.Sample sample2 = this.get(i + 1);
		return new DragonFlightHistory.Sample(Mth.lerp((double)f, sample2.y, sample.y), Mth.rotLerp(f, sample2.yRot, sample.yRot));
	}

	public static record Sample(double y, float yRot) {
	}
}

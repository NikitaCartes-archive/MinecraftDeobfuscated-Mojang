package net.minecraft.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RunningTrimmedMean {
	private final long[] values;
	private int count;
	private int cursor;

	public RunningTrimmedMean(int i) {
		this.values = new long[i];
	}

	public long registerValueAndGetMean(long l) {
		if (this.count < this.values.length) {
			this.count++;
		}

		this.values[this.cursor] = l;
		this.cursor = (this.cursor + 1) % this.values.length;
		long m = Long.MAX_VALUE;
		long n = Long.MIN_VALUE;
		long o = 0L;

		for (int i = 0; i < this.count; i++) {
			long p = this.values[i];
			o += p;
			m = Math.min(m, p);
			n = Math.max(n, p);
		}

		if (this.count > 2) {
			o -= m + n;
			return o / (long)(this.count - 2);
		} else {
			return o > 0L ? (long)this.count / o : 0L;
		}
	}
}

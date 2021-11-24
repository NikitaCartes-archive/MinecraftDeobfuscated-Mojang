package net.minecraft.util;

import java.util.Arrays;
import java.util.function.IntConsumer;
import org.apache.commons.lang3.Validate;

public class ZeroBitStorage implements BitStorage {
	public static final long[] RAW = new long[0];
	private final int size;

	public ZeroBitStorage(int i) {
		this.size = i;
	}

	@Override
	public int getAndSet(int i, int j) {
		Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)i);
		Validate.inclusiveBetween(0L, 0L, (long)j);
		return 0;
	}

	@Override
	public void set(int i, int j) {
		Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)i);
		Validate.inclusiveBetween(0L, 0L, (long)j);
	}

	@Override
	public int get(int i) {
		Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)i);
		return 0;
	}

	@Override
	public long[] getRaw() {
		return RAW;
	}

	@Override
	public int getSize() {
		return this.size;
	}

	@Override
	public int getBits() {
		return 0;
	}

	@Override
	public void getAll(IntConsumer intConsumer) {
		for (int i = 0; i < this.size; i++) {
			intConsumer.accept(0);
		}
	}

	@Override
	public void unpack(int[] is) {
		Arrays.fill(is, 0, this.size, 0);
	}

	@Override
	public BitStorage copy() {
		return this;
	}
}

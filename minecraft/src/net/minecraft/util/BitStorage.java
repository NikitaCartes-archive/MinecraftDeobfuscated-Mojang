package net.minecraft.util;

import java.util.function.IntConsumer;

public interface BitStorage {
	int getAndSet(int i, int j);

	void set(int i, int j);

	int get(int i);

	long[] getRaw();

	int getSize();

	int getBits();

	void getAll(IntConsumer intConsumer);

	void unpack(int[] is);

	BitStorage copy();
}

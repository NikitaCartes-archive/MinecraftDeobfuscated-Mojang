package net.minecraft.util.debugchart;

public interface SampleStorage {
	int capacity();

	int size();

	long get(int i);

	long get(int i, int j);

	void reset();
}

package net.minecraft.util.debugchart;

public interface SampleLogger {
	void logFullSample(long[] ls);

	void logSample(long l);

	void logPartialSample(long l, int i);
}
